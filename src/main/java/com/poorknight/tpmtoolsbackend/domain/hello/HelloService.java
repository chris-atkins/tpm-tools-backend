package com.poorknight.tpmtoolsbackend.domain.hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class HelloService {

	@Autowired
	private HelloRepository helloRepository;


	public HelloMessage saveNewMessage(HelloMessage messageToSave) {
		return helloRepository.save(messageToSave);
	}

	public void deleteMessageById(Long id) {
		helloRepository.deleteById(id);
	}

	public List<HelloMessage> getAllMessages() {
		Iterable<HelloMessage> allMessages = helloRepository.findAll();

		ArrayList<HelloMessage> messageList = new ArrayList<>();
		for (HelloMessage message : allMessages) {
			messageList.add(message);
		}
		return messageList;
	}

	public HelloMessage getRandomHelloMessage() {
		List<HelloMessage> allMessages = this.getAllMessages();
		int index = new Random().nextInt(0, allMessages.size());
		return allMessages.get(index);
	}
}
