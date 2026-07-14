package com.weather.service;

import com.weather.model.WeatherRecord;
import com.weather.model.WeatherResponse;
import com.weather.repository.WeatherRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);
    private static final String CACHE_KEY_PREFIX = "weather:";

    private final WebClient webClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final WeatherRecordRepository recordRepository;

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.cache.ttl:600}")
    private long cacheTtl;

    public WeatherService(WebClient.Builder webClientBuilder,
                          RedisTemplate<String, Object> redisTemplate,
                          WeatherRecordRepository recordRepository) {
        this.webClient = webClientBuilder.baseUrl("https://restapi.amap.com").build();
        this.redisTemplate = redisTemplate;
        this.recordRepository = recordRepository;
    }

    public WeatherResponse getWeather(String city) {
        String cacheKey = CACHE_KEY_PREFIX + city;

        // 1. 先查 Redis 缓存
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof WeatherResponse response) {
            log.info("从 Redis 缓存获取城市 [{}] 的天气数据", city);
            return response;
        }

        // 2. 缓存未命中，调用高德 API
        log.info("缓存未命中，调用高德 API 查询城市 [{}] 天气", city);
        String adcode = getAdcode(city);

        var weatherResponse = webClient.get()
                .uri(uri -> uri.path("/v3/weather/weatherInfo")
                        .queryParam("city", adcode)
                        .queryParam("extensions", "base")
                        .queryParam("output", "JSON")
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(AmapWeatherResponse.class)
                .block();

        if (weatherResponse == null || !"1".equals(weatherResponse.status())
                || weatherResponse.lives() == null || weatherResponse.lives().isEmpty()) {
            throw new RuntimeException("获取天气信息失败");
        }

        Live live = weatherResponse.lives().get(0);
        double temperature = parseDoubleSafe(live.temperature());
        int humidity = parseIntSafe(live.humidity());

        WeatherResponse response = new WeatherResponse(
                live.city(),
                live.weather(),
                temperature,
                temperature,
                humidity,
                live.windpower() + "级",
                getWeatherIcon(live.weather())
        );

        // 3. 写入 Redis 缓存（10分钟）
        redisTemplate.opsForValue().set(cacheKey, response, cacheTtl, TimeUnit.SECONDS);
        log.info("城市 [{}] 天气已缓存到 Redis，TTL={}秒", city, cacheTtl);

        // 4. 持久化到 MySQL
        try {
            WeatherRecord record = new WeatherRecord(
                    response.getCity(),
                    response.getDescription(),
                    response.getTemperature(),
                    response.getFeelsLike(),
                    response.getHumidity(),
                    response.getWindSpeed(),
                    response.getIcon(),
                    LocalDateTime.now()
            );
            recordRepository.save(record);
            log.info("城市 [{}] 天气查询记录已保存到 MySQL", city);
        } catch (Exception e) {
            log.error("保存查询记录到 MySQL 失败: {}", e.getMessage());
        }

        return response;
    }

    private String getAdcode(String city) {
        var geoResponse = webClient.get()
                .uri(uri -> uri.path("/v3/geocode/geo")
                        .queryParam("address", city)
                        .queryParam("output", "JSON")
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(AmapGeoResponse.class)
                .block();

        if (geoResponse == null || !"1".equals(geoResponse.status())
                || geoResponse.geocodes() == null || geoResponse.geocodes().isEmpty()) {
            throw new RuntimeException("未找到该城市: " + city);
        }

        return geoResponse.geocodes().get(0).adcode();
    }

    private double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String getWeatherIcon(String weather) {
        if (weather == null) return "";
        return switch (weather) {
            case "晴" -> "sunny";
            case "多云" -> "cloudy";
            case "阴" -> "overcast";
            case "小雨", "阵雨" -> "light-rain";
            case "中雨" -> "moderate-rain";
            case "大雨" -> "heavy-rain";
            case "雷阵雨" -> "thunderstorm";
            case "小雪" -> "light-snow";
            case "中雪" -> "moderate-snow";
            case "大雪" -> "heavy-snow";
            case "雾", "霾" -> "fog";
            default -> "cloudy";
        };
    }

    // 高德 API 响应 record
    private record AmapGeoResponse(String status, java.util.List<Geocode> geocodes) {}
    private record Geocode(String adcode) {}

    private record AmapWeatherResponse(String status, java.util.List<Live> lives) {}
    private record Live(String city, String weather, String temperature,
                        String winddirection, String windpower, String humidity) {}
}
