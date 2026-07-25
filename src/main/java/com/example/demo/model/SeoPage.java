package com.example.demo.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * SEO页面信息实体类，对应数据库表 seo_pages
 * <p>
 * 存储页面的SEO元信息、Open Graph标签、结构化数据、Sitemap配置等
 * </p>
 *
 * @author demo
 * @since 1.0
 */
public class SeoPage {

    /**
     * URL路径（主键）
     */
    private String urlPath;

    /**
     * 页面标题（建议60字符内）
     */
    private String title;

    /**
     * 页面描述（建议160字符内）
     */
    private String description;

    /**
     * 页面关键词（逗号分隔）
     */
    private String keywords;

    /**
     * 页面纯文本内容（供爬虫提取）
     */
    private String contentText;

    /**
     * 页面HTML内容（供爬虫渲染）
     */
    private String contentHtml;

    /**
     * 结构化数据（JSON-LD格式，如Article、Breadcrumb等）
     */
    private String structuredData;

    /**
     * Open Graph标题
     */
    private String ogTitle;

    /**
     * Open Graph描述
     */
    private String ogDescription;

    /**
     * Open Graph图片URL
     */
    private String ogImage;

    /**
     * Open Graph类型（website/article等）
     */
    private String ogType;

    /**
     * 规范化URL
     */
    private String canonicalUrl;

    /**
     * Robots指令（index/follow等）
     */
    private String robots;

    /**
     * Sitemap优先级（0.0-1.0）
     */
    private BigDecimal sitemapPriority;

    /**
     * Sitemap更新频率（always/hourly/daily/weekly/monthly/yearly/never）
     */
    private String sitemapChangefreq;

    /**
     * 状态：1-启用 0-停用
     */
    private Integer status;

    /**
     * 软删除标记：0-未删除 1-已删除
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 最后一次被爬虫抓取时间
     */
    private Date lastCrawledAt;

    /**
     * 被爬虫抓取次数
     */
    private Integer crawlCount;

    /**
     * 邮箱
     */
    private String userEmail;

    public SeoPage() {
    }

    /**
     * 全参构造方法
     *
     * @param urlPath             URL路径（主键）
     * @param title               页面标题
     * @param description         页面描述
     * @param keywords            页面关键词
     * @param contentText         页面纯文本内容
     * @param contentHtml         页面HTML内容
     * @param structuredData      结构化数据（JSON-LD格式）
     * @param ogTitle             Open Graph标题
     * @param ogDescription       Open Graph描述
     * @param ogImage             Open Graph图片URL
     * @param ogType              Open Graph类型
     * @param canonicalUrl        规范化URL
     * @param robots              Robots指令
     * @param sitemapPriority     Sitemap优先级
     * @param sitemapChangefreq   Sitemap更新频率
     * @param status              状态
     * @param isDeleted           软删除标记
     * @param createdAt           创建时间
     * @param updatedAt           更新时间
     * @param lastCrawledAt       最后抓取时间
     * @param crawlCount          抓取次数
     * @param userEmail           邮箱
     */
    public SeoPage(String urlPath, String title, String description, String keywords,
                   String contentText, String contentHtml, String structuredData,
                   String ogTitle, String ogDescription, String ogImage, String ogType,
                   String canonicalUrl, String robots, BigDecimal sitemapPriority,
                   String sitemapChangefreq, Integer status, Integer isDeleted,
                   Date createdAt, Date updatedAt, Date lastCrawledAt, Integer crawlCount,String userEmail) {
        this.urlPath = urlPath;
        this.title = title;
        this.description = description;
        this.keywords = keywords;
        this.contentText = contentText;
        this.contentHtml = contentHtml;
        this.structuredData = structuredData;
        this.ogTitle = ogTitle;
        this.ogDescription = ogDescription;
        this.ogImage = ogImage;
        this.ogType = ogType;
        this.canonicalUrl = canonicalUrl;
        this.robots = robots;
        this.sitemapPriority = sitemapPriority;
        this.sitemapChangefreq = sitemapChangefreq;
        this.status = status;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastCrawledAt = lastCrawledAt;
        this.crawlCount = crawlCount;
        this.userEmail = userEmail;
    }

    /**
     * 获取URL路径
     *
     * @return urlPath
     */
    public String getUrlPath() {
        return urlPath;
    }

    /**
     * 设置URL路径
     *
     * @param urlPath URL路径（主键）
     */
    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    /**
     * 获取页面标题
     *
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * 设置页面标题
     *
     * @param title 页面标题（建议60字符内）
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 获取页面描述
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置页面描述
     *
     * @param description 页面描述（建议160字符内）
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取页面关键词
     *
     * @return keywords
     */
    public String getKeywords() {
        return keywords;
    }

    /**
     * 设置页面关键词
     *
     * @param keywords 页面关键词（逗号分隔）
     */
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    /**
     * 获取邮箱
     *
     * @return userEmail
     */
    public String getUserEmail() {
        return userEmail;
    }
    /**
     * 设置邮箱
     *
     * @param userEmail 邮箱
     */
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    /**
     * 获取页面纯文本内容
     *
     * @return contentText
     */
    public String getContentText() {
        return contentText;
    }

    /**
     * 设置页面纯文本内容
     *
     * @param contentText 页面纯文本内容（供爬虫提取）
     */
    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    /**
     * 获取页面HTML内容
     *
     * @return contentHtml
     */
    public String getContentHtml() {
        return contentHtml;
    }

    /**
     * 设置页面HTML内容
     *
     * @param contentHtml 页面HTML内容（供爬虫渲染）
     */
    public void setContentHtml(String contentHtml) {
        this.contentHtml = contentHtml;
    }

    /**
     * 获取结构化数据
     *
     * @return structuredData
     */
    public String getStructuredData() {
        return structuredData;
    }

    /**
     * 设置结构化数据
     *
     * @param structuredData 结构化数据（JSON-LD格式）
     */
    public void setStructuredData(String structuredData) {
        this.structuredData = structuredData;
    }

    /**
     * 获取Open Graph标题
     *
     * @return ogTitle
     */
    public String getOgTitle() {
        return ogTitle;
    }

    /**
     * 设置Open Graph标题
     *
     * @param ogTitle Open Graph标题
     */
    public void setOgTitle(String ogTitle) {
        this.ogTitle = ogTitle;
    }

    /**
     * 获取Open Graph描述
     *
     * @return ogDescription
     */
    public String getOgDescription() {
        return ogDescription;
    }

    /**
     * 设置Open Graph描述
     *
     * @param ogDescription Open Graph描述
     */
    public void setOgDescription(String ogDescription) {
        this.ogDescription = ogDescription;
    }

    /**
     * 获取Open Graph图片URL
     *
     * @return ogImage
     */
    public String getOgImage() {
        return ogImage;
    }

    /**
     * 设置Open Graph图片URL
     *
     * @param ogImage Open Graph图片URL
     */
    public void setOgImage(String ogImage) {
        this.ogImage = ogImage;
    }

    /**
     * 获取Open Graph类型
     *
     * @return ogType
     */
    public String getOgType() {
        return ogType;
    }

    /**
     * 设置Open Graph类型
     *
     * @param ogType Open Graph类型（website/article等）
     */
    public void setOgType(String ogType) {
        this.ogType = ogType;
    }

    /**
     * 获取规范化URL
     *
     * @return canonicalUrl
     */
    public String getCanonicalUrl() {
        return canonicalUrl;
    }

    /**
     * 设置规范化URL
     *
     * @param canonicalUrl 规范化URL
     */
    public void setCanonicalUrl(String canonicalUrl) {
        this.canonicalUrl = canonicalUrl;
    }

    /**
     * 获取Robots指令
     *
     * @return robots
     */
    public String getRobots() {
        return robots;
    }

    /**
     * 设置Robots指令
     *
     * @param robots Robots指令（index/follow等）
     */
    public void setRobots(String robots) {
        this.robots = robots;
    }

    /**
     * 获取Sitemap优先级
     *
     * @return sitemapPriority
     */
    public BigDecimal getSitemapPriority() {
        return sitemapPriority;
    }

    /**
     * 设置Sitemap优先级
     *
     * @param sitemapPriority Sitemap优先级（0.0-1.0）
     */
    public void setSitemapPriority(BigDecimal sitemapPriority) {
        this.sitemapPriority = sitemapPriority;
    }

    /**
     * 获取Sitemap更新频率
     *
     * @return sitemapChangefreq
     */
    public String getSitemapChangefreq() {
        return sitemapChangefreq;
    }

    /**
     * 设置Sitemap更新频率
     *
     * @param sitemapChangefreq Sitemap更新频率（always/hourly/daily/weekly/monthly/yearly/never）
     */
    public void setSitemapChangefreq(String sitemapChangefreq) {
        this.sitemapChangefreq = sitemapChangefreq;
    }

    /**
     * 获取状态
     *
     * @return status
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置状态
     *
     * @param status 状态：1-启用 0-停用
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 获取软删除标记
     *
     * @return isDeleted
     */
    public Integer getIsDeleted() {
        return isDeleted;
    }

    /**
     * 设置软删除标记
     *
     * @param isDeleted 软删除标记：0-未删除 1-已删除
     */
    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    /**
     * 获取创建时间
     *
     * @return createdAt
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置创建时间
     *
     * @param createdAt 创建时间
     */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 获取更新时间
     *
     * @return updatedAt
     */
    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 设置更新时间
     *
     * @param updatedAt 更新时间
     */
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 获取最后一次被爬虫抓取时间
     *
     * @return lastCrawledAt
     */
    public Date getLastCrawledAt() {
        return lastCrawledAt;
    }

    /**
     * 设置最后一次被爬虫抓取时间
     *
     * @param lastCrawledAt 最后一次被爬虫抓取时间
     */
    public void setLastCrawledAt(Date lastCrawledAt) {
        this.lastCrawledAt = lastCrawledAt;
    }

    /**
     * 获取被爬虫抓取次数
     *
     * @return crawlCount
     */
    public Integer getCrawlCount() {
        return crawlCount;
    }

    /**
     * 设置被爬虫抓取次数
     *
     * @param crawlCount 被爬虫抓取次数
     */
    public void setCrawlCount(Integer crawlCount) {
        this.crawlCount = crawlCount;
    }
}
