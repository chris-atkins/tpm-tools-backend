package com.poorknight.tpmtoolsbackend.domain;

import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.*;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


public class BaseTestWithDatabase {

	public static PostgreSQLContainer<?> db = new PostgreSQLContainer<>("postgres:16-alpine")
			.withDatabaseName("tpm_tools")
			.withUsername("Chris")
			.withPassword("theBestPassword");

	protected static String url;

	static {
		db.start();
		url = db.getJdbcUrl();
		createSchemaAndSetAsDefault("tpm_tools");
	}

	protected static Connection getConnection() throws SQLException {
		Properties connectionProps = new Properties();
		connectionProps.setProperty("user", db.getUsername());
		connectionProps.setProperty("password", db.getPassword());
		return DriverManager.getConnection(url, connectionProps);
	}

	private static void createSchemaAndSetAsDefault(String schemaName) {
		try {
			while (!db.isRunning()) {
				System.out.println("DB is not running - waiting 1 sec");
				TimeUnit.SECONDS.sleep(1);
			}

			System.out.println("creating schema");
			Connection connection = getConnection();
			Statement createSchemaStatement = connection.createStatement();
			createSchemaStatement.executeUpdate("CREATE SCHEMA " + schemaName);
			createSchemaStatement.executeUpdate("ALTER DATABASE tpm_tools SET search_path TO tpm_tools");
			createSchemaStatement.close();
			connection.close();
			System.out.println("done creating schema");


		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void deleteAllTasks() {
		try {
			Connection connection = getConnection();
			Statement statement = connection.createStatement();
			statement.executeUpdate("DELETE FROM TASK");

			statement.close();
			connection.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void deleteAllTasksAndRows() {

		this.deleteAllTasks();
		try {
			Connection connection = getConnection();
			Statement statement = connection.createStatement();
			statement.executeUpdate("DELETE FROM P1_ROW");

			statement.close();
			connection.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void deleteAllTasksAndRowsAndProjectPlans() {

		this.deleteAllTasksAndRows();
		try {
			Connection connection = getConnection();
			Statement statement = connection.createStatement();
			statement.executeUpdate("DELETE FROM P0_PROJECT_PLAN");

			statement.close();
			connection.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Long createTaskWithSQLOnly(Long rowId, String title) {
		return this.createTaskWithSQLOnly(rowId, title, 1, 1);

	}
	protected Long createTaskWithSQLOnly(Long rowId, String title, Integer size, Integer position) {
		try {
			Connection connection = getConnection();
			Statement statement = connection.createStatement();
			statement.executeUpdate("INSERT INTO TASK (P1_ROW_FK, TITLE, SIZE, POSITION) VALUES (" + rowId + ", '" + title + "', " + size + ", " + position + ")");
			statement.close();

			Statement taskQueryStatement = connection.createStatement();
			ResultSet resultSet = taskQueryStatement.executeQuery("SELECT * FROM TASK WHERE P1_ROW_FK='" + rowId +"' AND TITLE='" + title + "'");
			Long taskId = null;
			while (resultSet.next()) {
				taskId = resultSet.getLong("ID");
			}
			taskQueryStatement.close();
			connection.close();

			return taskId;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Long createRowWithSQLOnly(Long projectPlanId, String title) {
		try {
			Connection connection = getConnection();

			Statement statement = connection.createStatement();
			statement.executeUpdate("INSERT INTO p1_row (p0_project_plan_fk, title) VALUES (" + projectPlanId + ", '" + title + "')");
			statement.close();

			Statement rowQueryStatement = connection.createStatement();
			ResultSet resultSet = rowQueryStatement.executeQuery("SELECT * FROM p1_row WHERE p0_project_plan_fk=" + projectPlanId + " AND TITLE='" + title + "'");

			int count = 0;
			Long rowId = null;
			while (resultSet.next()) {
				count++;
				rowId = resultSet.getLong("ID");
			}

			rowQueryStatement.close();
			connection.close();

			if (count != 1) {
				throw new RuntimeException("Expecting exactly 1 result for row query.  Instead found " + count);
			}
			return rowId;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Long createProjectPlanWithSQLOnly(String title) {
		try {
			Connection connection = getConnection();

			Statement statement = connection.createStatement();
			statement.executeUpdate("INSERT INTO p0_project_plan (title) VALUES ('" + title + "')");
			statement.close();

			Statement rowQueryStatement = connection.createStatement();
			ResultSet resultSet = rowQueryStatement.executeQuery("SELECT * FROM p0_project_plan WHERE title='" + title + "'");

			int count = 0;
			Long projectPlanId = null;
			while (resultSet.next()) {
				count++;
				projectPlanId = resultSet.getLong("ID");
			}

			rowQueryStatement.close();
			connection.close();

			if (count != 1) {
				throw new RuntimeException("Expecting exactly 1 result for row query.  Instead found " + count);
			}
			return projectPlanId;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
