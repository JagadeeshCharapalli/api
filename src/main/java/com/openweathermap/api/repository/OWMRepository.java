package com.openweathermap.api.repository;

import com.openweathermap.api.Model.WeatherEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OWMRepository extends JpaRepository<WeatherEntity, Long> {

}
