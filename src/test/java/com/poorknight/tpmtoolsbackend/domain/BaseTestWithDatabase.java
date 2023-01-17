package com.poorknight.tpmtoolsbackend.domain;

import org.junit.jupiter.api.BeforeEach;
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
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {Repository.class, Service.class}))
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = {BaseTestWithDatabase.DataSourceInitializer.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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


	protected Connection getConnection() throws SQLException {
		Properties connectionProps = new Properties();
		connectionProps.setProperty("user", "Chris");
		connectionProps.setProperty("password", "theBestPassword");
		return DriverManager.getConnection(this.url, connectionProps);
	}
}
