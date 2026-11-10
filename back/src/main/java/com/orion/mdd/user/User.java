package com.orion.mdd.user;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A registered user. The password field holds the hash, never the password itself.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String email;

    private String password;

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    /**
     * Changes the public name and the e-mail address. The identifier stays, so the user's token stays valid.
     *
     * @param username
     *            the new public name
     * @param email
     *            the new e-mail address
     */
    public void changeProfile(String username, String email) {
        this.username = username;
        this.email = email;
    }

    /**
     * Replaces the password.
     *
     * @param passwordHash
     *            the hash of the new password
     */
    public void changePassword(String passwordHash) {
        this.password = passwordHash;
    }

}
