package com.openweathermap.api.service;

public interface WeatherService {

	String getWeatherDetailsByCityandCountryName(String cityName, String countryName, String key);
}
