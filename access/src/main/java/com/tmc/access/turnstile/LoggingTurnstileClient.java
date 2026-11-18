package com.tmc.access.turnstile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demo stand-in for the turnstile control system: it only logs the request and
 * no gate is opened.
 */
public class LoggingTurnstileClient implements TurnstileClient {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void openGate(Direction direction) {
        logger.info("No turnstile control system configured, gate open request ({}) only logged", direction);
    }
}
