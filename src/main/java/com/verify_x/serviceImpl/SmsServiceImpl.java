package com.verify_x.serviceImpl;

import com.verify_x.services.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Override
    public void sendOtp(String phoneNumber, String otp) {

        log.info("DEV ONLY — mobile OTP for {} is: {}", phoneNumber, otp);
    }
}