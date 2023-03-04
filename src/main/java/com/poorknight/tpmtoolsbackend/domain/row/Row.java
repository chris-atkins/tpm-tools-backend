package com.poorknight.tpmtoolsbackend.domain.row;

import com.google.common.collect.ImmutableList;
import com.poorknight.tpmtoolsbackend.domain.tasks.Task;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@Table(name = "\"P1_ROW\"")
public class Row {

	public Row() {
		this(null, null,  ImmutableList.<Task>builder().build());
	}

	public Row(String title) {
		this(null, title);
	}

	public Row (String title, List<Task> taskList) {
		this(null, title, taskList);
	}

	public Row(Long id, String title) {
		this(id, title,  ImmutableList.<Task>builder().build());
	}

	public Row(Long id, String title, List<Task> taskList) {
		this.id = id;
		this.title = title;
		this.taskList = taskList == null ? ImmutableList.<Task>builder().build() : ImmutableList.copyOf(taskList);
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@OneToMany( mappedBy = "rowId", fetch = FetchType.EAGER)
	private List<Task> taskList;
}
