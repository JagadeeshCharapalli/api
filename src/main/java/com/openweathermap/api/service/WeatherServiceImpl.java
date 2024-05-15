package com.openweathermap.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openweathermap.api.Model.WeatherData;
import com.openweathermap.api.Model.WeatherEntity;
import com.openweathermap.api.repository.OWMRepository;
import com.openweathermap.api.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service("/owmService")
public class WeatherServiceImpl implements WeatherService {

	private WebClient webClient;

	public WeatherServiceImpl() {
		this.webClient = WebClient.create();
	}
	
	private static final Logger Log = LoggerFactory.getLogger(WeatherServiceImpl.class);

	@Autowired
	OWMRepository owmRepository;

	public String getWeatherDetailsByCityandCountryName(String cityName, String countryName, String key){
		ObjectMapper mapper = new ObjectMapper();
		WeatherData weatherData;
		String resourceUrl
				= requestUrlBuilder(Constants.URL_WEATHER, Constants.PARAM_CITY_NAME, cityName, countryName, key);

		String weatherApiResponse = getApiResponse(resourceUrl).block();

		try {
			 weatherData = mapper.readValue(weatherApiResponse, WeatherData.class);
		} catch (Exception ex) {
			throw new RuntimeException("error while processing API Response", ex);
		}

		return saveAndRetrieveResponse(weatherData);
	}

		private String requestUrlBuilder(String type, String param, String cityId,String countryName, String appId) {
			String owmAddressUrl = new StringBuilder()
					.append(Constants.URL_API).append(type).append(param).append(cityId).append(',').append(countryName)
					.append(Constants.PARAM_APP_ID).append(appId).toString();
			Log.info(owmAddressUrl);
			return owmAddressUrl;
		}

	public Mono<String> getApiResponse(String url) {
		return webClient.get()
				.uri(url)
				.retrieve()
				.bodyToMono(String.class)
				.onErrorResume(error -> {
					return Mono.just("Error occurred: " + error.getMessage());
				});
	}

	private String saveAndRetrieveResponse(WeatherData weatherData) {
		String description = weatherData.getWeather().get(0).getDescription();

		WeatherEntity weather = new WeatherEntity();
			weather.setDescription(description);
		owmRepository.save(weather);

		return description;
	}
}
