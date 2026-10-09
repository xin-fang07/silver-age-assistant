package com.example.elderai.controller;

import com.example.elderai.common.Result;
import com.example.elderai.entity.ElderInfo;
import com.example.elderai.mapper.ElderInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 智能餐饮推荐 Controller
 * 提供适老化营养食谱的静态推荐（推荐数据硬编码，符合"静态推荐"定位），
 * 可根据老人健康状态（healthStatus）智能标注推荐等级，体现"智能餐饮"。
 */
@RestController
@RequestMapping("/api/diet")
public class DietController {

    @Autowired
    private ElderInfoMapper elderInfoMapper;

    /**
     * 获取智能餐饮推荐
     * @param elderInfoId 老人档案ID（可选），传入时按健康状态智能标注适合程度
     */
    @GetMapping("/recommend")
    public Result<Map<String, Object>> recommend(@RequestParam(required = false) Long elderInfoId) {
        // 1. 解析老人健康状态 -> 适用病情标签
        String elderName = null;
        String conditionLabel = "健康";
        List<String> conditions = new ArrayList<>();
        if (elderInfoId != null) {
            ElderInfo elder = elderInfoMapper.selectById(elderInfoId);
            if (elder != null) {
                elderName = elder.getRealName();
                Integer hs = elder.getHealthStatus();
                // healthStatus: 0-健康 1-慢性病 2-需照护 3-其他疾病
                if (hs != null) {
                    switch (hs) {
                        case 0: conditionLabel = "健康"; break;
                        case 1: conditionLabel = "慢性病"; conditions.add("慢性病"); break;
                        case 2: conditionLabel = "需照护"; conditions.add("慢性病"); conditions.add("需照护"); break;
                        case 3: conditionLabel = "其他疾病"; conditions.add("其他疾病"); break;
                        default: conditionLabel = "未知";
                    }
                }
            }
        }

        // 2. 静态适老食谱推荐（硬编码演示数据）
        List<Map<String, Object>> list = buildStaticRecommendations();

        // 3. 按健康状态智能标注推荐等级
        for (Map<String, Object> item : list) {
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) item.get("tags");
            boolean match = (tags == null || tags.isEmpty())
                    || conditions.stream().anyMatch(tags::contains);
            item.put("recommended", match);
            item.put("matchLevel", (!match) ? "normal" : (tags != null && !tags.isEmpty()) ? "match" : "normal");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("elderName", elderName);
        result.put("conditionLabel", conditionLabel);
        result.put("list", list);
        return Result.success(result);
    }

    /** 静态适老营养食谱（真实场景可由运营后台维护，这里硬编码演示） */
    private List<Map<String, Object>> buildStaticRecommendations() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(makeItem("燕麦小米粥", "早餐", Arrays.asList("慢性病"),
                "燕麦50g、小米30g、枸杞少许", "低GI、富含膳食纤维，有助于平稳血糖与肠道健康"));
        list.add(makeItem("清蒸鲈鱼配西兰花", "午餐", new ArrayList<>(),
                "鲈鱼1条、西兰花200g、橄榄油5g", "优质蛋白、低盐低脂，适合各年龄段老人"));
        list.add(makeItem("芹菜炒香干", "午餐", Arrays.asList("慢性病"),
                "芹菜150g、香干80g、少盐", "芹菜含芹菜素，有助平稳血压；香干补充植物蛋白"));
        list.add(makeItem("杂粮饭拌木耳", "晚餐", Arrays.asList("慢性病"),
                "糙米、黑米、干木耳适量", "低热量、富含可溶性纤维，利于血脂管理"));
        list.add(makeItem("无糖酸奶配蓝莓", "加餐", new ArrayList<>(),
                "无糖酸奶1杯、蓝莓1小把", "益生菌+抗氧化物，护肠又护眼"));
        list.add(makeItem("蒸南瓜", "加餐", Arrays.asList("慢性病"),
                "南瓜200g", "低糖高纤维、富含β-胡萝卜素，适合需控糖老人"));
        return list;
    }

    private Map<String, Object> makeItem(String name, String mealType, List<String> tags,
                                         String ingredients, String nutrition) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("mealType", mealType);
        m.put("tags", tags);
        m.put("ingredients", ingredients);
        m.put("nutrition", nutrition);
        return m;
    }
}
