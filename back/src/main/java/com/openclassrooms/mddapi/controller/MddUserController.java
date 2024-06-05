package com.openclassrooms.mddapi.controller;

import com.openclassrooms.mddapi.dtos.MddUserDto;
import com.openclassrooms.mddapi.dtos.requests.NewPasswordRequest;
import com.openclassrooms.mddapi.dtos.requests.NewUserInfoRequest;
import com.openclassrooms.mddapi.dtos.responses.MessageResponse;
import com.openclassrooms.mddapi.dtos.responses.UserInfoResponse;
import com.openclassrooms.mddapi.model.MddUser;
import com.openclassrooms.mddapi.mappers.MddUserMapper;
import com.openclassrooms.mddapi.model.Topic;
import com.openclassrooms.mddapi.service.MddUserService;
import com.openclassrooms.mddapi.service.TopicService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("users/user")
public class MddUserController {

    @Autowired
    MddUserService mddUserService;

    @Autowired
    private MddUserMapper mddUserMapper;

    @Autowired
    TopicService topicService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public ResponseEntity<List<MddUserDto>> getUsers() {
        return ResponseEntity.ok(mddUserMapper.toDto(mddUserService.findAllUsers()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MddUserDto> getUserById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(mddUserMapper.toDto(mddUserService.findUserById(id)));
    }

    @PostMapping("/create")
    public ResponseEntity<MddUserDto> create(@RequestBody MddUserDto mddUserDto) {
        return ResponseEntity.ok(mddUserMapper.toDto(mddUserService.createUser(mddUserMapper.toEntity(mddUserDto))));
    }

    @PutMapping("/update")
    public ResponseEntity<MddUserDto> update(@RequestBody MddUserDto mddUserDto) {
        return ResponseEntity.ok(mddUserMapper.toDto(mddUserService.updateUser(mddUserMapper.toEntity(mddUserDto))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> delete(@PathVariable("id") Long id) {
        return ResponseEntity.ok(new MessageResponse(mddUserService.deleteUser(id)));
    }

    @GetMapping("/contactinfo")
    public ResponseEntity<UserInfoResponse> me(Authentication authentication){
        MddUser mddUser = mddUserService.findUserByEmail(authentication.getName());
        UserInfoResponse userInfoResponse = new UserInfoResponse();
        userInfoResponse.setUsername(mddUser.getUsername());
        userInfoResponse.setEmail(mddUser.getEmail());
        userInfoResponse.setTopicsIds(topicService.mySubscriptions(mddUser));
        log.info("Call for myInfo");
        System.out.println("Me info request for :" + authentication + userInfoResponse);
        return ResponseEntity.ok(userInfoResponse);
    }

    @PutMapping("newinfo")
    public ResponseEntity<MessageResponse> newInfo(@RequestBody NewUserInfoRequest newUserInfoRequest, Authentication authentication){
        MddUser mddUser = mddUserService.findUserByEmail(authentication.getName());
        mddUser.setUsername(newUserInfoRequest.getUsername());
        mddUser.setEmail(newUserInfoRequest.getEmail());
        mddUserService.updateUser(mddUser);
        return ResponseEntity.ok(new MessageResponse("User information updated successfully"));
    }

    @PutMapping("newpass")
    public ResponseEntity<MessageResponse> newPassword(@RequestBody NewPasswordRequest newPasswordRequest, Authentication authentication){
        MddUser mddUser = mddUserService.findUserByEmail(authentication.getName());
        mddUser.setPassword(passwordEncoder.encode(newPasswordRequest.getNewPass()));
        mddUserService.updateUser(mddUser);
        return ResponseEntity.ok(new MessageResponse("Password updated successfully"));
    }
}