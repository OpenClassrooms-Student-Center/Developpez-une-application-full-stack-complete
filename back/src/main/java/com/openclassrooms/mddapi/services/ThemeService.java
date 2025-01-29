package com.openclassrooms.mddapi.services;

import com.openclassrooms.mddapi.exception.BadRequestException;
import com.openclassrooms.mddapi.exception.NotFoundException;
import com.openclassrooms.mddapi.models.Theme;
import com.openclassrooms.mddapi.models.User;
import com.openclassrooms.mddapi.repository.ThemeRepository;
import com.openclassrooms.mddapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ThemeService {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Theme> findAll() {
        return this.themeRepository.findAll();
    }

    public Theme findById(Long themeId) {
        return this.themeRepository.findById(themeId).orElse(null);
    }

    public Theme update(Long theme_id, Theme theme) {
        theme.setThemeId(theme_id);
        return themeRepository.save(theme);
    }
}
