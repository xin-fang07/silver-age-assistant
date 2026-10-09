package com.example.elderai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 天气查询请求 DTO
 * <p>
 * 支持按城市名查询全球实时天气，城市名可为中文或英文。
 * </p>
 */
@Data
public class WeatherQueryDTO {

    /**
     * 城市名称，必填
     * <p>
     * 例如：北京、上海、New York、Tokyo
     * </p>
     */
    @NotBlank(message = "城市名称不能为空")
    private String city;
}
