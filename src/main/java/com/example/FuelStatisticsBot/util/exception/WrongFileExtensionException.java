package com.example.FuelStatisticsBot.util.exception;

public class WrongFileExtensionException extends RuntimeException{
    public WrongFileExtensionException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongFileExtensionException(String message) {
        super(message);
    }
}
