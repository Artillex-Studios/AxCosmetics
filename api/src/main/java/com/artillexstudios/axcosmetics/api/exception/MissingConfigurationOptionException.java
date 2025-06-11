package com.artillexstudios.axcosmetics.api.exception;

/**
 * An exception, which is thrown when a configuration option is missing.
 */
public class MissingConfigurationOptionException extends RuntimeException {
    private final String option;

    public MissingConfigurationOptionException(String option) {
        super();
        this.option = option;
    }

    public String option() {
        return this.option;
    }
}
