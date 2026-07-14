import React from 'react';

// 天气图标映射
const weatherIcons = {
    'sunny': '☀️',
    'cloudy': '☁️',
    'overcast': '☁️',
    'light-rain': '🌦️',
    'moderate-rain': '🌧️',
    'heavy-rain': '🌧️',
    'thunderstorm': '⛈️',
    'light-snow': '🌨️',
    'moderate-snow': '❄️',
    'heavy-snow': '❄️',
    'fog': '🌫️',
};

function WeatherCard({ weather }) {
    const icon = weatherIcons[weather.icon] || '🌤️';

    return (
        <div className="weather-card">
            <h2>{weather.city}</h2>
            <div className="weather-icon-large">{icon}</div>
            <p className="temperature">{weather.temperature}°C</p>
            <p className="description">{weather.description}</p>
            <div className="details">
                <div className="detail-item">
                    <span>体感温度</span>
                    <span>{weather.feelsLike}°C</span>
                </div>
                <div className="detail-item">
                    <span>湿度</span>
                    <span>{weather.humidity}%</span>
                </div>
                <div className="detail-item">
                    <span>风力</span>
                    <span>{weather.windSpeed}</span>
                </div>
            </div>
        </div>
    );
}

export default WeatherCard;
