package com.tmc.access.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.tmc.access.decision.AccessRejectedException;
import com.tmc.access.turnstile.TurnstileUnavailableException;
import com.tmc.access.user.UserNotActiveException;

/** Maps the service's own exceptions to HTTP statuses in one place. */
@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(AccessRejectedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String accessRejected(AccessRejectedException exception) {
        LOGGER.info("Access rejected: {}", exception.getMessage());
        return exception.getMessage();
    }

    @ExceptionHandler(UserNotActiveException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String userNotActive(UserNotActiveException exception) {
        LOGGER.info("QR code not issued: {}", exception.getMessage());
        return exception.getMessage();
    }

    @ExceptionHandler(TurnstileUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String turnstileUnavailable(TurnstileUnavailableException exception) {
        LOGGER.error("Gate could not be opened, the scan was rolled back", exception);
        return "Gate could not be opened";
    }
}
