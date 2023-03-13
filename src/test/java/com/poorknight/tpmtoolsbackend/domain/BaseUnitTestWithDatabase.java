package com.poorknight.tpmtoolsbackend.domain;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.sql.*;

@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {Repository.class, Service.class}))
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = {BaseUnitTestWithDatabase.DataSourceInitializer.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BaseUnitTestWithDatabase extends BaseTestWithDatabase {

	public static class DataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		@Override
		public void initialize(ConfigurableApplicationContext applicationContext) {
			TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
					applicationContext,
					"spring.datasource.url=" + db.getJdbcUrl(),
					"spring.datasource.username=" + db.getUsername(),
					"spring.datasource.password=" + db.getPassword()
			);
		}
	}

	protected int findTotalNumberOfTasks() {
		try {
			Connection connection = this.getConnection();
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT * FROM TASK");

			int count = 0;
			while (resultSet.next()) {
				count++;
			}
			resultSet.close();
			statement.close();
			connection.close();
			return count;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
