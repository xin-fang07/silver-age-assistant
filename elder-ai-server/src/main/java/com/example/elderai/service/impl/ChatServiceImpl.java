package com.example.elderai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.elderai.dto.WeatherResultDTO;
import com.example.elderai.entity.ChatRecord;
import com.example.elderai.mapper.ChatRecordMapper;
import com.example.elderai.service.ChatService;
import com.example.elderai.service.NewsService;
import com.example.elderai.service.WeatherService;
import com.example.elderai.utils.DeepSeekClient;
import com.example.elderai.utils.FaqFallbackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI 对话服务实现类
 * <p>
 * 实现用户与 AI 助手的对话功能，包含：
 * <ul>
 *   <li>DeepSeek 大模型调用</li>
 *   <li>FAQ 降级机制</li>
 *   <li>实时天气意图识别：当用户询问天气时，自动调用 WeatherService 获取天气并注入上下文</li>
 * </ul>
 * 所有对话记录都会保存到 chat_record 表。
 * </p>
 *
 * @author elder-ai-team
 */
@Service // 标记为 Spring Service Bean
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    /** DeepSeek AI 大模型客户端 */
    @Autowired
    private DeepSeekClient deepSeekClient;

    /** FAQ 降级回答服务（本地关键词知识库） */
    @Autowired
    private FaqFallbackService faqFallbackService;

    /** 对话记录数据访问层 */
    @Autowired
    private ChatRecordMapper chatRecordMapper;

    /** 天气查询服务 */
    @Autowired
    private WeatherService weatherService;

    /** 新闻查询服务 */
    @Autowired
    private NewsService newsService;

    /** 默认城市（当未识别到城市时） */
    private static final String DEFAULT_CITY = "北京";

    /** 天气相关关键词 */
    private static final Set<String> WEATHER_KEYWORDS = Set.of(
            "天气", "气温", "温度", "下雨", "下雪", "刮风", "风大", "雾霾", "晴天",
            "多云", "阴天", "雷阵雨", "暴雨", "大雪", "小雨", "中雨", "大雨",
            "weather", "rain", "snow", "sunny", "cloudy", "windy", "storm"
    );

    /** 新闻相关关键词 */
    private static final Set<String> NEWS_KEYWORDS = Set.of(
            "新闻", "资讯", "头条", "新闻联播", "新闻联播讲了什么", "最近有什么新闻",
            "国际形势", "国内新闻", "国际新闻", "今天新闻", "昨天新闻",
            "热点", "要闻", "快报", "播报", "新闻资讯"
    );

    /** 常见城市列表（中文优先），按长度降序排列，优先匹配长名称 */
    private static final List<String> CITY_LIST = new ArrayList<>();

    static {
        // 中国主要城市（含港澳台、中国台湾）
        String[] chineseCities = {
                "北京", "上海", "广州", "深圳", "天津", "重庆", "成都", "杭州", "武汉", "西安",
                "南京", "长沙", "郑州", "沈阳", "青岛", "大连", "厦门", "苏州", "昆明", "哈尔滨",
                "济南", "宁波", "无锡", "福州", "合肥", "石家庄", "太原", "南昌", "南宁", "贵阳",
                "兰州", "海口", "乌鲁木齐", "拉萨", "银川", "呼和浩特", "西宁", "长春",
                "香港", "澳门", "台北", "高雄", "台中", "台南", "新竹", "基隆",
                "桂林", "三亚", "珠海", "汕头", "佛山", "东莞", "中山", "惠州", "江门", "湛江",
                "徐州", "常州", "南通", "扬州", "盐城", "淮安", "镇江", "泰州", "宿迁",
                "温州", "嘉兴", "绍兴", "金华", "台州", "湖州", "衢州", "丽水", "舟山",
                "泉州", "漳州", "莆田", "龙岩", "三明", "南平", "宁德",
                "烟台", "潍坊", "临沂", "淄博", "济宁", "泰安", "威海", "日照", "德州", "聊城",
                "洛阳", "开封", "安阳", "新乡", "许昌", "南阳", "商丘", "周口", "驻马店", "信阳",
                "襄阳", "宜昌", "荆州", "十堰", "黄冈", "孝感", "黄石", "荆门", "咸宁", "恩施",
                "岳阳", "常德", "株洲", "湘潭", "衡阳", "邵阳", "郴州", "永州", "怀化", "娄底",
                "惠州", "珠海", "韶关", "湛江", "肇庆", "江门", "茂名", "梅州", "汕尾", "河源",
                "阳江", "清远", "潮州", "揭阳", "云浮",
                "佛山", "顺德", "南海", "番禺", "花都", "增城", "从化"
        };
        // 国际主要城市
        String[] globalCities = {
                "纽约", "伦敦", "东京", "巴黎", "悉尼", "莫斯科", "新加坡", "迪拜", "曼谷", "首尔",
                "柏林", "多伦多", "孟买", "伊斯坦布尔", "洛杉矶", "芝加哥", "旧金山", "温哥华",
                "墨尔本", "罗马", "马德里", "巴塞罗那", "阿姆斯特丹", "苏黎世", "斯德哥尔摩",
                "奥斯陆", "赫尔辛基", "哥本哈根", "维也纳", "布拉格", "华沙", "里斯本", "雅典",
                "开罗", "约翰内斯堡", "内罗毕", "里约热内卢", "布宜诺斯艾利斯", "墨西哥城",
                "利马", "奥克兰", "吉隆坡", "雅加达", "马尼拉", "河内", "胡志明市", "达卡",
                "卡拉奇", "利雅得", "德黑兰", "特拉维夫", "布鲁塞尔", "布达佩斯", "布加勒斯特",
                "索非亚", "萨格勒布", "贝尔格莱德", "都柏林", "爱丁堡", "曼彻斯特", "伯明翰",
                "里昂", "马赛", "米兰", "那不勒斯", "威尼斯", "佛罗伦萨", "都灵", "汉堡", "慕尼黑",
                "法兰克福", "科隆", "斯图加特", "新德里", "加尔各答", "班加罗尔", "海得拉巴",
                "浦那", "钦奈", "伊斯兰堡", "拉合尔", "达累斯萨拉姆", "卡萨布兰卡", "阿尔及尔",
                "突尼斯", "亚历山大", "科威特城", "多哈", "马斯喀特", "阿布扎比", "耶路撒冷",
                "安曼", "贝鲁特", "巴格达", "大马士革", "麦纳麦", "萨那",
                "纽约", "洛杉矶", "芝加哥", "休斯顿", "菲尼克斯", "费城", "圣安东尼奥", "圣迭戈",
                "达拉斯", "圣何塞", "奥斯汀", "杰克逊维尔", "沃思堡", "哥伦布", "夏洛特", "旧金山",
                "印第安纳波利斯", "西雅图", "丹佛", "华盛顿", "波士顿", "底特律", "纳什维尔",
                "波特兰", "俄克拉荷马城", "拉斯维加斯", "路易斯维尔", "巴尔的摩", "密尔沃基",
                "阿尔伯克基", "图森", "弗雷斯诺", "萨克拉门托", "堪萨斯城", "梅萨", "亚特兰大",
                "长滩", "科罗拉多斯普林斯", "罗利", "奥马哈", "迈阿密", "奥克兰", "明尼阿波利斯",
                "塔尔萨", "阿灵顿", "威奇托", "贝克斯菲尔德"
        };

        CITY_LIST.addAll(Arrays.asList(chineseCities));
        CITY_LIST.addAll(Arrays.asList(globalCities));
        // 按长度降序，避免短城市名被长城市名包含时误匹配（如“东京” vs“京”）
        CITY_LIST.sort((a, b) -> Integer.compare(b.length(), a.length()));
    }

    /** 默认保留的上下文轮数（最近5轮对话） */
    private static final int MAX_CONTEXT_ROUNDS = 5;

    // ==================== AI 对话 ====================

    /**
     * AI 对话核心方法（支持多轮上下文）
     * <p>
     * 采用"尝试-降级"模式处理用户提问：
     * 1. 若 conversationId 不为空，查询该会话最近 N 轮历史作为上下文
     * 2. 若用户询问天气，自动查询实时天气并注入上下文
     * 3. 尝试调用 DeepSeek API 获取高质量 AI 回答（带历史上下文）
     * 4. 如果失败但天气数据已获取，直接用天气数据回复
     * 5. 如果失败且非天气问题，使用本地 FAQ 关键词匹配作为降级方案
     * 6. 保存对话记录到数据库，关联 conversationId
     * 7. 返回包含问题、回答、是否降级、时间、会话ID的完整结果
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> chat(Long userId, String question, String city, Long conversationId) {
        LocalDateTime now = LocalDateTime.now();

        // 1. 确定会话ID：为空则生成新会话（使用当前时间戳，简单可靠）
        Long convId = (conversationId != null) ? conversationId : generateConversationId(userId);

        // 2. 查询历史上下文（最近 MAX_CONTEXT_ROUNDS 轮）
        List<DeepSeekClient.ChatHistoryMessage> historyMessages = loadHistoryContext(userId, convId);

        String answer;
        int isFallback;

        // 3. 构建真正发送给 AI 的消息（可能包含天气或新闻上下文）
        String messageToSend = buildMessageWithContext(question, city);

        // === 第一阶段：尝试调用 DeepSeek AI 大模型（带上下文） ===
        try {
            log.info("用户 [{}] 发起 AI 对话，会话ID={}，历史轮数={}",
                    userId, convId, historyMessages.size());
            answer = deepSeekClient.chatWithHistory(historyMessages, messageToSend);
            isFallback = 0;
            log.info("DeepSeek 回答成功，用户: {}", userId);
        } catch (RuntimeException e) {
            // === 第二阶段：DeepSeek 失败，优先检查天气 ===
            log.warn("DeepSeek 调用失败，检查天气降级。用户: {}，异常类型: {}",
                    userId, e.getClass().getSimpleName());

            if (isWeatherQuestion(question)) {
                String weatherAnswer = buildWeatherFallbackAnswer(question, city);
                if (weatherAnswer != null) {
                    answer = weatherAnswer;
                    isFallback = 1;
                    log.info("天气降级回复完成，用户: {}", userId);
                } else {
                    answer = faqFallbackService.getFaqAnswer(question);
                    isFallback = 1;
                    log.info("天气降级失败，转 FAQ。用户: {}", userId);
                }
            } else if (isNewsQuestion(question)) {
                String newsAnswer = buildNewsFallbackAnswer(question);
                if (newsAnswer != null) {
                    answer = newsAnswer;
                    isFallback = 1;
                    log.info("新闻降级回复完成，用户: {}", userId);
                } else {
                    answer = faqFallbackService.getFaqAnswer(question);
                    isFallback = 1;
                    log.info("新闻降级失败，转 FAQ。用户: {}", userId);
                }
            } else {
                answer = faqFallbackService.getFaqAnswer(question);
                isFallback = 1;
                log.info("FAQ 降级回答完成，用户: {}", userId);
            }
        }

        // === 第三阶段：保存对话记录到 chat_record 表 ===
        ChatRecord record = new ChatRecord();
        record.setUserId(userId);
        record.setConversationId(convId);
        record.setQuestion(question);
        record.setAnswer(answer);
        record.setIsFallback(isFallback);
        record.setCreateTime(now);
        chatRecordMapper.insert(record);

        // === 第四阶段：构建返回结果 ===
        Map<String, Object> result = new HashMap<>();
        result.put("question", question);
        result.put("answer", answer);
        result.put("isFallback", isFallback);
        result.put("createTime", now);
        result.put("conversationId", convId);

        return result;
    }

    // ==================== 上下文记忆 ====================

    /**
     * 生成新的会话ID
     * 基于用户ID + 时间戳，保证同用户不同时刻的会话不重复
     */
    private Long generateConversationId(Long userId) {
        return System.currentTimeMillis();
    }

    /**
     * 加载指定会话的历史上下文
     * 查询最近 MAX_CONTEXT_ROUNDS 轮对话，按时间正序排列
     */
    private List<DeepSeekClient.ChatHistoryMessage> loadHistoryContext(Long userId, Long conversationId) {
        if (conversationId == null) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<ChatRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatRecord::getUserId, userId)
               .eq(ChatRecord::getConversationId, conversationId)
               .orderByAsc(ChatRecord::getCreateTime)
               .last("LIMIT " + MAX_CONTEXT_ROUNDS);

        List<ChatRecord> records = chatRecordMapper.selectList(wrapper);
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }

        return records.stream()
                .map(r -> new DeepSeekClient.ChatHistoryMessage(r.getQuestion(), r.getAnswer()))
                .collect(Collectors.toList());
    }

    // ==================== 天气意图识别 ====================

    /**
     * 判断用户问题是否涉及天气
     */
    private boolean isWeatherQuestion(String question) {
        if (question == null || question.isBlank()) {
            return false;
        }
        String lower = question.toLowerCase();
        for (String keyword : WEATHER_KEYWORDS) {
            if (lower.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断用户问题是否涉及新闻
     */
    private boolean isNewsQuestion(String question) {
        if (question == null || question.isBlank()) {
            return false;
        }
        String lower = question.toLowerCase();
        for (String keyword : NEWS_KEYWORDS) {
            if (lower.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从问题中提取城市名，未提取到则返回默认城市
     */
    private String extractCity(String question, String cityParam) {
        // 优先使用前端显式传入的城市
        if (cityParam != null && !cityParam.isBlank()) {
            return cityParam.trim();
        }
        if (question == null || question.isBlank()) {
            return DEFAULT_CITY;
        }
        String lower = question.toLowerCase();
        for (String city : CITY_LIST) {
            if (lower.contains(city.toLowerCase())) {
                return city;
            }
        }
        return DEFAULT_CITY;
    }

    /**
     * 构建带上下文的 AI 消息
     * <p>
     * 如果问题是天气相关，则查询天气并将结果注入到用户问题前；
     * 如果问题是新闻相关，则查询新闻并将结果注入到用户问题前；
     * 否则直接返回原问题。
     * </p>
     */
    private String buildMessageWithContext(String question, String cityParam) {
        String message = question;

        if (isWeatherQuestion(question)) {
            message = buildWeatherContext(question, cityParam);
        }

        if (isNewsQuestion(question)) {
            message = buildNewsContext(question);
        }

        return message;
    }

    /**
     * 构建天气上下文
     */
    private String buildWeatherContext(String question, String cityParam) {
        String city = extractCity(question, cityParam);
        try {
            WeatherResultDTO weather = weatherService.queryByCity(city);
            StringBuilder sb = new StringBuilder();
            sb.append("【实时天气信息，请据此回答用户问题】\n");
            sb.append("城市：").append(weather.getCity()).append("\n");
            sb.append("当前天气：").append(weather.getWeatherText()).append("\n");
            sb.append("当前温度：").append(weather.getTemperature()).append("°C\n");
            if (weather.getApparentTemperature() != null) {
                sb.append("体感温度：").append(weather.getApparentTemperature()).append("°C\n");
            }
            sb.append("相对湿度：").append(weather.getHumidity()).append("%\n");
            sb.append("风速：").append(weather.getWindSpeed()).append("km/h\n");
            sb.append("数据来源：").append(weather.getDataSource()).append("\n");
            sb.append("更新时间：").append(weather.getUpdateTime()).append("\n\n");
            sb.append("用户原始问题：").append(question);

            log.info("天气上下文注入成功");
            return sb.toString();
        } catch (Exception e) {
            log.warn("天气查询失败，继续以原问题调用 AI。异常类型: {}",
                    e.getClass().getSimpleName());
            return question;
        }
    }

    /**
     * 构建新闻上下文
     */
    private String buildNewsContext(String question) {
        try {
            String category = extractNewsCategory(question);
            com.example.elderai.dto.NewsResultDTO news = newsService.getLatestNews(category);

            StringBuilder sb = new StringBuilder();
            sb.append("【最新新闻资讯，请据此回答用户问题】\n");
            sb.append("更新时间：").append(news.getPublishTime()).append("\n");
            sb.append("数据来源：").append(news.getSource()).append("\n\n");

            if (news.getArticles() != null && !news.getArticles().isEmpty()) {
                int count = Math.min(news.getArticles().size(), 5);
                for (int i = 0; i < count; i++) {
                    com.example.elderai.dto.NewsResultDTO.NewsArticle article = news.getArticles().get(i);
                    sb.append(i + 1).append(". ").append(article.getTitle()).append("\n");
                    if (article.getDescription() != null) {
                        sb.append("   ").append(article.getDescription()).append("\n");
                    }
                    sb.append("   来源：").append(article.getSource()).append("\n\n");
                }
            }

            sb.append("用户原始问题：").append(question);

            log.info("新闻上下文注入成功");
            return sb.toString();
        } catch (Exception e) {
            log.warn("新闻查询失败，继续以原问题调用 AI。异常类型: {}",
                    e.getClass().getSimpleName());
            return question;
        }
    }

    /**
     * 从问题中提取新闻分类
     */
    private String extractNewsCategory(String question) {
        if (question == null || question.isBlank()) {
            return null;
        }
        String lower = question.toLowerCase();
        if (lower.contains("健康") || lower.contains("养生")) {
            return "健康";
        } else if (lower.contains("科技") || lower.contains("人工智能")) {
            return "科技";
        } else if (lower.contains("体育") || lower.contains("运动")) {
            return "体育";
        } else if (lower.contains("娱乐")) {
            return "娱乐";
        }
        return null;
    }

    /**
     * 新闻降级回复：当 DeepSeek 不可用时，直接用新闻数据生成回复
     */
    private String buildNewsFallbackAnswer(String question) {
        try {
            String category = extractNewsCategory(question);
            com.example.elderai.dto.NewsResultDTO news = newsService.getLatestNews(category);

            StringBuilder sb = new StringBuilder();
            sb.append("以下是最新新闻资讯：\n\n");

            if (news.getArticles() != null && !news.getArticles().isEmpty()) {
                int count = Math.min(news.getArticles().size(), 5);
                for (int i = 0; i < count; i++) {
                    com.example.elderai.dto.NewsResultDTO.NewsArticle article = news.getArticles().get(i);
                    sb.append(i + 1).append(". ").append(article.getTitle()).append("\n");
                    if (article.getDescription() != null) {
                        sb.append("   ").append(article.getDescription()).append("\n");
                    }
                    sb.append("   来源：").append(article.getSource()).append("\n\n");
                }
            }

            sb.append("（数据更新时间：").append(news.getPublishTime())
              .append("，来源：").append(news.getSource()).append("）");

            return sb.toString();
        } catch (Exception e) {
            log.warn("新闻降级回复失败，异常类型: {}", e.getClass().getSimpleName());
            return null;
        }
    }

    /**
     * 天气降级回复：当 DeepSeek 不可用时，直接用天气数据生成回复
     * <p>
     * 调用 WeatherService 获取实时数据，格式化为易读的自然语言回答。
     * 如果天气查询也失败，返回 null 让上层转 FAQ。
     * </p>
     */
    private String buildWeatherFallbackAnswer(String question, String cityParam) {
        String city = extractCity(question, cityParam);
        try {
            WeatherResultDTO weather = weatherService.queryByCity(city);

            StringBuilder sb = new StringBuilder();
            sb.append(city).append("当前天气：").append(weather.getWeatherText()).append("。\n");
            sb.append("温度 ").append(String.format("%.0f", weather.getTemperature())).append("°C");
            if (weather.getApparentTemperature() != null) {
                sb.append("，体感 ").append(String.format("%.0f", weather.getApparentTemperature())).append("°C");
            }
            sb.append("。\n");
            sb.append("湿度 ").append(weather.getHumidity()).append("%");
            sb.append("，风速 ").append(String.format("%.0f", weather.getWindSpeed())).append("km/h。\n\n");

            // 根据天气给出生活建议
            String weatherText = weather.getWeatherText();
            double temp = weather.getTemperature();
            if (weatherText.contains("雨")) {
                sb.append("温馨提示：今天有雨，出门记得带伞，路面湿滑注意防滑。");
            } else if (weatherText.contains("雪")) {
                sb.append("温馨提示：今天下雪，出门注意保暖防滑，建议减少外出。");
            } else if (temp > 35) {
                sb.append("温馨提示：今天很热，注意防暑降温，多喝水，避免中午外出。");
            } else if (temp < 5) {
                sb.append("温馨提示：今天很冷，出门多穿衣服，注意保暖防感冒。");
            } else if (temp < 15) {
                sb.append("温馨提示：天气偏凉，建议穿件外套，早晚温差大注意添衣。");
            } else if (weatherText.contains("晴")) {
                sb.append("天气不错，适合出门散步晒太阳，祝您心情愉快！");
            }

            sb.append("\n（天气数据来源：").append(weather.getDataSource())
              .append("，更新于").append(weather.getUpdateTime()).append("）");

            return sb.toString();
        } catch (Exception e) {
            log.warn("天气降级回复失败，异常类型: {}", e.getClass().getSimpleName());
            return null; // 返回 null 让上层转 FAQ
        }
    }
}
