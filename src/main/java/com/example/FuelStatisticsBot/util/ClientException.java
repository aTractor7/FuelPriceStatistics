package com.example.FuelStatisticsBot.util;

public class ClientException extends RuntimeException{
    public ClientException(String massage, Throwable cause) {
        super(massage, cause);
    }

    public ClientException(String massage) {
        super(massage);
    }
}
