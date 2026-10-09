package com.example.elderai.controller;

import com.example.elderai.common.Result;
import com.example.elderai.dto.WeatherQueryDTO;
import com.example.elderai.dto.WeatherResultDTO;
import com.example.elderai.service.WeatherService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 天气查询控制器
 * <p>
 * 提供全球城市实时天气与7日预报查询接口。
 * 支持按城市名查询，数据来源于 Open-Meteo 免费天气 API。
 * </p>
 */
@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    @Resource
    private WeatherService weatherService;

    /**
     * 根据城市名查询当前天气与7日预报
     *
     * @param dto 查询参数（城市名）
     * @return 天气查询结果
     */
    @GetMapping("/current")
    public Result<WeatherResultDTO> currentByCity(@Valid WeatherQueryDTO dto) {
        WeatherResultDTO result = weatherService.queryByCity(dto.getCity());
        return Result.success(result);
    }

    /**
     * 根据经纬度查询当前天气与7日预报
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 天气查询结果
     */
    @GetMapping("/location")
    public Result<WeatherResultDTO> currentByLocation(@RequestParam Double latitude,
                                                    @RequestParam Double longitude) {
        WeatherResultDTO result = weatherService.queryByLocation(latitude, longitude);
        return Result.success(result);
    }
}
