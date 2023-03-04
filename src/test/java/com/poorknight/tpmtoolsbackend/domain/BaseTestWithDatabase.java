package com.poorknight.tpmtoolsbackend.domain;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.*;
import java.util.Properties;


public class BaseTestWithDatabase {

	public static MySQLContainer db = (MySQLContainer) new MySQLContainer(DockerImageName.parse("mysql:8.0.27"))
			.withDatabaseName("tpm-tools")
			.withUsername("Chris")
			.withPassword("theBestPassword");

	protected static String url;

	static {
		db.start();
		url = db.getJdbcUrl();
	}

	protected Connection getConnection() throws SQLException {
		Properties connectionProps = new Properties();
		connectionProps.setProperty("user", db.getUsername());
		connectionProps.setProperty("password", db.getPassword());
		return DriverManager.getConnection(this.url, connectionProps);
	}

	protected void deleteAllTasks() {
		try {
			Connection connection = this.getConnection();
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
			Connection connection = this.getConnection();
			Statement statement = connection.createStatement();
			statement.executeUpdate("DELETE FROM P1_ROW");

			statement.close();
			connection.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Long createTaskWithSQLOnly(Long rowId, String title) {
		try {
			Connection connection = this.getConnection();
			Statement statement = connection.createStatement();
			statement.executeUpdate("INSERT INTO TASK (P1_ROW_FK, TITLE, SIZE) VALUES (" + rowId + ", \"" + title + "\", 1)");
			statement.close();

			Statement taskQueryStatement = connection.createStatement();
			ResultSet resultSet = taskQueryStatement.executeQuery("SELECT * FROM TASK WHERE P1_ROW_FK=\"" + rowId +"\" AND TITLE=\"" + title + "\"");
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

	protected Long createRowWithSQLOnly(String title) {
		try {
			Connection connection = this.getConnection();

			Statement statement = connection.createStatement();
			statement.executeUpdate("INSERT INTO P1_ROW (TITLE) VALUES (\"" + title + "\")");
			statement.close();

			Statement rowQueryStatement = connection.createStatement();
			ResultSet resultSet = rowQueryStatement.executeQuery("SELECT * FROM P1_ROW WHERE TITLE=\"" + title + "\"");

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
}
