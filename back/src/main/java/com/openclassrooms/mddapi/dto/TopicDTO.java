package com.openclassrooms.mddapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openclassrooms.mddapi.validation.Validation;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopicDTO {
    private Long id;
    @NotEmpty(message = "Le titre ne peut pas être vide.")
    @NotNull(message = "Le titre ne peut pas être NULL.")
    private String title;
    @NotEmpty(message = "Le contenu ne peut pas être vide.")
    @NotNull(message = "Le contenu ne peut pas être NULL.")
    private String description;
    private String createdAt;
    private String updatedAt;
}
