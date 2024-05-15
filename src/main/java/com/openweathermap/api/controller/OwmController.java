package com.openweathermap.api.controller;

import com.openweathermap.api.service.WeatherService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api")
public class OwmController {

	private static final int MAX_REQUESTS_PER_HOUR = 5;
	private ConcurrentHashMap<String, RateLimiter> rateLimits = new ConcurrentHashMap<>();
	private static final Logger Log = LoggerFactory.getLogger(OwmController.class);
	
	@Autowired
	WeatherService weatherService;

	@GetMapping("/weather/{countryName}/{cityName}&{key}")
	public ResponseEntity<?> getWeatherDetailsByCityName(
			@PathVariable("cityName") String cityName, @PathVariable("countryName") String countryName, @PathVariable("key") String key) {
		Log.info("Fetching weather details with city {} & country name: {}", cityName, countryName);

		if (!rateLimits.containsKey(key)) {
			rateLimits.put(key, new RateLimiter());
		}

		RateLimiter limiter = rateLimits.get(key);

		// Check if the client has reached the maximum allowed requests
		if (limiter.getNumRequests() >= MAX_REQUESTS_PER_HOUR) {
			return ResponseEntity
					.status(429) // 429 Too Many Requests status code
					.body("Maximum requests in an hour exceeded for this key");
		}

		// Increment the request count for this client
		limiter.incrementRequestCount();
		
		String description = weatherService.getWeatherDetailsByCityandCountryName(cityName,countryName, key);
		if (description.isEmpty()) {
			return new ResponseEntity<Object>("No Response from API", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(description, HttpStatus.OK);
	}

	private static class RateLimiter {
		private int numRequests = 0;
		private LocalDateTime lastResetTime = LocalDateTime.now();

		public void incrementRequestCount() {
			LocalDateTime now = LocalDateTime.now();
			if (now.minusHours(1).isAfter(lastResetTime)) {
				// Reset the counter if more than an hour has passed since last reset
				numRequests = 0;
				lastResetTime = now;
			}
			numRequests++;
		}

		public int getNumRequests() {
			return numRequests;
		}
	}
}
