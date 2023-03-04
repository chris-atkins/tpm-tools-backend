package com.poorknight.tpmtoolsbackend.domain.tasks;

import lombok.*;

import javax.persistence.*;

@Entity
@Data
@Table(name = "\"TASK\"")
public class Task {

	public Task() {
		this(null, null, null, null);
	}

	public Task(Long rowId, String title, Integer size) {
		this(null, rowId, title, size);
	}

	public Task(Long id, Long rowId, String title, Integer size) {
		this.id = id;
		this.rowId = rowId;
		this.title = title;
		this.size = size;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "P1_ROW_FK", nullable = false)
	private Long rowId;

	@Column(nullable = false)
	private String title;

	@Column(nullable=false)
	private Integer size;
}
