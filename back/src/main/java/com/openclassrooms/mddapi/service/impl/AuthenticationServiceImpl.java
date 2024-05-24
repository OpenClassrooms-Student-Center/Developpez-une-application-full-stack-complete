package com.openclassrooms.mddapi.service.impl;

import com.openclassrooms.mddapi.dto.UpdateProfileRequestDTO;
import com.openclassrooms.mddapi.dto.UserDTO;
import com.openclassrooms.mddapi.excepton.CustomAlreadyExistsException;
import com.openclassrooms.mddapi.excepton.CustomAuthenticationException;
import com.openclassrooms.mddapi.excepton.CustomNotFoundException;
import com.openclassrooms.mddapi.model.DBUser;
import com.openclassrooms.mddapi.model.LoginRequest;
import com.openclassrooms.mddapi.model.RegistrationRequest;
import com.openclassrooms.mddapi.repository.DBUserRepository;
import com.openclassrooms.mddapi.service.AuthenticationService;
import com.openclassrooms.mddapi.service.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Logger logger = Logger.getLogger(AuthenticationServiceImpl.class.getName());

    private final DBUserRepository dbUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationServiceImpl(DBUserRepository dbUserRepository, BCryptPasswordEncoder bCryptPasswordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.dbUserRepository = dbUserRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Map<String, String> registerUserAndGenerateToken(RegistrationRequest registrationRequest) {
        logger.info("Registering user: " + registrationRequest.getEmail());

        if (dbUserRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new CustomAlreadyExistsException("Email already in use.");
        }

        DBUser newUser = new DBUser();
        newUser.setName(registrationRequest.getName());
        newUser.setEmail(registrationRequest.getEmail());
        newUser.setPassword(bCryptPasswordEncoder.encode(registrationRequest.getPassword()));

        dbUserRepository.save(newUser);

        // Generate token for the new user
        String token = jwtService.generateTokenForUser(newUser);
        return Map.of("token", token);
    }

    @Override
    public Map<String, String> authenticateAndGenerateToken(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtService.generateToken(authentication);
            return Collections.singletonMap("token", jwt);
        } catch (AuthenticationException e) {
            logger.severe("Authentication failed for user: " + loginRequest.getEmail());
            throw new CustomAuthenticationException("Invalid username or password");
        }
    }

    @Override
    public String getAuthenticatedUserEmail() {
        Authentication authentication = getAuthentication();
        return (authentication != null) ? authentication.getName() : null;
    }

    @Override
    public String getAuthenticatedUsername() {
        Authentication authentication = getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            }
            return principal.toString();
        }
        return null;
    }

    @Override
    public UserDTO getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return dbUserRepository.findByEmail(email)
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getCreatedAt(),
                        user.getUpdatedAt()))
                .orElseThrow(() -> new CustomNotFoundException("User not found"));
    }

    protected Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private DBUser getCurrentAuthenticatedUser() {
        Authentication authentication = getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            logger.info("Principal class: " + principal.getClass().getName());
            logger.info("Principal: " + principal.toString());

            if (principal instanceof UserDetails) {
                String email = ((UserDetails) principal).getUsername();
                logger.info("Authenticated user email: " + email);
                return dbUserRepository.findByEmail(email).orElse(null);
            } else if (principal instanceof String) {
                String email = principal.toString();
                logger.info("Authenticated user email: " + email);
                return dbUserRepository.findByEmail(email).orElse(null);
            } else if (principal instanceof org.springframework.security.oauth2.jwt.Jwt) {
                org.springframework.security.oauth2.jwt.Jwt jwt = (org.springframework.security.oauth2.jwt.Jwt) principal;
                Map<String, Object> claims = jwt.getClaims();
                logger.info("JWT Claims: " + claims.toString());
                String email = jwt.getClaim("email");
                logger.info("Authenticated user email from JWT: " + email);
                return dbUserRepository.findByEmail(email).orElse(null);
            }
        } else {
            logger.warning("Authentication object is null or principal is not UserDetails");
        }
        return null;
    }




    @Override
    public UserDTO updateUserProfile(UpdateProfileRequestDTO updateRequestDTO) throws CustomNotFoundException {
        DBUser currentUser = getCurrentAuthenticatedUser();
        if (currentUser == null) {
            throw new CustomNotFoundException("User not found.");
        }

        currentUser.setName(updateRequestDTO.getName());
        currentUser.setEmail(updateRequestDTO.getEmail());
        dbUserRepository.save(currentUser);

        return new UserDTO(currentUser.getId(), currentUser.getName(), currentUser.getEmail(), currentUser.getCreatedAt(), currentUser.getUpdatedAt());
    }
}
