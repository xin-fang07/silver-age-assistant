package com.example.elderai.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 天气查询结果 DTO
 * <p>
 * 包含当前天气实况、未来7日预报、城市与数据来源等信息。
 * </p>
 */
@Data
public class WeatherResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 城市名称
     */
    private String city;

    /**
     * 国家
     */
    private String country;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 当前温度（摄氏度）
     */
    private Double temperature;

    /**
     * 体感温度或天气描述中附加的温度说明
     */
    private Double apparentTemperature;

    /**
     * 相对湿度（%）
     */
    private Integer humidity;

    /**
     * 风速（km/h）
     */
    private Double windSpeed;

    /**
     * 天气状况文字描述，如"晴"、"多云"、"小雨"
     */
    private String weatherText;

    /**
     * 天气图标编码，前端可用作图标映射
     */
    private String iconCode;

    /**
     * 更新时间，ISO 8601 格式
     */
    private String updateTime;

    /**
     * 7 日天气预报列表
     */
    private List<DailyForecastDTO> dailyForecast;

    /**
     * 数据来源说明
     */
    private String dataSource;
}
