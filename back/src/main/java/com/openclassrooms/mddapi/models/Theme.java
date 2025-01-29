package com.openclassrooms.mddapi.models;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = "THEMES")
@EntityListeners(AuditingEntityListener.class)
@Data
@Accessors(chain = true)
@EqualsAndHashCode(of = {"themeId"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "theme_id")
    private Long themeId;

    @NonNull
    private String titre;

    @NonNull
    private String description;

    private Boolean follow;


//    @OneToMany(mappedBy = "theme")
//    private List<Article> articles;

//    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "themes")
//    private List<User> users;

}
