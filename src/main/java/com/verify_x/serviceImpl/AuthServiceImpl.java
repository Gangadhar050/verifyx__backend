package com.verify_x.serviceImpl;

import com.verify_x.dto.*;
import com.verify_x.entity.Admin;
import com.verify_x.entity.Candidate;
import com.verify_x.enums.Role;
import com.verify_x.exception.EmailAlreadyExistsException;
import com.verify_x.exception.UserAlreadyExistsException;
import com.verify_x.jwt.JwtService;
import com.verify_x.jwt.TokenBlacklist;
import com.verify_x.jwt.UserPrincipal;
import com.verify_x.repository.AdminRepository;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.services.AuthService;
//import com.verify_x.services.UserProfileService;
import com.verify_x.services.CandidateService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthServiceImpl implements AuthService {


    private final CandidateRepository candidateRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CandidateService candidateService;
    private final AdminRepository adminRepository;
    private final TokenBlacklist tokenBlacklist;

    @Override
    public String register(UserRegistrationDto dto) {

        log.info("Registering user : {}", dto.getEmail());

        if (candidateRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists.");
        }

        if (candidateRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new UserAlreadyExistsException("Phone number already exists.");
        }


        Candidate user = Candidate.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .password(passwordEncoder.encode(dto.getPassword()))
                .appliedRole(dto.getAppliedRole())
                .candidateType(dto.getCandidateType())
                .role(Role.CANDIDATE)
                .build();

        Candidate savedUser = candidateRepository.save(user);

          candidateService.saveCandidateProfile(savedUser);
        log.info("User registered successfully : {}", savedUser.getId());

        return "Registration Successful";
    }

    @Override
    public LoginResponseDto login(LoginRequestDto dto) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.getEmail(),
                        dto.getPassword()
                )
        );

        Candidate user = candidateRepository.findByEmail(dto.getEmail())
                .orElseThrow();

        String token = jwtService.generateToken(user);

        return LoginResponseDto.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .candidateType(user.getCandidateType())
                .build();
    }

    @Override
    public CurrentUserDto getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadCredentialsException("User is not authenticated");
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        if (principal == null) {
            throw new UsernameNotFoundException("User not found");
        }

        Long userId = principal.getUserId();

        Candidate user = candidateRepository.findById(userId)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        return CurrentUserDto.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    public HrLoginResponseDto adminLogin(AdminLoginRequestDto request) {
        Admin admin = adminRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        if (admin.getRole() != Role.ADMIN) {
            throw new BadCredentialsException("Access denied");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String token = jwtService.generateToken(admin);

        return HrLoginResponseDto.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .email(admin.getEmail())
                .role(admin.getRole())
                .build();
    }
    @Override
    public void adminLogout(String token) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token is required.");
        }

        tokenBlacklist.blacklistToken(token);

        SecurityContextHolder.clearContext();

        log.info("Admin logged out successfully.");
    }
    @Override
    public void logout(String token) {

        tokenBlacklist.blacklistToken(token);

        SecurityContextHolder.clearContext();

        log.info("User logged out successfully.");
    }



}