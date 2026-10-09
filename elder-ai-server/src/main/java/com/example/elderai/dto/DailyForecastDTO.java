package com.example.elderai.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 每日天气预报 DTO
 */
@Data
public class DailyForecastDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日期，ISO 8601 格式（如 2026-07-15）
     */
    private String date;

    /**
     * 星期几，中文（如 周一、周二）
     */
    private String weekday;

    /**
     * 最高温度（摄氏度）
     */
    private Double maxTemp;

    /**
     * 最低温度（摄氏度）
     */
    private Double minTemp;

    /**
     * 天气状况文字描述
     */
    private String weatherText;

    /**
     * 天气图标编码
     */
    private String iconCode;
}
