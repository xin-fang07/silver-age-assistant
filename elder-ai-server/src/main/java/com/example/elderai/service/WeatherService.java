package com.example.elderai.service;

import com.example.elderai.dto.WeatherResultDTO;

/**
 * 天气查询服务接口
 * <p>
 * 提供基于 Open-Meteo 免费天气 API 的全球城市天气查询能力。
 * </p>
 */
public interface WeatherService {

    /**
     * 根据城市名查询当前天气与7日预报
     *
     * @param city 城市名称，支持中文/英文
     * @return 天气查询结果
     */
    WeatherResultDTO queryByCity(String city);

    /**
     * 根据经纬度查询当前天气与7日预报
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 天气查询结果
     */
    WeatherResultDTO queryByLocation(Double latitude, Double longitude);
}
