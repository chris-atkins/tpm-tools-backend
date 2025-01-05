package com.poorknight.tpmtoolsbackend.domain.tasks.entity;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Data
@Table(name = "task")
public class Task {

	public Task() {
		this(null, null, null, null, null);
	}

	public Task(Long rowId, String title, Integer size, Integer position) {
		this(null, rowId, title, size, position);
	}

	public Task(Long id, Long rowId, String title, Integer size, Integer position) {
		this.id = id;
		this.rowId = rowId;
		this.title = title;
		this.size = size;
		this.position = position;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "p1_row_fk", nullable = false)
	private Long rowId;

	@Column(nullable = false)
	private String title;

	@Column(nullable=false)
	private Integer size;

	@Column(nullable = false)
	private Integer position;
}
