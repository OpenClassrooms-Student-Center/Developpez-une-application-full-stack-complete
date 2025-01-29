package com.openclassrooms.mddapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDto {

    private Long articleId;

    private String titre;

    private String theme_id;

    private String contenu;

    private Date createdAt;

    private Long user_id;

    private String auteur;
}
