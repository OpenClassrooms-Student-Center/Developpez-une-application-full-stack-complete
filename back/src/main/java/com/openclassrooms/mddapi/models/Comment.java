package com.openclassrooms.mddapi.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = "COMMENTS")
@EntityListeners(AuditingEntityListener.class)
@Data
@Accessors(chain = true)
@EqualsAndHashCode(of = {"id"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @NonNull
    private String commentaire;

    @NonNull
    private String auteur;

    @ManyToOne(cascade=CascadeType.PERSIST)
    @JoinColumn(name= "article_id", referencedColumnName = "article_id")
    @JsonIgnore
    private Article article;

    @ManyToOne(cascade=CascadeType.PERSIST)
    @JoinColumn(name= "user_id", referencedColumnName = "user_id")
    private User user;
}
