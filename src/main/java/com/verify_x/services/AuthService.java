package com.verify_x.services;

import com.verify_x.dto.*;

public interface AuthService {


        String register(UserRegistrationDto registrationDto);

        LoginResponseDto login(LoginRequestDto loginRequest);

        CurrentUserDto getCurrentUser();

        void logout(String token);

        HrLoginResponseDto adminLogin(AdminLoginRequestDto request);

        void adminLogout(String token);

        String verifyRegistrationOtp(VerifyOtpRequestDto request);

//                void sendOtp(String email, String otp);
//
//        void sendOtp(String phoneNumber, String otp);
    }


