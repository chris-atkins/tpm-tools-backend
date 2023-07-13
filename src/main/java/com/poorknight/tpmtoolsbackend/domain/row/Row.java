package com.poorknight.tpmtoolsbackend.domain.row;

import com.google.common.collect.ImmutableList;
import com.poorknight.tpmtoolsbackend.domain.tasks.Task;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "\"P1_ROW\"")
public class Row {

	public Row() {
		this(null, null, null,  ImmutableList.<Task>builder().build());
	}

	public Row(Long projectPlanId, String title) {
		this(null, projectPlanId, title);
	}

	public Row (Long projectPlanId, String title, List<Task> taskList) {
		this(null, projectPlanId, title, taskList);
	}

	public Row(Long id, Long projectPlanId, String title) {
		this(id, projectPlanId, title, ImmutableList.<Task>builder().build());
	}

	public Row(Long id, Long projectPlanId, String title, List<Task> taskList) {
		this.id = id;
		this.projectPlanId = projectPlanId;
		this.title = title;
		this.taskList = taskList == null ? ImmutableList.<Task>builder().build() : ImmutableList.copyOf(taskList);
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "P0_PROJECT_PLAN_FK", nullable = false)
	private Long projectPlanId;

	@Column(nullable = false)
	private String title;

	@OneToMany(mappedBy = "rowId", fetch = FetchType.EAGER)
	private List<Task> taskList;
}
