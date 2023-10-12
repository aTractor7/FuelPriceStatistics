package com.example.FuelStatisticsBot.model;


public class Fuel {
    private FuelType fuelType;

    private int price;

    public Fuel(FuelType fuelType, int price) {
        this.fuelType = fuelType;
        this.price = price;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Fuel{" +
                "fuelType=" + fuelType +
                ", price=" + price +
                '}';
    }
}
