package com.example.demo.util;

import com.example.demo.model.SeoPage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

/**
 * SEO页面HTML生成工具类
 * 用于根据实体类数据生成完整的HTML页面
 */
public class SeoPageHtmlUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 生成完整的HTML页面
     *
     * @param entity SEO页面实体对象
     * @return 完整的HTML字符串
     */
    public static String generateFullHtml(SeoPage entity) {
        if (entity == null) {
            return "";
        }

        StringBuilder html = new StringBuilder();

        // DOCTYPE声明
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"zh-CN\">\n");

        // 生成head部分
        html.append(generateHead(entity));

        // 生成body部分
        html.append(generateBody(entity));

        html.append("</html>");

        return html.toString();
    }

    /**
     * 生成HTML head部分
     */
    private static String generateHead(SeoPage entity) {
        StringBuilder head = new StringBuilder();
        head.append("<head>\n");

        // 基础meta标签
        head.append("    <meta charset=\"UTF-8\">\n");
        head.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");

        // 标题
        if (entity.getTitle() != null && !entity.getTitle().isEmpty()) {
            head.append("    <title>").append(escapeHtml(entity.getTitle())).append("</title>\n");
        }

        // 描述
        if (entity.getDescription() != null && !entity.getDescription().isEmpty()) {
            head.append("    <meta name=\"description\" content=\"").append(escapeHtml(entity.getDescription())).append("\">\n");
        }

        // 关键词
        if (entity.getKeywords() != null && !entity.getKeywords().isEmpty()) {
            head.append("    <meta name=\"keywords\" content=\"").append(escapeHtml(entity.getKeywords())).append("\">\n");
        }

        // Robots指令
        if (entity.getRobots() != null && !entity.getRobots().isEmpty()) {
            head.append("    <meta name=\"robots\" content=\"").append(escapeHtml(entity.getRobots())).append("\">\n");
        }

        // 规范化URL
        if (entity.getCanonicalUrl() != null && !entity.getCanonicalUrl().isEmpty()) {
            head.append("    <link rel=\"canonical\" href=\"").append(escapeHtml(entity.getCanonicalUrl())).append("\">\n");
        }

        // Open Graph标签
        head.append(generateOpenGraph(entity));

        // 结构化数据（JSON-LD）
        if (entity.getStructuredData() != null && !entity.getStructuredData().isEmpty()) {
            head.append("    <script type=\"application/ld+json\">\n");
            head.append("        ").append(entity.getStructuredData()).append("\n");
            head.append("    </script>\n");
        }

        head.append("</head>\n");

        return head.toString();
    }

    /**
     * 生成Open Graph标签
     */
    private static String generateOpenGraph(SeoPage entity) {
        StringBuilder og = new StringBuilder();

        // og:title
        String ogTitle = entity.getOgTitle() != null ? entity.getOgTitle() : entity.getTitle();
        if (ogTitle != null && !ogTitle.isEmpty()) {
            og.append("    <meta property=\"og:title\" content=\"").append(escapeHtml(ogTitle)).append("\">\n");
        }

        // og:description
        String ogDescription = entity.getOgDescription() != null ? entity.getOgDescription() : entity.getDescription();
        if (ogDescription != null && !ogDescription.isEmpty()) {
            og.append("    <meta property=\"og:description\" content=\"").append(escapeHtml(ogDescription)).append("\">\n");
        }

        // og:image
        if (entity.getOgImage() != null && !entity.getOgImage().isEmpty()) {
            og.append("    <meta property=\"og:image\" content=\"").append(escapeHtml(entity.getOgImage())).append("\">\n");
        }

        // og:type
        if (entity.getOgType() != null && !entity.getOgType().isEmpty()) {
            og.append("    <meta property=\"og:type\" content=\"").append(escapeHtml(entity.getOgType())).append("\">\n");
        }

        // og:url（使用规范化URL）
        if (entity.getCanonicalUrl() != null && !entity.getCanonicalUrl().isEmpty()) {
            og.append("    <meta property=\"og:url\" content=\"").append(escapeHtml(entity.getCanonicalUrl())).append("\">\n");
        }

        return og.toString();
    }

    /**
     * 生成HTML body部分
     */
    private static String generateBody(SeoPage entity) {
        StringBuilder body = new StringBuilder();
        body.append("<body>\n");

        // 主要内容区域
        body.append("    <main>\n");

        // 页面标题（可见标题）
        if (entity.getTitle() != null && !entity.getTitle().isEmpty()) {
            body.append("        <h1>").append(escapeHtml(entity.getTitle())).append("</h1>\n");
        }

        // 页面内容（HTML格式）
        if (entity.getContentHtml() != null && !entity.getContentHtml().isEmpty()) {
            body.append("        <div class=\"content\">\n");
            body.append("            ").append(entity.getContentHtml()).append("\n");
            body.append("        </div>\n");
        } else if (entity.getContentText() != null && !entity.getContentText().isEmpty()) {
            // 如果没有HTML内容，使用纯文本内容（转换为段落）
            body.append("        <div class=\"content\">\n");
            String[] paragraphs = entity.getContentText().split("\n");
            for (String paragraph : paragraphs) {
                if (!paragraph.trim().isEmpty()) {
                    body.append("            <p>").append(escapeHtml(paragraph.trim())).append("</p>\n");
                }
            }
            body.append("        </div>\n");
        }

        body.append("    </main>\n");

        // 页面底部信息（可选）
        body.append("    <footer>\n");
        body.append("        <p>最后更新：").append(formatDateTime(entity.getUpdatedAt())).append("</p>\n");
        body.append("        <p>抓取次数：").append(entity.getCrawlCount()).append("</p>\n");
        body.append("    </footer>\n");

        body.append("</body>\n");

        return body.toString();
    }

    /**
     * 生成用于Sitemap的XML（扩展功能）
     */
    public static String generateSitemapXml(SeoPage entity) {
        if (entity == null || entity.getUrlPath() == null) {
            return "";
        }

        StringBuilder xml = new StringBuilder();
        xml.append("    <url>\n");
        xml.append("        <loc>").append(escapeXml(entity.getCanonicalUrl() != null ? entity.getCanonicalUrl() : entity.getUrlPath())).append("</loc>\n");

        if (entity.getSitemapPriority() != null) {
            xml.append("        <priority>").append(entity.getSitemapPriority()).append("</priority>\n");
        }

        if (entity.getSitemapChangefreq() != null && !entity.getSitemapChangefreq().isEmpty()) {
            xml.append("        <changefreq>").append(entity.getSitemapChangefreq()).append("</changefreq>\n");
        }

        if (entity.getUpdatedAt() != null) {
            xml.append("        <lastmod>").append(formatDateTime(entity.getUpdatedAt())).append("</lastmod>\n");
        }

        xml.append("    </url>\n");

        return xml.toString();
    }

    /**
     * HTML转义（防XSS）
     */
    private static String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * XML转义
     */
    private static String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    /**
     * 格式化日期时间
     */
    private static String formatDateTime(LocalDateTime dateTime) {

        if (dateTime == null) {
            return "未知";
        }
        return dateTime.format(DATE_FORMATTER);
    }
    private static String formatDateTime(Date dateTime) {
        LocalDateTime localDateTime = dateTime.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        return formatDateTime(localDateTime);
    }
}