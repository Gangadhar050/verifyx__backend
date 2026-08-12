package com.verify_x.services;

import com.verify_x.dto.*;

public interface AuthService {

        String register(UserRegistrationDto registrationDto);

        String verifyRegistrationOtp(VerifyOtpRequestDto request);

        LoginResponseDto login(LoginRequestDto loginRequest);

        CurrentUserDto getCurrentUser();

        void logout(String token);

        HrLoginResponseDto adminLogin(AdminLoginRequestDto request);

        void adminLogout(String token);
}