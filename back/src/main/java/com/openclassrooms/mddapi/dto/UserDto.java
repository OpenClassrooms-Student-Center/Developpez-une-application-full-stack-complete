package com.openclassrooms.mddapi.dto;

import com.openclassrooms.mddapi.models.Theme;

import java.util.List;

public class UserDto {

    private String email;

    private String firstName;

    private List<Theme> themes;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public List<Theme> getThemes() {
        return themes;
    }

    public void setThemes(List<Theme> themes) {
        this.themes = themes;
    }
}
