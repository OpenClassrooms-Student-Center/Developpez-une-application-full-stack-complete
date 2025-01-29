package com.openclassrooms.mddapi.controllers;

import com.openclassrooms.mddapi.dto.UserDto;
import com.openclassrooms.mddapi.dto.UserUpdateDto;
import com.openclassrooms.mddapi.models.User;
import com.openclassrooms.mddapi.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {

    private PasswordEncoder passwordEncoder;

    private UserService userService;

    public UserController(PasswordEncoder passwordEncoder, UserService userService) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @GetMapping("/{user_id}")
    public ResponseEntity<?> findById(@PathVariable("user_id") String id) {
        try {
            User user = this.userService.findById(Long.valueOf(id));
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            UserDto userDto = new UserDto();
            userDto.setEmail(user.getEmail());
            userDto.setFirstName(user.getFirstName());
            userDto.setThemes(new ArrayList<>(user.getThemes()));

            return ResponseEntity.ok().body(userDto);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<?> update(@PathVariable("id") String id, @Valid @RequestBody UserUpdateDto userUpdateDto) {

        User user = new User();
        user.setEmail(userUpdateDto.getEmail());
        user.setFirstName(userUpdateDto.getFirstName());
        user.setPassword(passwordEncoder.encode(userUpdateDto.getPassword()));

        try {
            user = this.userService.update(Long.parseLong(id),user);

            UserDto userDto = new UserDto();
            userDto.setEmail(user.getEmail());
            userDto.setFirstName(user.getFirstName());
            userDto.setThemes(new ArrayList<>(user.getThemes()));


            return ResponseEntity.ok().body(userDto);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("{themeId}/follow/{userId}")
    public ResponseEntity<?> follow(@PathVariable("themeId") String themeId, @PathVariable("userId") String userId) {
        try {
            this.userService.follow(Long.parseLong(themeId), Long.parseLong(userId));

            return ResponseEntity.ok().build();
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("{themeId}/follow/{userId}")
    public ResponseEntity<?> unFollow(@PathVariable("themeId") String themeId, @PathVariable("userId") String userId) {
        try {
            this.userService.unFollow(Long.parseLong(themeId), Long.parseLong(userId));

            return ResponseEntity.ok().build();
        } catch (NumberFormatException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
