package com.orion.mdd.user;

import com.orion.mdd.topic.Topic;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * A registered user and the topics they follow. The password field holds the hash, never the password itself.
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

    // The topics the user follows. Only the methods below read or change them, so they have no getter. Hibernate
    // replaces this set with its own when it saves or loads the user, so the field cannot be final
    @ManyToMany
    @JoinTable(name = "subscriptions", joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_id"))
    @Getter(AccessLevel.NONE)
    @SuppressWarnings("FieldMayBeFinal")
    private Set<Topic> subscriptions = new HashSet<>();

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

    /**
     * Tells whether the user follows a topic. Within a transaction, Hibernate gives each row a single object, so
     * the topic is found among the subscriptions without comparing identifiers.
     *
     * @param topic
     *            a topic loaded in the same transaction
     * @return true if the user follows it
     */
    public boolean isSubscribedTo(Topic topic) {
        return subscriptions.contains(topic);
    }

    /**
     * Follows a topic. Following it again changes nothing, since the subscriptions form a set.
     *
     * @param topic
     *            a topic loaded in the same transaction
     */
    public void subscribe(Topic topic) {
        subscriptions.add(topic);
    }

    /**
     * Stops following a topic. Nothing changes if the user did not follow it.
     *
     * @param topic
     *            a topic loaded in the same transaction
     */
    public void unsubscribe(Topic topic) {
        subscriptions.remove(topic);
    }

}
