package com.openclassrooms.mddapi.services;

import com.openclassrooms.mddapi.exception.BadRequestException;
import com.openclassrooms.mddapi.exception.NotFoundException;
import com.openclassrooms.mddapi.models.Theme;
import com.openclassrooms.mddapi.models.User;
import com.openclassrooms.mddapi.repository.ThemeRepository;
import com.openclassrooms.mddapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ThemeRepository themeRepository;

    public User update(Long user_id, User user) {
        user.setUserId(user_id);
        return userRepository.save(user);
    }

    public User findById(Long id) {
        return this.userRepository.findById(id).orElse(null);
    }

    public User findByEmail(String email) {
        User user = this.userRepository.findByEmail(email);
        if (user==null) {
            throw new NotFoundException();
        }
        return user;
    }

    public void follow(Long themeId, Long userId) {
        Theme theme = this.themeRepository.findById(themeId).orElse(null);
        User user = this.userRepository.findById(userId).orElse(null);

        if (theme == null || user == null) {
            throw new NotFoundException();
        }

        boolean alreadyFollow = user.getThemes().stream().anyMatch(o -> o.getThemeId().equals(themeId));
        if(alreadyFollow) {
            throw new BadRequestException();
        }

        themeRepository.save(theme);

        user.getThemes().add(theme);

        this.userRepository.save(user);
    }

    public void unFollow(Long themeId, Long userId) {
        User user = this.userRepository.findById(userId).orElse(null);
        Theme theme = this.themeRepository.findById(themeId).orElse(null);

        if (user == null || theme == null) {
            throw new NotFoundException();
        }

        boolean alreadyFollow = user.getThemes().stream().anyMatch(o -> o.getThemeId().equals(themeId));
        if(!alreadyFollow) {
            throw new BadRequestException();
        }

        themeRepository.save(theme);
        user.setThemes(user.getThemes().stream().filter(themeUp -> !themeUp.getThemeId().equals(themeId)).collect(Collectors.toList()));

        this.userRepository.save(user);
    }
}
