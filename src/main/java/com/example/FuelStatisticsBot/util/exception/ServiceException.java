package com.example.FuelStatisticsBot.util.exception;

public class ServiceException extends RuntimeException{
    public ServiceException(String massage, Throwable cause) {
        super(massage, cause);
    }

    public ServiceException(String massage) {
        super(massage);
    }
}
