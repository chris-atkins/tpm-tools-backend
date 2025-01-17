package com.poorknight.tpmtoolsbackend.domain.hello;

import com.poorknight.tpmtoolsbackend.domain.BaseUnitTestWithDatabase;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.sql.*;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;


class HelloServiceTest extends BaseUnitTestWithDatabase {

	@Autowired
	private HelloService helloService;

	@Test
	@Order(1)
	void isPopulatedByDragonflyBeforeTesting() throws SQLException {
		int count = this.findTotalNumberOfHelloMessages();
		assertThat(count).isEqualTo(3);
	}

	@Test
	void canInsertAHelloMessage() throws SQLException {
		int originalCount = findTotalNumberOfHelloMessages();

		helloService.saveNewMessage(new HelloMessage("Ohai it is me again"));

		int newCount = findTotalNumberOfHelloMessages();
		boolean found = canFindMessageWithText("Ohai it is me again");
		assertThat(newCount).isEqualTo(originalCount + 1);
		assertTrue(found);
	}

	@Test
	void insertingReturnsAnEntityPopulatedWithADatabaseAssignedId() throws SQLException {
		HelloMessage savedMessage = helloService.saveNewMessage(new HelloMessage("one other message"));

		Long id = findIdThatBelongsToEntryWithMessage("one other message");
		assertThat(id).isEqualTo(savedMessage.getId());
	}

	@Test
	void canDelete() throws SQLException {
		HelloMessage savedMessage = helloService.saveNewMessage(new HelloMessage("messageToBeDeleted"));
		int originalCount = findTotalNumberOfHelloMessages();

		helloService.deleteMessageById(savedMessage.getId());

		int newCount = findTotalNumberOfHelloMessages();
		assertThat(newCount).isEqualTo(originalCount -1);
	}

	@Test
	void getAllReturnsTheRightNumberOfMessages() throws Exception{
		int currentCount = findTotalNumberOfHelloMessages();
		helloService.saveNewMessage(new HelloMessage("hi."));

		List<HelloMessage> allMessages = helloService.getAllMessages();
		assertThat(allMessages.size()).isEqualTo(currentCount + 1);
	}

	@Test
	void getAllCanSeeValuesInsertedExternally_ForExampleADifferentInstanceOfTheApp() throws Exception{
		int currentCount = findTotalNumberOfHelloMessages();
		this.insertANewMessage("some other string");

		List<HelloMessage> allMessages = helloService.getAllMessages();

		assertThat(allMessages.size()).isEqualTo(currentCount + 1);
	}

	@Test
	void getAllCanNotSeeDeletedItems() throws Exception{
		helloService.saveNewMessage(new HelloMessage("Oh hello again from the db!"));
		helloService.saveNewMessage(new HelloMessage("DB says fu :) "));

		int initialCount = findTotalNumberOfHelloMessages();
		List<HelloMessage> allMessages = helloService.getAllMessages();
		assertThat(allMessages.size()).isEqualTo(initialCount);

		helloService.deleteMessageById(allMessages.get(0).getId());

		List<HelloMessage> afterDeleting = helloService.getAllMessages();

		helloService.saveNewMessage(new HelloMessage("omfg i keep needing new strings"));
		assertThat(afterDeleting.size()).isEqualTo(initialCount -1);

		List<HelloMessage> afterAdding = helloService.getAllMessages();
		assertThat(afterAdding.size()).isEqualTo(initialCount);

	}

	private int findTotalNumberOfHelloMessages() throws SQLException {
		Connection connection = this.getConnection();
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("SELECT * FROM hello");

		int count = 0;
		while (resultSet.next()) {
			count++;
		}
		resultSet.close();
		statement.close();
		connection.close();
		return count;
	}

	private void insertANewMessage(String message) throws SQLException {
		Connection connection = this.getConnection();
		Statement statement = connection.createStatement();

		statement.execute("INSERT INTO hello(message) VALUES ('" + message + "')");

		statement.close();
		connection.close();
	}

	private boolean canFindMessageWithText(String messageToSearchFor) throws SQLException {
		Connection connection = this.getConnection();
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("SELECT * FROM hello");

		boolean found = false;
		while (resultSet.next()) {
			if (messageToSearchFor.equals(resultSet.getString("message"))) {
				found = true;
			}
		}
		resultSet.close();
		statement.close();
		connection.close();
		return found;
	}

	private Long findIdThatBelongsToEntryWithMessage(String messageToSearchFor) throws SQLException {
		Connection connection = getConnection();
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("SELECT * FROM hello");

		Long id = null;
		while (resultSet.next()) {
			if (messageToSearchFor.equals(resultSet.getString("message"))) {
				id = resultSet.getLong("id");
			}
		}
		resultSet.close();
		statement.close();
		connection.close();
		return id;
	}
}