package com.openclassrooms.mddapi.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtResponse {
  private String token;
  private String type = "Bearer";
  private Long id;
  private String username;
  private String firstName;

  public JwtResponse(String accessToken, Long id, String username, String firstName) {
    this.token = accessToken;
    this.id = id;
    this.firstName = firstName;
    this.username = username;
  }
}
