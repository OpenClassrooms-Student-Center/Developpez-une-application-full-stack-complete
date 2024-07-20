package com.openclassrooms.mddapi.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.openclassrooms.mddapi.validation.Validation;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {
    private Long id;
    @NotNull(message = "Le post ne peut pas être NULL.")
    @JsonProperty("post_id")
    private Long postId;
    @NotNull(message = "L'utilisateur ne peut pas être NULL.")
    @JsonProperty("user_id")
    private Long userId;
    @NotEmpty(message = "Le contenu ne peut pas être vide.")
    @NotNull(message = "Le contenu ne peut pas être NULL.")
    private String content;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;
}
