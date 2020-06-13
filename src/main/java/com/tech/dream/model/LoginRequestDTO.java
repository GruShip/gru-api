package com.tech.dream.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequestDTO {
	private String username;
	private String password;
	private String type;
}
