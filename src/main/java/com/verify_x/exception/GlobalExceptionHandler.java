package com.verify_x.exception;

import com.verify_x.payload.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);


    // =========================================================
    // DATA INTEGRITY VIOLATION
    // =========================================================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>>
    handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {

        log.error("Data integrity violation", ex);

        ApiResponse<Object> response =
                ApiResponse.builder()
                        .success(false)
                        .message("A record with this data already exists.")
                        .data(null)
                        .timeStamp(LocalDateTime.now())
                        .build();

        return new ResponseEntity<>(
                response,
                HttpStatus.CONFLICT
        );
    }


    // =========================================================
    // RESOURCE NOT FOUND
    // =========================================================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>>
    handleResourceNotFoundException(
            ResourceNotFoundException ex) {

        ApiResponse<Object> response =
                ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .data(null)
                        .timeStamp(LocalDateTime.now())
                        .build();

        return new ResponseEntity<>(
                response,
                HttpStatus.NOT_FOUND
        );
    }


    // =========================================================
    // EMAIL ALREADY EXISTS
    // =========================================================

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>>
    handleEmailAlreadyExistsException(
            EmailAlreadyExistsException ex) {

        ApiResponse<Object> response =
                ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .data(null)
                        .timeStamp(LocalDateTime.now())
                        .build();

        return new ResponseEntity<>(
                response,
                HttpStatus.CONFLICT
        );
    }


    // =========================================================
    // USER ALREADY EXISTS
    // =========================================================

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>>
    handleUserAlreadyExistsException(
            UserAlreadyExistsException ex) {

        ApiResponse<Object> response =
                ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .data(null)
                        .timeStamp(LocalDateTime.now())
                        .build();

        return new ResponseEntity<>(
                response,
                HttpStatus.CONFLICT
        );
    }


    // =========================================================
    // BAD CREDENTIALS
    // =========================================================

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>>
    handleBadCredentialsException(
            BadCredentialsException ex) {

        /*
         * IMPORTANT:
         * Do not hard-code "Invalid email or password".
         *
         * BadCredentialsException is also used during
         * OTP verification.
         *
         * Using ex.getMessage() allows the actual message
         * from AuthServiceImpl to reach the client.
         */

        ApiResponse<Object> response =
                ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .data(null)
                        .timeStamp(LocalDateTime.now())
                        .build();

        return new ResponseEntity<>(
                response,
                HttpStatus.UNAUTHORIZED
        );
    }


    // =========================================================
    // ACCESS DENIED
    // =========================================================

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>>
    handleAccessDeniedException(
            AccessDeniedException ex) {

        ApiResponse<Object> response =
                ApiResponse.builder()
                        .success(false)
                        .message("Access denied: " + ex.getMessage())
                        .data(null)
                        .timeStamp(LocalDateTime.now())
                        .build();

        return new ResponseEntity<>(
                response,
                HttpStatus.FORBIDDEN
        );
    }


    // =========================================================
    // VALIDATION ERROR
    // =========================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>>
    handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors =
                new HashMap<>();

        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {

                    String fieldName =
                            ((FieldError) error).getField();

                    String errorMessage =
                            error.getDefaultMessage();

                    errors.put(
                            fieldName,
                            errorMessage
                    );
                });

        ApiResponse<Map<String, String>> response =
                ApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .message("Validation failed")
                        .data(errors)
                        .timeStamp(LocalDateTime.now())
                        .build();

        return new ResponseEntity<>(
                response,
                HttpStatus.BAD_REQUEST
        );
    }


    // =========================================================
    // BAD REQUEST
    // =========================================================

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Object>>
    handleBadRequestException(
            BadRequestException ex) {

        ApiResponse<Object> response =
                ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .data(null)
                        .timeStamp(LocalDateTime.now())
                        .build();

        return new ResponseEntity<>(
                response,
                HttpStatus.BAD_REQUEST
        );
    }


    // =========================================================
    // RESOURCE ALREADY EXISTS
    // =========================================================

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>>
    handleResourceAlreadyExistsException(
            ResourceAlreadyExistsException ex) {

        ApiResponse<Object> response =
                ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .data(null)
                        .timeStamp(LocalDateTime.now())
                        .build();

        return new ResponseEntity<>(
                response,
                HttpStatus.CONFLICT
        );
    }


    // =========================================================
    // USER NOT FOUND
    // =========================================================

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>>
    handleUserNotFoundException(
            UserNotFoundException ex) {

        ApiResponse<Object> response =
                ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .data(null)
                        .timeStamp(LocalDateTime.now())
                        .build();

        return new ResponseEntity<>(
                response,
                HttpStatus.UNAUTHORIZED
        );
    }


    // =========================================================
    // INVALID VERIFICATION STATE
    // =========================================================

    @ExceptionHandler(InvalidVerificationStateException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleInvalidState(
            InvalidVerificationStateException ex) {

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(
                        ApiResponse.error(
                                ex.getMessage()
                        )
                );
    }


    // =========================================================
    // GLOBAL EXCEPTION
    // =========================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>>
    handleGlobalException(Exception ex) {

        log.error(
                "Unhandled exception",
                ex
        );

        ApiResponse<Object> response =
                ApiResponse.builder()
                        .success(false)
                        .message(
                                "An unexpected error occurred. Please try again later."
                        )
                        .data(null)
                        .timeStamp(LocalDateTime.now())
                        .build();

        return new ResponseEntity<>(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}