package com.weather.model;

import java.io.Serializable;

public class WeatherResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String city;
    private String description;
    private double temperature;
    private double feelsLike;
    private int humidity;
    private String windSpeed;
    private String icon;

    public WeatherResponse() {}

    public WeatherResponse(String city, String description, double temperature,
                           double feelsLike, int humidity, String windSpeed, String icon) {
        this.city = city;
        this.description = description;
        this.temperature = temperature;
        this.feelsLike = feelsLike;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.icon = icon;
    }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public double getFeelsLike() { return feelsLike; }
    public void setFeelsLike(double feelsLike) { this.feelsLike = feelsLike; }

    public int getHumidity() { return humidity; }
    public void setHumidity(int humidity) { this.humidity = humidity; }

    public String getWindSpeed() { return windSpeed; }
    public void setWindSpeed(String windSpeed) { this.windSpeed = windSpeed; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}
