package com.verify_x.serviceImpl;

import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import com.verify_x.services.SmsService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

//    @Value("${twilio.verify-service-sid}")
//    private String verifyServiceSid;

    @PostConstruct
    public void initializeTwilio() {
        Twilio.init(accountSid, authToken);
    }

    @Override
    public void sendOtp(String phoneNumber, String otp) {

    log.info("DEV ONLY — mobile OTP for {} is: {}", phoneNumber, otp);

    }

//    @Override
//    public boolean verifyOtp(String phoneNumber, String otp) {
//
//        VerificationCheck verificationCheck =
//                VerificationCheck.creator(
//                        verifyServiceSid
//                ).create();
//
//        return "approved".equalsIgnoreCase(
//                verificationCheck.getStatus()
//        );
//    }
}