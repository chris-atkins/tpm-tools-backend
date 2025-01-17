package com.poorknight.tpmtoolsbackend.domain.projectplan.entity;

import com.poorknight.tpmtoolsbackend.domain.row.entity.Row;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "p0_project_plan")
public class ProjectPlan {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = true)
	private String title;

	@OneToMany(mappedBy = "projectPlanId", fetch = FetchType.EAGER)
	private List<Row> rowList;
}
