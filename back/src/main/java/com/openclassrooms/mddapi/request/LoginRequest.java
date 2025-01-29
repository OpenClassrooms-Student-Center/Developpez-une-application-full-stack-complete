package com.openclassrooms.mddapi.request;

import javax.validation.constraints.NotBlank;

public class LoginRequest {
	@NotBlank
  	private String emailOrFirstName;

	@NotBlank
	private String password;

	public String getEmailOrFirstName() {
		return emailOrFirstName;
	}

	public void setEmailOrFirstName(String emailOrFirstName) {
		this.emailOrFirstName = emailOrFirstName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
