package com.tmc.access.turnstile;

/** Asks the turnstile control system to open a gate. */
public interface TurnstileClient {
    /**
     * @throws TurnstileUnavailableException
     *             when the request was not accepted; the gate must be treated as
     *             closed
     */
    void openGate(Direction direction);
}
