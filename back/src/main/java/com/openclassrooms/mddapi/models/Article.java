package com.openclassrooms.mddapi.models;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "ARTICLES")
@EntityListeners(AuditingEntityListener.class)
@Data
@Accessors(chain = true)
@EqualsAndHashCode(of = {"id"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id")
    private Long articleId;

    @NonNull
    private String titre;

    @NonNull
    private String auteur;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @NonNull
    private String contenu;

    @ManyToOne(cascade=CascadeType.PERSIST)
    @JoinColumn(name = "theme_id", referencedColumnName="theme_id")
    private Theme theme;

    @OneToMany(mappedBy = "article")
    private List<Comment> commentaires;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName="user_id")
    private User user;

}
