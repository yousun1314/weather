import React, { useState } from 'react';
import axios from 'axios';
import WeatherCard from './components/WeatherCard';
import './App.css';

const API_BASE_URL = process.env.REACT_APP_API_URL || '/api';

function App() {
    const [city, setCity] = useState('');
    const [weather, setWeather] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const fetchWeather = async () => {
        if (!city.trim()) return;

        setLoading(true);
        setError(null);
        setWeather(null);

        try {
            const response = await axios.get(`${API_BASE_URL}/weather`, {
                params: { city: city.trim() }
            });
            setWeather(response.data);
        } catch (err) {
            setError(err.response?.data?.message || '获取天气信息失败，请检查城市名称');
        } finally {
            setLoading(false);
        }
    };

    const handleKeyDown = (e) => {
        if (e.key === 'Enter') {
            fetchWeather();
        }
    };

    return (
        <div className="app">
            <h1 className="title">天气预报</h1>
            <div className="search-box">
                <input
                    type="text"
                    placeholder="输入城市名称..."
                    value={city}
                    onChange={(e) => setCity(e.target.value)}
                    onKeyDown={handleKeyDown}
                />
                <button onClick={fetchWeather} disabled={loading}>
                    {loading ? '查询中...' : '搜索'}
                </button>
            </div>

            {error && <p className="error">{error}</p>}
            {weather && <WeatherCard weather={weather} />}
        </div>
    );
}

export default App;
