package com.poorknight.tpmtoolsbackend.domain.tasks;

import lombok.*;

import javax.persistence.*;

@Entity
@Data
@Table(name = "\"TASK\"")
public class Task {

	public Task() {
		this(null, null, null);
	}

	public Task(String title, Integer size) {
		this(null, title, size);
	}

	public Task(Long id, String title, Integer size) {
		this.id = id;
		this.title = title;
		this.size = size;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable=false)
	private Integer size;
}
