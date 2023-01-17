package com.poorknight.tpmtoolsbackend.domain.hello;

import lombok.*;
import javax.persistence.*;

@Entity
@Data
@RequiredArgsConstructor
@NoArgsConstructor
@Table(name = "\"HELLO\"")
public class HelloMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NonNull
	@Column(nullable = false)
	private String message;
}
