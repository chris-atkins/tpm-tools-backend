package com.poorknight.tpmtoolsbackend.domain.projectplan;

import com.poorknight.tpmtoolsbackend.domain.row.Row;
import lombok.Data;

import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.List;

@Entity
@Data
@Table(name = "P0_PROJECT_PLAN")
public class ProjectPlan {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = true)
	private String title;

	@OneToMany(mappedBy = "projectPlanId", fetch = FetchType.EAGER)
	private List<Row> rowList;
}
