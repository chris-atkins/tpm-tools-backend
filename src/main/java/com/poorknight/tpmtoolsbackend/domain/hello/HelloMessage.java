package com.poorknight.tpmtoolsbackend.domain.hello;

import lombok.*;
import jakarta.persistence.*;

@Entity
@Data
@RequiredArgsConstructor
@NoArgsConstructor
@Table(name = "hello")
public class HelloMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NonNull
	@Column(nullable = false)
	private String message;
}
