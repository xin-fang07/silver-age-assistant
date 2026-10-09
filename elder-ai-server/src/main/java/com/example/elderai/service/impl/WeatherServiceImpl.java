package com.example.elderai.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.elderai.common.BusinessException;
import com.example.elderai.dto.DailyForecastDTO;
import com.example.elderai.dto.WeatherResultDTO;
import com.example.elderai.service.WeatherService;
import com.example.elderai.service.RedisCacheService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 天气查询服务实现
 * <p>
 * 基于 Open-Meteo 免费天气 API（无需 API Key，支持全球城市）：
 * <ul>
 *   <li>地理编码：将城市名转换为经纬度</li>
 *   <li>天气预报：获取当前实况与7日预报</li>
 * </ul>
 * </p>
 */
@Service
public class WeatherServiceImpl implements WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherServiceImpl.class);

    @Autowired
    private RedisCacheService redisCacheService;

    /**
     * Open-Meteo 地理编码 API
     */
    private static final String GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search";

    /**
     * Open-Meteo 天气预报 API
     */
    private static final String FORECAST_URL = "https://api.open-meteo.com/v1/forecast";

    /**
     * 常用城市内置坐标（城市名 -> 经纬度），作为地理编码 API 的兜底。
     * 当 geocoding-api.open-meteo.com 不可达时，仍可查询这些城市的天气。
     */
    private static final Map<String, GeoLocation> BUILTIN_CITIES = new LinkedHashMap<>();
    static {
        // 中国主要城市
        BUILTIN_CITIES.put("北京", loc(39.9042, 116.4074, "北京", "中国"));
        BUILTIN_CITIES.put("上海", loc(31.2304, 121.4737, "上海", "中国"));
        BUILTIN_CITIES.put("广州", loc(23.1291, 113.2644, "广州", "中国"));
        BUILTIN_CITIES.put("深圳", loc(22.5431, 114.0579, "深圳", "中国"));
        BUILTIN_CITIES.put("天津", loc(39.3434, 117.3616, "天津", "中国"));
        BUILTIN_CITIES.put("重庆", loc(29.5630, 106.5516, "重庆", "中国"));
        BUILTIN_CITIES.put("成都", loc(30.5728, 104.0668, "成都", "中国"));
        BUILTIN_CITIES.put("杭州", loc(30.2741, 120.1551, "杭州", "中国"));
        BUILTIN_CITIES.put("武汉", loc(30.5928, 114.3055, "武汉", "中国"));
        BUILTIN_CITIES.put("西安", loc(34.3416, 108.9398, "西安", "中国"));
        BUILTIN_CITIES.put("南京", loc(32.0603, 118.7969, "南京", "中国"));
        BUILTIN_CITIES.put("长沙", loc(28.2282, 112.9388, "长沙", "中国"));
        BUILTIN_CITIES.put("郑州", loc(34.7466, 113.6254, "郑州", "中国"));
        BUILTIN_CITIES.put("沈阳", loc(41.8057, 123.4315, "沈阳", "中国"));
        BUILTIN_CITIES.put("青岛", loc(36.0671, 120.3826, "青岛", "中国"));
        BUILTIN_CITIES.put("大连", loc(38.9140, 121.6147, "大连", "中国"));
        BUILTIN_CITIES.put("厦门", loc(24.4798, 118.0894, "厦门", "中国"));
        BUILTIN_CITIES.put("苏州", loc(31.2989, 120.5853, "苏州", "中国"));
        BUILTIN_CITIES.put("昆明", loc(24.8801, 102.8329, "昆明", "中国"));
        BUILTIN_CITIES.put("哈尔滨", loc(45.8038, 126.5350, "哈尔滨", "中国"));
        BUILTIN_CITIES.put("济南", loc(36.6512, 117.1201, "济南", "中国"));
        BUILTIN_CITIES.put("福州", loc(26.0745, 119.2965, "福州", "中国"));
        BUILTIN_CITIES.put("合肥", loc(31.8206, 117.2272, "合肥", "中国"));
        BUILTIN_CITIES.put("南宁", loc(22.8170, 108.3665, "南宁", "中国"));
        BUILTIN_CITIES.put("贵阳", loc(26.6477, 106.6302, "贵阳", "中国"));
        BUILTIN_CITIES.put("兰州", loc(36.0611, 103.8343, "兰州", "中国"));
        BUILTIN_CITIES.put("海口", loc(20.0440, 110.1999, "海口", "中国"));
        BUILTIN_CITIES.put("乌鲁木齐", loc(43.8256, 87.6168, "乌鲁木齐", "中国"));
        BUILTIN_CITIES.put("拉萨", loc(29.6520, 91.1721, "拉萨", "中国"));
        BUILTIN_CITIES.put("呼和浩特", loc(40.8424, 111.7492, "呼和浩特", "中国"));
        BUILTIN_CITIES.put("香港", loc(22.3193, 114.1694, "香港", "中国"));
        BUILTIN_CITIES.put("澳门", loc(22.1987, 113.5439, "澳门", "中国"));
        BUILTIN_CITIES.put("台北", loc(25.0330, 121.5654, "台北", "中国台湾"));
        // 国际主要城市
        BUILTIN_CITIES.put("纽约", loc(40.7128, -74.0060, "纽约", "美国"));
        BUILTIN_CITIES.put("伦敦", loc(51.5074, -0.1278, "伦敦", "英国"));
        BUILTIN_CITIES.put("东京", loc(35.6762, 139.6503, "东京", "日本"));
        BUILTIN_CITIES.put("巴黎", loc(48.8566, 2.3522, "巴黎", "法国"));
        BUILTIN_CITIES.put("悉尼", loc(-33.8688, 151.2093, "悉尼", "澳大利亚"));
        BUILTIN_CITIES.put("新加坡", loc(1.3521, 103.8198, "新加坡", "新加坡"));
        BUILTIN_CITIES.put("首尔", loc(37.5665, 126.9780, "首尔", "韩国"));
        BUILTIN_CITIES.put("曼谷", loc(13.7563, 100.5018, "曼谷", "泰国"));
        BUILTIN_CITIES.put("莫斯科", loc(55.7558, 37.6173, "莫斯科", "俄罗斯"));
        BUILTIN_CITIES.put("迪拜", loc(25.2048, 55.2708, "迪拜", "阿联酋"));
        BUILTIN_CITIES.put("洛杉矶", loc(34.0522, -118.2437, "洛杉矶", "美国"));
        BUILTIN_CITIES.put("柏林", loc(52.5200, 13.4050, "柏林", "德国"));
        BUILTIN_CITIES.put("多伦多", loc(43.6532, -79.3832, "多伦多", "加拿大"));
        BUILTIN_CITIES.put("石家庄", loc(39.0428, 114.5149, "石家庄", "中国"));
        BUILTIN_CITIES.put("唐山", loc(39.63, 118.18, "唐山", "中国"));
        BUILTIN_CITIES.put("秦皇岛", loc(39.935, 119.6, "秦皇岛", "中国"));
        BUILTIN_CITIES.put("邯郸", loc(36.625, 114.539, "邯郸", "中国"));
        BUILTIN_CITIES.put("保定", loc(38.867, 115.464, "保定", "中国"));
        BUILTIN_CITIES.put("张家口", loc(40.824, 114.888, "张家口", "中国"));
        BUILTIN_CITIES.put("承德", loc(40.987, 117.962, "承德", "中国"));
        BUILTIN_CITIES.put("沧州", loc(38.304, 116.838, "沧州", "中国"));
        BUILTIN_CITIES.put("廊坊", loc(39.518, 116.683, "廊坊", "中国"));
        BUILTIN_CITIES.put("衡水", loc(37.735, 115.665, "衡水", "中国"));
        BUILTIN_CITIES.put("太原", loc(37.87, 112.548, "太原", "中国"));
        BUILTIN_CITIES.put("大同", loc(40.076, 113.3, "大同", "中国"));
        BUILTIN_CITIES.put("临汾", loc(36.088, 111.518, "临汾", "中国"));
        BUILTIN_CITIES.put("包头", loc(40.657, 109.84, "包头", "中国"));
        BUILTIN_CITIES.put("赤峰", loc(42.257, 118.922, "赤峰", "中国"));
        BUILTIN_CITIES.put("长春", loc(43.817, 125.323, "长春", "中国"));
        BUILTIN_CITIES.put("南昌", loc(28.682, 115.857, "南昌", "中国"));
        BUILTIN_CITIES.put("景德镇", loc(29.272, 117.179, "景德镇", "中国"));
        BUILTIN_CITIES.put("萍乡", loc(27.622, 113.852, "萍乡", "中国"));
        BUILTIN_CITIES.put("九江", loc(29.705, 116.002, "九江", "中国"));
        BUILTIN_CITIES.put("新余", loc(27.816, 114.929, "新余", "中国"));
        BUILTIN_CITIES.put("赣州", loc(25.831, 114.933, "赣州", "中国"));
        BUILTIN_CITIES.put("吉安", loc(27.112, 114.986, "吉安", "中国"));
        BUILTIN_CITIES.put("宜春", loc(27.812, 114.38, "宜春", "中国"));
        BUILTIN_CITIES.put("上饶", loc(28.454, 117.944, "上饶", "中国"));
        BUILTIN_CITIES.put("淄博", loc(36.81, 118.054, "淄博", "中国"));
        BUILTIN_CITIES.put("枣庄", loc(34.862, 117.323, "枣庄", "中国"));
        BUILTIN_CITIES.put("东营", loc(37.434, 118.675, "东营", "中国"));
        BUILTIN_CITIES.put("烟台", loc(37.463, 121.448, "烟台", "中国"));
        BUILTIN_CITIES.put("潍坊", loc(36.706, 119.16, "潍坊", "中国"));
        BUILTIN_CITIES.put("济宁", loc(35.415, 116.586, "济宁", "中国"));
        BUILTIN_CITIES.put("泰安", loc(36.201, 117.097, "泰安", "中国"));
        BUILTIN_CITIES.put("威海", loc(37.51, 122.118, "威海", "中国"));
        BUILTIN_CITIES.put("临沂", loc(35.103, 118.355, "临沂", "中国"));
        BUILTIN_CITIES.put("德州", loc(37.435, 116.361, "德州", "中国"));
        BUILTIN_CITIES.put("聊城", loc(36.456, 115.985, "聊城", "中国"));
        BUILTIN_CITIES.put("菏泽", loc(35.233, 115.481, "菏泽", "中国"));
        BUILTIN_CITIES.put("开封", loc(34.797, 114.307, "开封", "中国"));
        BUILTIN_CITIES.put("洛阳", loc(34.619, 112.454, "洛阳", "中国"));
        BUILTIN_CITIES.put("平顶山", loc(33.739, 113.307, "平顶山", "中国"));
        BUILTIN_CITIES.put("安阳", loc(36.098, 114.392, "安阳", "中国"));
        BUILTIN_CITIES.put("新乡", loc(35.303, 113.926, "新乡", "中国"));
        BUILTIN_CITIES.put("焦作", loc(35.24, 113.242, "焦作", "中国"));
        BUILTIN_CITIES.put("南阳", loc(32.993, 112.528, "南阳", "中国"));
        BUILTIN_CITIES.put("信阳", loc(32.148, 114.091, "信阳", "中国"));
        BUILTIN_CITIES.put("周口", loc(33.631, 114.697, "周口", "中国"));
        BUILTIN_CITIES.put("黄石", loc(30.201, 115.035, "黄石", "中国"));
        BUILTIN_CITIES.put("十堰", loc(32.629, 110.787, "十堰", "中国"));
        BUILTIN_CITIES.put("宜昌", loc(30.691, 111.287, "宜昌", "中国"));
        BUILTIN_CITIES.put("襄阳", loc(32.011, 112.122, "襄阳", "中国"));
        BUILTIN_CITIES.put("荆州", loc(30.35, 112.238, "荆州", "中国"));
        BUILTIN_CITIES.put("黄冈", loc(30.446, 114.879, "黄冈", "中国"));
        BUILTIN_CITIES.put("株洲", loc(27.832, 113.134, "株洲", "中国"));
        BUILTIN_CITIES.put("湘潭", loc(27.829, 112.944, "湘潭", "中国"));
        BUILTIN_CITIES.put("衡阳", loc(26.89, 112.571, "衡阳", "中国"));
        BUILTIN_CITIES.put("岳阳", loc(29.357, 113.129, "岳阳", "中国"));
        BUILTIN_CITIES.put("常德", loc(29.031, 111.699, "常德", "中国"));
        BUILTIN_CITIES.put("张家界", loc(29.117, 110.48, "张家界", "中国"));
        BUILTIN_CITIES.put("郴州", loc(25.77, 113.011, "郴州", "中国"));
        BUILTIN_CITIES.put("韶关", loc(24.81, 113.601, "韶关", "中国"));
        BUILTIN_CITIES.put("珠海", loc(22.271, 113.576, "珠海", "中国"));
        BUILTIN_CITIES.put("汕头", loc(23.354, 116.682, "汕头", "中国"));
        BUILTIN_CITIES.put("佛山", loc(23.022, 113.122, "佛山", "中国"));
        BUILTIN_CITIES.put("江门", loc(22.579, 113.082, "江门", "中国"));
        BUILTIN_CITIES.put("湛江", loc(21.271, 110.359, "湛江", "中国"));
        BUILTIN_CITIES.put("惠州", loc(23.111, 114.416, "惠州", "中国"));
        BUILTIN_CITIES.put("东莞", loc(23.021, 113.752, "东莞", "中国"));
        BUILTIN_CITIES.put("中山", loc(22.517, 113.393, "中山", "中国"));
        BUILTIN_CITIES.put("潮州", loc(23.661, 116.622, "潮州", "中国"));
        BUILTIN_CITIES.put("揭阳", loc(23.549, 116.372, "揭阳", "中国"));
        BUILTIN_CITIES.put("柳州", loc(24.327, 109.423, "柳州", "中国"));
        BUILTIN_CITIES.put("桂林", loc(25.273, 110.291, "桂林", "中国"));
        BUILTIN_CITIES.put("北海", loc(21.482, 109.12, "北海", "中国"));
        BUILTIN_CITIES.put("钦州", loc(21.978, 108.654, "钦州", "中国"));
        BUILTIN_CITIES.put("玉林", loc(22.655, 110.175, "玉林", "中国"));
        BUILTIN_CITIES.put("三亚", loc(18.253, 109.512, "三亚", "中国"));
        BUILTIN_CITIES.put("自贡", loc(29.339, 104.778, "自贡", "中国"));
        BUILTIN_CITIES.put("攀枝花", loc(26.582, 101.718, "攀枝花", "中国"));
        BUILTIN_CITIES.put("泸州", loc(28.87, 105.441, "泸州", "中国"));
        BUILTIN_CITIES.put("德阳", loc(31.128, 104.398, "德阳", "中国"));
        BUILTIN_CITIES.put("绵阳", loc(31.467, 104.679, "绵阳", "中国"));
        BUILTIN_CITIES.put("广元", loc(32.436, 105.843, "广元", "中国"));
        BUILTIN_CITIES.put("遂宁", loc(30.538, 105.573, "遂宁", "中国"));
        BUILTIN_CITIES.put("内江", loc(29.582, 105.063, "内江", "中国"));
        BUILTIN_CITIES.put("乐山", loc(29.552, 103.767, "乐山", "中国"));
        BUILTIN_CITIES.put("南充", loc(30.795, 106.084, "南充", "中国"));
        BUILTIN_CITIES.put("宜宾", loc(28.752, 104.643, "宜宾", "中国"));
        BUILTIN_CITIES.put("达州", loc(31.209, 107.468, "达州", "中国"));
        BUILTIN_CITIES.put("资阳", loc(30.121, 104.617, "资阳", "中国"));
        BUILTIN_CITIES.put("遵义", loc(27.727, 106.929, "遵义", "中国"));
        BUILTIN_CITIES.put("安顺", loc(26.245, 105.933, "安顺", "中国"));
        BUILTIN_CITIES.put("铜仁", loc(27.722, 109.191, "铜仁", "中国"));
        BUILTIN_CITIES.put("曲靖", loc(25.49, 103.797, "曲靖", "中国"));
        BUILTIN_CITIES.put("玉溪", loc(24.351, 102.546, "玉溪", "中国"));
        BUILTIN_CITIES.put("丽江", loc(26.855, 100.233, "丽江", "中国"));
        BUILTIN_CITIES.put("大理", loc(25.606, 100.267, "大理", "中国"));
        BUILTIN_CITIES.put("咸阳", loc(34.329, 108.707, "咸阳", "中国"));
        BUILTIN_CITIES.put("铜川", loc(34.903, 108.949, "铜川", "中国"));
        BUILTIN_CITIES.put("渭南", loc(34.499, 109.51, "渭南", "中国"));
        BUILTIN_CITIES.put("延安", loc(36.596, 109.49, "延安", "中国"));
        BUILTIN_CITIES.put("汉中", loc(33.068, 107.024, "汉中", "中国"));
        BUILTIN_CITIES.put("榆林", loc(38.29, 109.741, "榆林", "中国"));
        BUILTIN_CITIES.put("天水", loc(34.578, 105.728, "天水", "中国"));
        BUILTIN_CITIES.put("武威", loc(37.93, 102.632, "武威", "中国"));
        BUILTIN_CITIES.put("张掖", loc(38.925, 100.447, "张掖", "中国"));
        BUILTIN_CITIES.put("酒泉", loc(39.744, 98.49, "酒泉", "中国"));
        BUILTIN_CITIES.put("庆阳", loc(35.712, 107.638, "庆阳", "中国"));
        BUILTIN_CITIES.put("嘉峪关", loc(39.773, 98.291, "嘉峪关", "中国"));
        BUILTIN_CITIES.put("白银", loc(36.545, 104.173, "白银", "中国"));
        BUILTIN_CITIES.put("平凉", loc(35.543, 106.669, "平凉", "中国"));
        BUILTIN_CITIES.put("陇南", loc(33.4, 104.92, "陇南", "中国"));
        BUILTIN_CITIES.put("银川", loc(38.487, 106.231, "银川", "中国"));
        BUILTIN_CITIES.put("石嘴山", loc(38.983, 106.397, "石嘴山", "中国"));
        BUILTIN_CITIES.put("吴忠", loc(37.996, 106.2, "吴忠", "中国"));
        BUILTIN_CITIES.put("固原", loc(36.004, 106.247, "固原", "中国"));
        BUILTIN_CITIES.put("中卫", loc(37.507, 105.186, "中卫", "中国"));
        BUILTIN_CITIES.put("克拉玛依", loc(45.6, 84.889, "克拉玛依", "中国"));
        BUILTIN_CITIES.put("吐鲁番", loc(42.948, 89.185, "吐鲁番", "中国"));
        BUILTIN_CITIES.put("哈密", loc(42.833, 93.515, "哈密", "中国"));
        BUILTIN_CITIES.put("昌吉", loc(44.01, 87.305, "昌吉", "中国"));
        BUILTIN_CITIES.put("喀什", loc(39.467, 75.989, "喀什", "中国"));
        BUILTIN_CITIES.put("和田", loc(37.11, 79.922, "和田", "中国"));
        BUILTIN_CITIES.put("鄂尔多斯", loc(39.608, 109.779, "鄂尔多斯", "中国"));
        BUILTIN_CITIES.put("呼伦贝尔", loc(49.209, 119.767, "呼伦贝尔", "中国"));
        BUILTIN_CITIES.put("通辽", loc(43.651, 122.265, "通辽", "中国"));
        BUILTIN_CITIES.put("齐齐哈尔", loc(47.353, 123.918, "齐齐哈尔", "中国"));
        BUILTIN_CITIES.put("牡丹江", loc(44.551, 129.615, "牡丹江", "中国"));
        BUILTIN_CITIES.put("绥化", loc(46.641, 126.976, "绥化", "中国"));
        BUILTIN_CITIES.put("旧金山", loc(37.774, -122.419, "旧金山", "美国"));
        BUILTIN_CITIES.put("芝加哥", loc(41.878, -87.629, "芝加哥", "美国"));
        BUILTIN_CITIES.put("西雅图", loc(47.606, -122.332, "西雅图", "美国"));
        BUILTIN_CITIES.put("波士顿", loc(42.36, -71.058, "波士顿", "美国"));
        BUILTIN_CITIES.put("华盛顿", loc(38.907, -77.037, "华盛顿", "美国"));
        BUILTIN_CITIES.put("拉斯维加斯", loc(36.171, -115.139, "拉斯维加斯", "美国"));
        BUILTIN_CITIES.put("迈阿密", loc(25.761, -80.191, "迈阿密", "美国"));
        BUILTIN_CITIES.put("温哥华", loc(49.282, -123.12, "温哥华", "加拿大"));
        BUILTIN_CITIES.put("蒙特利尔", loc(45.502, -73.567, "蒙特利尔", "加拿大"));
        BUILTIN_CITIES.put("墨西哥城", loc(19.433, -99.133, "墨西哥城", "墨西哥"));
        BUILTIN_CITIES.put("圣保罗", loc(-23.55, -46.633, "圣保罗", "巴西"));
        BUILTIN_CITIES.put("里约热内卢", loc(-22.906, -43.172, "里约热内卢", "巴西"));
        BUILTIN_CITIES.put("布宜诺斯艾利斯", loc(-34.603, -58.382, "布宜诺斯艾利斯", "阿根廷"));
        BUILTIN_CITIES.put("罗马", loc(41.902, 12.496, "罗马", "意大利"));
        BUILTIN_CITIES.put("米兰", loc(45.464, 9.19, "米兰", "意大利"));
        BUILTIN_CITIES.put("马德里", loc(40.416, -3.703, "马德里", "西班牙"));
        BUILTIN_CITIES.put("巴塞罗那", loc(41.385, 2.173, "巴塞罗那", "西班牙"));
        BUILTIN_CITIES.put("阿姆斯特丹", loc(52.367, 4.904, "阿姆斯特丹", "荷兰"));
        BUILTIN_CITIES.put("维也纳", loc(48.208, 16.373, "维也纳", "奥地利"));
        BUILTIN_CITIES.put("苏黎世", loc(47.374, 8.541, "苏黎世", "瑞士"));
        BUILTIN_CITIES.put("布鲁塞尔", loc(50.85, 4.351, "布鲁塞尔", "比利时"));
        BUILTIN_CITIES.put("哥本哈根", loc(55.676, 12.568, "哥本哈根", "丹麦"));
        BUILTIN_CITIES.put("斯德哥尔摩", loc(59.329, 18.068, "斯德哥尔摩", "瑞典"));
        BUILTIN_CITIES.put("奥斯陆", loc(59.913, 10.752, "奥斯陆", "挪威"));
        BUILTIN_CITIES.put("赫尔辛基", loc(60.169, 24.938, "赫尔辛基", "芬兰"));
        BUILTIN_CITIES.put("华沙", loc(52.229, 21.012, "华沙", "波兰"));
        BUILTIN_CITIES.put("布拉格", loc(50.075, 14.437, "布拉格", "捷克"));
        BUILTIN_CITIES.put("雅典", loc(37.983, 23.727, "雅典", "希腊"));
        BUILTIN_CITIES.put("伊斯坦布尔", loc(41.008, 28.978, "伊斯坦布尔", "土耳其"));
        BUILTIN_CITIES.put("开罗", loc(30.044, 31.236, "开罗", "埃及"));
        BUILTIN_CITIES.put("约翰内斯堡", loc(-26.205, 28.047, "约翰内斯堡", "南非"));
        BUILTIN_CITIES.put("内罗毕", loc(-1.292, 36.821, "内罗毕", "肯尼亚"));
        BUILTIN_CITIES.put("开普敦", loc(-33.924, 18.424, "开普敦", "南非"));
        BUILTIN_CITIES.put("孟买", loc(19.076, 72.878, "孟买", "印度"));
        BUILTIN_CITIES.put("新德里", loc(28.613, 77.209, "新德里", "印度"));
        BUILTIN_CITIES.put("班加罗尔", loc(12.971, 77.594, "班加罗尔", "印度"));
        BUILTIN_CITIES.put("加尔各答", loc(22.573, 88.364, "加尔各答", "印度"));
        BUILTIN_CITIES.put("吉隆坡", loc(3.139, 101.687, "吉隆坡", "马来西亚"));
        BUILTIN_CITIES.put("雅加达", loc(-6.208, 106.846, "雅加达", "印尼"));
        BUILTIN_CITIES.put("河内", loc(21.028, 105.854, "河内", "越南"));
        BUILTIN_CITIES.put("胡志明市", loc(10.823, 106.629, "胡志明市", "越南"));
        BUILTIN_CITIES.put("马尼拉", loc(14.599, 120.984, "马尼拉", "菲律宾"));
        BUILTIN_CITIES.put("釜山", loc(35.18, 129.075, "釜山", "韩国"));
        BUILTIN_CITIES.put("高雄", loc(22.627, 120.301, "高雄", "中国台湾"));
        BUILTIN_CITIES.put("都柏林", loc(53.349, -6.26, "都柏林", "爱尔兰"));
        BUILTIN_CITIES.put("里斯本", loc(38.722, -9.139, "里斯本", "葡萄牙"));
    }

    private static GeoLocation loc(double lat, double lng, String name, String country) {
        GeoLocation g = new GeoLocation();
        g.latitude = lat; g.longitude = lng; g.name = name; g.country = country;
        return g;
    }

    /**
     * OkHttp 客户端，天气 API 响应快，超时设置较短
     * <p>
     * 支持通过环境变量 HTTP_PROXY / HTTPS_PROXY（或 JVM 参数 -Dhttp.proxyHost 等）
     * 走代理访问 Open-Meteo（部分网络环境下直连外网 API 会超时）。
     * </p>
     */
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .proxy(buildProxy())
            .build();

    /**
     * 根据环境变量或 JVM 系统属性构造代理配置；未配置则返回 null（直连）
     */
    private Proxy buildProxy() {
        String proxyHost = firstNonBlank(
                System.getenv("HTTPS_PROXY"),
                System.getenv("HTTP_PROXY"),
                System.getProperty("https.proxyHost"),
                System.getProperty("http.proxyHost")
        );
        if (proxyHost == null || proxyHost.isBlank()) {
            return null;
        }
        // 去掉协议前缀 http:// 或 https://
        String host = proxyHost.replaceFirst("^https?://", "").trim();
        int port = 80;
        if (host.contains(":")) {
            try {
                port = Integer.parseInt(host.substring(host.indexOf(':') + 1).trim());
            } catch (NumberFormatException ignored) {
                port = 80;
            }
            host = host.substring(0, host.indexOf(':'));
        }
        log.info("天气服务使用代理：{}:{}", host, port);
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
    }

    private String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }

    /**
     * 查询城市天气（入口）
     */
    @Override
    public WeatherResultDTO queryByCity(String city) {
        if (city == null || city.isBlank()) {
            throw new BusinessException(400, "城市名称不能为空");
        }
        String trimmedCity = city.trim();
        log.info("查询城市天气，城市内容未记录");

        // 缓存：同一城市 1 小时内不重复请求 Open-Meteo
        String cityCacheKey = "weather:city:" + trimmedCity;
        WeatherResultDTO cityCached = redisCacheService.getObject(cityCacheKey, WeatherResultDTO.class);
        if (cityCached != null) {
            return cityCached;
        }

        // 1. 地理编码：城市名 -> 经纬度（优先内置坐标，失败再调 API）
        GeoLocation location = BUILTIN_CITIES.get(trimmedCity);
        if (location == null) {
            location = geocodeCity(trimmedCity);
        }
        if (location == null) {
            throw new BusinessException(404, "未找到城市：" + trimmedCity + "，请检查城市名称是否正确");
        }

        // 2. 查询天气
        WeatherResultDTO result = queryByLocation(location.latitude, location.longitude);
        result.setCity(location.name);
        result.setCountry(location.country);

        // 写入缓存（1 小时）
        redisCacheService.setObject(cityCacheKey, result, 60);
        return result;
    }

    /**
     * 根据经纬度查询天气
     */
    @Override
    public WeatherResultDTO queryByLocation(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new BusinessException(400, "经纬度不能为空");
        }

        // 缓存：同一经纬度 1 小时内不重复请求 Open-Meteo
        String locCacheKey = "weather:loc:" + latitude + ":" + longitude;
        WeatherResultDTO locCached = redisCacheService.getObject(locCacheKey, WeatherResultDTO.class);
        if (locCached != null) {
            return locCached;
        }

        String url = FORECAST_URL
                + "?latitude=" + latitude
                + "&longitude=" + longitude
                + "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m"
                + "&daily=weather_code,temperature_2m_max,temperature_2m_min"
                + "&timezone=auto"
                + "&forecast_days=7";

        String json = get(url);
        JSONObject root = JSON.parseObject(json);

        JSONObject current = root.getJSONObject("current");
        JSONObject daily = root.getJSONObject("daily");

        WeatherResultDTO result = new WeatherResultDTO();
        result.setLatitude(latitude);
        result.setLongitude(longitude);
        result.setTemperature(current.getDouble("temperature_2m"));
        result.setApparentTemperature(current.getDouble("apparent_temperature"));
        result.setHumidity(current.getInteger("relative_humidity_2m"));
        result.setWindSpeed(current.getDouble("wind_speed_10m"));

        int weatherCode = current.getInteger("weather_code");
        WeatherInfo currentInfo = parseWeatherCode(weatherCode);
        result.setWeatherText(currentInfo.text);
        result.setIconCode(currentInfo.icon);
        result.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        result.setDataSource("Open-Meteo");

        String timezone = root.getString("timezone");
        GeoLocation location = reverseGeocode(latitude, longitude);
        if (location != null && location.name != null && !location.name.isBlank()) {
            result.setCity(location.name);
            result.setCountry(location.country);
        } else if (timezone != null && !timezone.isBlank()) {
            String city = parseCityFromTimezone(timezone);
            result.setCity(city);
        }

        // 7 日预报
        List<DailyForecastDTO> forecastList = buildDailyForecast(daily);
        result.setDailyForecast(forecastList);

        // 写入缓存（1 小时）
        redisCacheService.setObject(locCacheKey, result, 60);

        return result;
    }

    private GeoLocation reverseGeocode(Double latitude, Double longitude) {
        try {
            String url = GEOCODING_URL
                    + "?latitude=" + latitude
                    + "&longitude=" + longitude
                    + "&language=zh"
                    + "&count=1";

            String json = get(url);
            JSONObject root = JSON.parseObject(json);
            JSONArray results = root.getJSONArray("results");

            if (results != null && !results.isEmpty()) {
                JSONObject first = results.getJSONObject(0);
                GeoLocation location = new GeoLocation();
                location.name = first.getString("name");
                location.country = first.getString("country");
                location.latitude = first.getDouble("latitude");
                location.longitude = first.getDouble("longitude");
                return location;
            }
        } catch (Exception e) {
            log.warn("反地理编码失败: {}", e.getMessage());
        }
        return null;
    }

    private String parseCityFromTimezone(String timezone) {
        Map<String, String> timezoneCityMap = new LinkedHashMap<>();
        timezoneCityMap.put("Asia/Shanghai", "上海");
        timezoneCityMap.put("Asia/Beijing", "北京");
        timezoneCityMap.put("Asia/Tianjin", "天津");
        timezoneCityMap.put("Asia/Chongqing", "重庆");
        timezoneCityMap.put("Asia/Hong_Kong", "香港");
        timezoneCityMap.put("Asia/Macau", "澳门");
        timezoneCityMap.put("Asia/Taipei", "台北");
        timezoneCityMap.put("Asia/Guangzhou", "广州");
        timezoneCityMap.put("Asia/Shenzhen", "深圳");
        timezoneCityMap.put("Asia/Hangzhou", "杭州");
        timezoneCityMap.put("Asia/Nanjing", "南京");
        timezoneCityMap.put("Asia/Wuhan", "武汉");
        timezoneCityMap.put("Asia/Chengdu", "成都");
        timezoneCityMap.put("Asia/Xian", "西安");
        timezoneCityMap.put("Asia/Harbin", "哈尔滨");
        timezoneCityMap.put("Asia/Dalian", "大连");
        timezoneCityMap.put("Asia/Qingdao", "青岛");
        timezoneCityMap.put("Asia/Suzhou", "苏州");
        timezoneCityMap.put("Asia/Wuxi", "无锡");
        timezoneCityMap.put("Asia/Ningbo", "宁波");
        timezoneCityMap.put("Asia/Zhengzhou", "郑州");
        timezoneCityMap.put("Asia/Changsha", "长沙");
        timezoneCityMap.put("Asia/Kunming", "昆明");
        timezoneCityMap.put("Asia/Nanning", "南宁");
        timezoneCityMap.put("Asia/Hefei", "合肥");
        timezoneCityMap.put("Asia/Fuzhou", "福州");
        timezoneCityMap.put("Asia/Xiamen", "厦门");
        timezoneCityMap.put("Asia/Jinan", "济南");
        timezoneCityMap.put("Asia/Taiyuan", "太原");
        timezoneCityMap.put("Asia/Shenyang", "沈阳");
        timezoneCityMap.put("Asia/Changchun", "长春");
        timezoneCityMap.put("Asia/Jilin", "吉林");
        timezoneCityMap.put("Asia/Lanzhou", "兰州");
        timezoneCityMap.put("Asia/Xining", "西宁");
        timezoneCityMap.put("Asia/Urumqi", "乌鲁木齐");
        timezoneCityMap.put("Asia/Hohhot", "呼和浩特");
        timezoneCityMap.put("Asia/Nanchang", "南昌");
        timezoneCityMap.put("Asia/Hangzhou", "杭州");
        timezoneCityMap.put("Asia/Shanghai", "上海");
        timezoneCityMap.put("Asia/Seoul", "首尔");
        timezoneCityMap.put("Asia/Tokyo", "东京");
        timezoneCityMap.put("Asia/Singapore", "新加坡");
        timezoneCityMap.put("Asia/Bangkok", "曼谷");
        timezoneCityMap.put("Asia/Kolkata", "加尔各答");
        timezoneCityMap.put("Asia/Dubai", "迪拜");
        timezoneCityMap.put("Asia/Kuwait", "科威特");
        timezoneCityMap.put("Asia/Mumbai", "孟买");
        timezoneCityMap.put("Asia/Delhi", "德里");
        timezoneCityMap.put("Asia/Kuala_Lumpur", "吉隆坡");
        timezoneCityMap.put("Asia/Jakarta", "雅加达");
        timezoneCityMap.put("Asia/Manila", "马尼拉");
        timezoneCityMap.put("Asia/Phnom_Penh", "金边");
        timezoneCityMap.put("Asia/Vientiane", "万象");
        timezoneCityMap.put("Asia/Hanoi", "河内");
        timezoneCityMap.put("Asia/Haiphong", "海防");
        timezoneCityMap.put("Asia/Yangon", "仰光");
        timezoneCityMap.put("Asia/Bangkok", "曼谷");
        timezoneCityMap.put("Asia/Chennai", "金奈");
        timezoneCityMap.put("Asia/Bangalore", "班加罗尔");
        timezoneCityMap.put("Asia/Colombo", "科伦坡");
        timezoneCityMap.put("Asia/Dhaka", "达卡");
        timezoneCityMap.put("Asia/Kathmandu", "加德满都");
        timezoneCityMap.put("Asia/Thimphu", "廷布");
        timezoneCityMap.put("Asia/Almaty", "阿拉木图");
        timezoneCityMap.put("Asia/Bishkek", "比什凯克");
        timezoneCityMap.put("Asia/Tashkent", "塔什干");
        timezoneCityMap.put("Asia/Ashgabat", "阿什哈巴德");
        timezoneCityMap.put("Asia/Dushanbe", "杜尚别");
        timezoneCityMap.put("Asia/Muscat", "马斯喀特");
        timezoneCityMap.put("Asia/Aden", "亚丁");
        timezoneCityMap.put("Asia/Baghdad", "巴格达");
        timezoneCityMap.put("Asia/Tehran", "德黑兰");
        timezoneCityMap.put("Asia/Jerusalem", "耶路撒冷");
        timezoneCityMap.put("Asia/Baku", "巴库");
        timezoneCityMap.put("Asia/Tbilisi", "第比利斯");
        timezoneCityMap.put("Asia/Yerevan", "埃里温");
        timezoneCityMap.put("Asia/Nicosia", "尼科西亚");
        timezoneCityMap.put("Asia/Kuwait", "科威特");
        timezoneCityMap.put("Asia/Riyadh", "利雅得");
        timezoneCityMap.put("Asia/Jeddah", "吉达");
        timezoneCityMap.put("Asia/Abu_Dhabi", "阿布扎比");
        timezoneCityMap.put("Asia/Dubai", "迪拜");
        timezoneCityMap.put("Asia/Qatar", "多哈");
        timezoneCityMap.put("Asia/Oman", "马斯喀特");
        timezoneCityMap.put("Asia/Bahrain", "麦纳麦");
        timezoneCityMap.put("Asia/Kuwait", "科威特");
        timezoneCityMap.put("Asia/Saudi_Arabia", "利雅得");
        timezoneCityMap.put("Asia/Jordan", "安曼");
        timezoneCityMap.put("Asia/Syria", "大马士革");
        timezoneCityMap.put("Asia/Lebanon", "贝鲁特");
        timezoneCityMap.put("Asia/Cyprus", "尼科西亚");
        timezoneCityMap.put("Europe/London", "伦敦");
        timezoneCityMap.put("Europe/Paris", "巴黎");
        timezoneCityMap.put("Europe/Berlin", "柏林");
        timezoneCityMap.put("Europe/Rome", "罗马");
        timezoneCityMap.put("Europe/Madrid", "马德里");
        timezoneCityMap.put("Europe/Amsterdam", "阿姆斯特丹");
        timezoneCityMap.put("Europe/Brussels", "布鲁塞尔");
        timezoneCityMap.put("Europe/Luxembourg", "卢森堡");
        timezoneCityMap.put("Europe/Stockholm", "斯德哥尔摩");
        timezoneCityMap.put("Europe/Copenhagen", "哥本哈根");
        timezoneCityMap.put("Europe/Oslo", "奥斯陆");
        timezoneCityMap.put("Europe/Helsinki", "赫尔辛基");
        timezoneCityMap.put("Europe/Warsaw", "华沙");
        timezoneCityMap.put("Europe/Prague", "布拉格");
        timezoneCityMap.put("Europe/Vienna", "维也纳");
        timezoneCityMap.put("Europe/Budapest", "布达佩斯");
        timezoneCityMap.put("Europe/Zagreb", "萨格勒布");
        timezoneCityMap.put("Europe/Ljubljana", "卢布尔雅那");
        timezoneCityMap.put("Europe/Belgrade", "贝尔格莱德");
        timezoneCityMap.put("Europe/Sofia", "索非亚");
        timezoneCityMap.put("Europe/Bucharest", "布加勒斯特");
        timezoneCityMap.put("Europe/Athens", "雅典");
        timezoneCityMap.put("Europe/Istanbul", "伊斯坦布尔");
        timezoneCityMap.put("Europe/Moscow", "莫斯科");
        timezoneCityMap.put("Europe/Kiev", "基辅");
        timezoneCityMap.put("Europe/Minsk", "明斯克");
        timezoneCityMap.put("Europe/Tallinn", "塔林");
        timezoneCityMap.put("Europe/Riga", "里加");
        timezoneCityMap.put("Europe/Vilnius", "维尔纽斯");
        timezoneCityMap.put("Europe/Bratislava", "布拉迪斯拉发");
        timezoneCityMap.put("Europe/Lisbon", "里斯本");
        timezoneCityMap.put("Europe/Valletta", "瓦莱塔");
        timezoneCityMap.put("Europe/Malta", "瓦莱塔");
        timezoneCityMap.put("Europe/San_Marino", "圣马力诺");
        timezoneCityMap.put("Europe/Vatican", "梵蒂冈");
        timezoneCityMap.put("Europe/Andorra", "安道尔");
        timezoneCityMap.put("Europe/Monaco", "摩纳哥");
        timezoneCityMap.put("Europe/Liechtenstein", "列支敦士登");
        timezoneCityMap.put("America/New_York", "纽约");
        timezoneCityMap.put("America/Los_Angeles", "洛杉矶");
        timezoneCityMap.put("America/Chicago", "芝加哥");
        timezoneCityMap.put("America/Houston", "休斯顿");
        timezoneCityMap.put("America/Phoenix", "凤凰城");
        timezoneCityMap.put("America/Philadelphia", "费城");
        timezoneCityMap.put("America/San_Antonio", "圣安东尼奥");
        timezoneCityMap.put("America/San_Diego", "圣迭戈");
        timezoneCityMap.put("America/Dallas", "达拉斯");
        timezoneCityMap.put("America/Austin", "奥斯汀");
        timezoneCityMap.put("America/Seattle", "西雅图");
        timezoneCityMap.put("America/Denver", "丹佛");
        timezoneCityMap.put("America/Boston", "波士顿");
        timezoneCityMap.put("America/Washington", "华盛顿");
        timezoneCityMap.put("America/Miami", "迈阿密");
        timezoneCityMap.put("America/Atlanta", "亚特兰大");
        timezoneCityMap.put("America/Detroit", "底特律");
        timezoneCityMap.put("America/Cleveland", "克利夫兰");
        timezoneCityMap.put("America/Pittsburgh", "匹兹堡");
        timezoneCityMap.put("America/Baltimore", "巴尔的摩");
        timezoneCityMap.put("America/Indianapolis", "印第安纳波利斯");
        timezoneCityMap.put("America/Columbus", "哥伦布");
        timezoneCityMap.put("America/Charlotte", "夏洛特");
        timezoneCityMap.put("America/Fort_Worth", "沃斯堡");
        timezoneCityMap.put("America/El_Paso", "埃尔帕索");
        timezoneCityMap.put("America/Portland", "波特兰");
        timezoneCityMap.put("America/San_Francisco", "旧金山");
        timezoneCityMap.put("America/Las_Vegas", "拉斯维加斯");
        timezoneCityMap.put("America/Salt_Lake_City", "盐湖城");
        timezoneCityMap.put("America/Albuquerque", "阿尔伯克基");
        timezoneCityMap.put("America/Tucson", "图森");
        timezoneCityMap.put("America/Omaha", "奥马哈");
        timezoneCityMap.put("America/Kansas_City", "堪萨斯城");
        timezoneCityMap.put("America/St_Louis", "圣路易斯");
        timezoneCityMap.put("America/Minneapolis", "明尼阿波利斯");
        timezoneCityMap.put("America/Milwaukee", "密尔沃基");
        timezoneCityMap.put("America/Chicago", "芝加哥");
        timezoneCityMap.put("America/Detroit", "底特律");
        timezoneCityMap.put("America/Cincinnati", "辛辛那提");
        timezoneCityMap.put("America/Nashville", "纳什维尔");
        timezoneCityMap.put("America/New_Orleans", "新奥尔良");
        timezoneCityMap.put("America/Birmingham", "伯明翰");
        timezoneCityMap.put("America/Memphis", "孟菲斯");
        timezoneCityMap.put("America/Houston", "休斯顿");
        timezoneCityMap.put("America/Dallas", "达拉斯");
        timezoneCityMap.put("America/Austin", "奥斯汀");
        timezoneCityMap.put("America/San_Antonio", "圣安东尼奥");
        timezoneCityMap.put("America/El_Paso", "埃尔帕索");
        timezoneCityMap.put("America/Phoenix", "凤凰城");
        timezoneCityMap.put("America/Los_Angeles", "洛杉矶");
        timezoneCityMap.put("America/San_Diego", "圣迭戈");
        timezoneCityMap.put("America/San_Francisco", "旧金山");
        timezoneCityMap.put("America/Seattle", "西雅图");
        timezoneCityMap.put("America/Anchorage", "安克雷奇");
        timezoneCityMap.put("America/Honolulu", "檀香山");
        timezoneCityMap.put("America/Toronto", "多伦多");
        timezoneCityMap.put("America/Vancouver", "温哥华");
        timezoneCityMap.put("America/Montreal", "蒙特利尔");
        timezoneCityMap.put("America/Calgary", "卡尔加里");
        timezoneCityMap.put("America/Edmonton", "埃德蒙顿");
        timezoneCityMap.put("America/Ottawa", "渥太华");
        timezoneCityMap.put("America/Mexico_City", "墨西哥城");
        timezoneCityMap.put("America/Sao_Paulo", "圣保罗");
        timezoneCityMap.put("America/Buenos_Aires", "布宜诺斯艾利斯");
        timezoneCityMap.put("America/Rio_de_Janeiro", "里约热内卢");
        timezoneCityMap.put("America/Santiago", "圣地亚哥");
        timezoneCityMap.put("America/Lima", "利马");
        timezoneCityMap.put("America/Bogota", "波哥大");
        timezoneCityMap.put("America/Caracas", "加拉加斯");
        timezoneCityMap.put("America/Guatemala", "危地马拉");
        timezoneCityMap.put("America/Panama", "巴拿马城");
        timezoneCityMap.put("America/Havana", "哈瓦那");
        timezoneCityMap.put("America/Santo_Domingo", "圣多明各");
        timezoneCityMap.put("America/Port_of_Spain", "西班牙港");
        timezoneCityMap.put("America/Trinidad", "西班牙港");
        timezoneCityMap.put("America/Montevideo", "蒙得维的亚");
        timezoneCityMap.put("America/Asuncion", "亚松森");
        timezoneCityMap.put("America/La_Paz", "拉巴斯");
        timezoneCityMap.put("America/Quito", "基多");
        timezoneCityMap.put("America/Cayenne", "卡宴");
        timezoneCityMap.put("America/Belem", "贝伦");
        timezoneCityMap.put("America/Manaus", "马瑙斯");
        timezoneCityMap.put("America/Recife", "累西腓");
        timezoneCityMap.put("America/Brasilia", "巴西利亚");
        timezoneCityMap.put("America/Cuiaba", "库亚巴");
        timezoneCityMap.put("America/Sao_Paulo", "圣保罗");
        timezoneCityMap.put("America/Rio_de_Janeiro", "里约热内卢");
        timezoneCityMap.put("America/Belo_Horizonte", "贝洛奥里藏特");
        timezoneCityMap.put("America/Campinas", "坎皮纳斯");
        timezoneCityMap.put("America/Porto_Alegre", "阿雷格里港");
        timezoneCityMap.put("America/Salvador", "萨尔瓦多");
        timezoneCityMap.put("America/Fortaleza", "福塔莱萨");
        timezoneCityMap.put("America/Natal", "纳塔尔");
        timezoneCityMap.put("America/Maceio", "马塞约");
        timezoneCityMap.put("America/Joao_Pessoa", "若昂佩索阿");
        timezoneCityMap.put("America/Aracaju", "阿拉卡茹");
        timezoneCityMap.put("America/Teresina", "特雷西纳");
        timezoneCityMap.put("America/Palmas", "帕尔马斯");
        timezoneCityMap.put("America/Brasilia", "巴西利亚");
        timezoneCityMap.put("Africa/Cairo", "开罗");
        timezoneCityMap.put("Africa/Johannesburg", "约翰内斯堡");
        timezoneCityMap.put("Africa/Lagos", "拉各斯");
        timezoneCityMap.put("Africa/Nairobi", "内罗毕");
        timezoneCityMap.put("Africa/Cape_Town", "开普敦");
        timezoneCityMap.put("Africa/Durban", "德班");
        timezoneCityMap.put("Africa/Port_Elizabeth", "伊丽莎白港");
        timezoneCityMap.put("Africa/Bloemfontein", "布隆方丹");
        timezoneCityMap.put("Africa/Pretoria", "比勒陀利亚");
        timezoneCityMap.put("Africa/Soweto", "索韦托");
        timezoneCityMap.put("Africa/Alexandria", "亚历山大");
        timezoneCityMap.put("Africa/Giza", "吉萨");
        timezoneCityMap.put("Africa/Sharm_el_Sheikh", "沙姆沙伊赫");
        timezoneCityMap.put("Africa/Tunis", "突尼斯");
        timezoneCityMap.put("Africa/Algiers", "阿尔及尔");
        timezoneCityMap.put("Africa/Casablanca", "卡萨布兰卡");
        timezoneCityMap.put("Africa/Dakar", "达喀尔");
        timezoneCityMap.put("Africa/Abidjan", "阿比让");
        timezoneCityMap.put("Africa/Accra", "阿克拉");
        timezoneCityMap.put("Africa/Lagos", "拉各斯");
        timezoneCityMap.put("Africa/Port_Harcourt", "哈科特港");
        timezoneCityMap.put("Africa/Abuja", "阿布贾");
        timezoneCityMap.put("Africa/Kano", "卡诺");
        timezoneCityMap.put("Africa/Onitsha", "奥尼查");
        timezoneCityMap.put("Africa/Ibadan", "伊巴丹");
        timezoneCityMap.put("Africa/Johannesburg", "约翰内斯堡");
        timezoneCityMap.put("Africa/Cape_Town", "开普敦");
        timezoneCityMap.put("Africa/Durban", "德班");
        timezoneCityMap.put("Africa/Nairobi", "内罗毕");
        timezoneCityMap.put("Africa/Addis_Ababa", "亚的斯亚贝巴");
        timezoneCityMap.put("Africa/Dar_es_Salaam", "达累斯萨拉姆");
        timezoneCityMap.put("Africa/Kampala", "坎帕拉");
        timezoneCityMap.put("Africa/Mombasa", "蒙巴萨");
        timezoneCityMap.put("Africa/Zanzibar", "桑给巴尔");
        timezoneCityMap.put("Africa/Lusaka", "卢萨卡");
        timezoneCityMap.put("Africa/Harare", "哈拉雷");
        timezoneCityMap.put("Africa/Bulawayo", "布拉瓦约");
        timezoneCityMap.put("Africa/Maputo", "马普托");
        timezoneCityMap.put("Africa/Windhoek", "温得和克");
        timezoneCityMap.put("Africa/Gaborone", "哈博罗内");
        timezoneCityMap.put("Africa/Maseru", "马塞卢");
        timezoneCityMap.put("Africa/Mbabane", "姆巴巴内");
        timezoneCityMap.put("Africa/Port_Louis", "路易港");
        timezoneCityMap.put("Africa/Reunion", "圣但尼");
        timezoneCityMap.put("Africa/Mauritius", "路易港");
        timezoneCityMap.put("Africa/Seychelles", "维多利亚");
        timezoneCityMap.put("Africa/Maldives", "马累");
        timezoneCityMap.put("Oceania/Sydney", "悉尼");
        timezoneCityMap.put("Oceania/Melbourne", "墨尔本");
        timezoneCityMap.put("Oceania/Brisbane", "布里斯班");
        timezoneCityMap.put("Oceania/Perth", "珀斯");
        timezoneCityMap.put("Oceania/Adelaide", "阿德莱德");
        timezoneCityMap.put("Oceania/Hobart", "霍巴特");
        timezoneCityMap.put("Oceania/Darwin", "达尔文");
        timezoneCityMap.put("Oceania/Canberra", "堪培拉");
        timezoneCityMap.put("Oceania/Auckland", "奥克兰");
        timezoneCityMap.put("Oceania/Wellington", "惠灵顿");
        timezoneCityMap.put("Oceania/Christchurch", "基督城");
        timezoneCityMap.put("Oceania/Queenstown", "皇后镇");
        timezoneCityMap.put("Oceania/Suva", "苏瓦");
        timezoneCityMap.put("Oceania/Noumea", "努美阿");
        timezoneCityMap.put("Oceania/Papeete", "帕皮提");
        timezoneCityMap.put("Oceania/Guam", "关岛");
        timezoneCityMap.put("Oceania/Honolulu", "檀香山");
        timezoneCityMap.put("Oceania/Port_Moresby", "莫尔兹比港");
        timezoneCityMap.put("Oceania/Brisbane", "布里斯班");
        timezoneCityMap.put("Oceania/Sydney", "悉尼");
        timezoneCityMap.put("Oceania/Melbourne", "墨尔本");
        timezoneCityMap.put("Oceania/Perth", "珀斯");
        timezoneCityMap.put("Oceania/Auckland", "奥克兰");
        timezoneCityMap.put("Oceania/Wellington", "惠灵顿");

        String city = timezoneCityMap.get(timezone);
        if (city != null) {
            return city;
        }

        String[] parts = timezone.split("/");
        if (parts.length >= 2) {
            String cityPart = parts[parts.length - 1].replace("_", " ");
            return cityPart;
        }

        return "未知";
    }

    /**
     * 城市地理编码
     */
    private GeoLocation geocodeCity(String city) {
        GeoLocation g = doGeocode(city, "zh");
        if (g != null) {
            return g;
        }
        return doGeocode(city, "en");
    }

    private GeoLocation doGeocode(String city, String lang) {
        try {
            String encoded = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String url = GEOCODING_URL + "?name=" + encoded
                    + "&count=1&language=" + lang + "&format=json";

            String json = get(url);
            JSONObject root = JSON.parseObject(json);
            JSONArray results = root.getJSONArray("results");
            if (results == null || results.isEmpty()) {
                return null;
            }

            JSONObject first = results.getJSONObject(0);
            GeoLocation location = new GeoLocation();
            location.name = first.getString("name");
            location.country = first.getString("country");
            location.latitude = first.getDouble("latitude");
            location.longitude = first.getDouble("longitude");
            return location;
        } catch (BusinessException e) {
            log.warn("地理编码失败（lang={}）：{}", lang, e.getMessage());
            return null;
        }
    }

    /**
     * 构建 7 日预报列表
     */
    private List<DailyForecastDTO> buildDailyForecast(JSONObject daily) {
        List<DailyForecastDTO> list = new ArrayList<>();
        if (daily == null) {
            return list;
        }

        JSONArray dates = daily.getJSONArray("time");
        JSONArray codes = daily.getJSONArray("weather_code");
        JSONArray maxTemps = daily.getJSONArray("temperature_2m_max");
        JSONArray minTemps = daily.getJSONArray("temperature_2m_min");

        if (dates == null || codes == null || maxTemps == null || minTemps == null) {
            return list;
        }

        for (int i = 0; i < dates.size(); i++) {
            DailyForecastDTO day = new DailyForecastDTO();
            String dateStr = dates.getString(i);
            day.setDate(dateStr);
            day.setWeekday(getWeekday(dateStr));
            day.setMaxTemp(maxTemps.getDouble(i));
            day.setMinTemp(minTemps.getDouble(i));

            WeatherInfo info = parseWeatherCode(codes.getInteger(i));
            day.setWeatherText(info.text);
            day.setIconCode(info.icon);
            list.add(day);
        }
        return list;
    }

    /**
     * 根据日期字符串获取中文星期几
     */
    private String getWeekday(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.SIMPLIFIED_CHINESE);
            return dayName.replace("周", "星期");
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 发送 GET 请求并返回 JSON 字符串
     */
    private String get(String url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                log.error("天气 API 请求失败，状态码: {}", response.code());
                throw new BusinessException(500, "天气服务暂时不可用（HTTP " + response.code() + "），请稍后重试");
            }
            return response.body().string();
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            log.error("天气 API 网络异常，异常类型={}", e.getClass().getSimpleName());
            throw new BusinessException(500, "无法连接天气服务，请检查服务器网络或代理设置后重试");
        }
    }

    /**
     * 将 Open-Meteo 天气代码转换为中文描述和图标编码
     */
    private WeatherInfo parseWeatherCode(int code) {
        return switch (code) {
            case 0 -> new WeatherInfo("晴", "sunny");
            case 1, 2 -> new WeatherInfo("多云", "cloudy");
            case 3 -> new WeatherInfo("阴", "overcast");
            case 45, 48 -> new WeatherInfo("雾", "fog");
            case 51, 53, 55 -> new WeatherInfo("小雨", "drizzle");
            case 56, 57 -> new WeatherInfo("冻雨", "freezing-rain");
            case 61, 63, 65 -> new WeatherInfo("雨", "rain");
            case 66, 67 -> new WeatherInfo("冻雨", "freezing-rain");
            case 71, 73, 75 -> new WeatherInfo("雪", "snow");
            case 77 -> new WeatherInfo("雪粒", "snow");
            case 80, 81, 82 -> new WeatherInfo("阵雨", "rain-shower");
            case 85, 86 -> new WeatherInfo("阵雪", "snow-shower");
            case 95 -> new WeatherInfo("雷雨", "thunderstorm");
            case 96, 99 -> new WeatherInfo("雷阵雨伴冰雹", "thunderstorm");
            default -> new WeatherInfo("未知", "unknown");
        };
    }

    /**
     * 天气描述与图标内部类
     */
    private static class WeatherInfo {
        final String text;
        final String icon;
        WeatherInfo(String text, String icon) {
            this.text = text;
            this.icon = icon;
        }
    }

    /**
     * 地理位置内部类
     */
    private static class GeoLocation {
        String name;
        String country;
        Double latitude;
        Double longitude;
    }
}
