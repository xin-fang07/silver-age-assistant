package com.example.elderai.common;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTML 富文本清洗工具（XSS 防护）。
 * <p>
 * 使用 Jsoup 白名单（Safelist）对富文本进行净化，只保留安全的排版标签，
 * 强制剥离 script / style / iframe / object / embed、所有 on* 事件属性、
 * javascript: / data: 等危险协议。清洗失败（或内容为空）时安全降级，不抛异常。
 * </p>
 * <p>
 * 用法：在资讯入库前调用 {@code HtmlSanitizer.sanitize(content)}，
 * 保证数据库中存储的始终是安全 HTML，所有消费端（前端详情页等）均受益。
 * </p>
 */
public final class HtmlSanitizer {

    private static final Logger log = LoggerFactory.getLogger(HtmlSanitizer.class);

    /** 宽松白名单：保留常用富文本排版标签，剔除危险标签/属性 */
    private static final Safelist SAFE_LIST = buildSafelist();

    private HtmlSanitizer() {
    }

    private static Safelist buildSafelist() {
        return Safelist.relaxed()
                // 链接：仅允许 http/https/mailto，并支持 target/rel（用于新窗口打开）
                .addAttributes("a", "href", "target", "rel")
                // 图片：仅允许 http/https 来源，避免 data: 注入
                .addAttributes("img", "src", "alt", "width", "height")
                .addProtocols("a", "href", "http", "https", "mailto")
                .addProtocols("img", "src", "http", "https")
                .preserveRelativeLinks(false);
    }

    /**
     * 净化 HTML 富文本。
     *
     * @param html 原始 HTML（可能为 null）
     * @return 净化后的安全 HTML；入参为 null 时返回 null，异常时返回空串
     */
    public static String sanitize(String html) {
        if (html == null) {
            return null;
        }
        try {
            Cleaner cleaner = new Cleaner(SAFE_LIST);
            Document clean = cleaner.clean(Jsoup.parse(html));
            // 给“新窗口打开”的链接补 rel，防止 tabnabbing（钓鱼页篡改原窗口）
            for (Element a : clean.select("a[target=_blank]")) {
                a.attr("rel", "noopener noreferrer");
            }
            return clean.body().html();
        } catch (Exception e) {
            log.warn("HTML 富文本清洗失败: {}", e.getMessage());
            return "";
        }
    }
}
