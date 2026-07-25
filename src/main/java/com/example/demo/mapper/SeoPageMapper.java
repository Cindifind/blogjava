package com.example.demo.mapper;

import com.example.demo.model.SeoPage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SeoPageMapper {

    @Insert("INSERT INTO seo_pages(url_path, title, description, keywords, content_text, content_html, " +
            "structured_data, og_title, og_description, og_image, og_type, canonical_url, robots, " +
            "sitemap_priority, sitemap_changefreq, status, is_deleted, last_crawled_at, crawl_count, user_email) " +
            "VALUES(#{urlPath}, #{title}, #{description}, #{keywords}, #{contentText}, #{contentHtml}, " +
            "#{structuredData}, #{ogTitle}, #{ogDescription}, #{ogImage}, #{ogType}, #{canonicalUrl}, " +
            "#{robots}, #{sitemapPriority}, #{sitemapChangefreq}, #{status}, #{isDeleted}, " +
            "#{lastCrawledAt}, #{crawlCount}, #{userEmail})")
    int insertSeoPage(SeoPage seoPage);

    @Select("SELECT * FROM seo_pages WHERE url_path = #{urlPath}")
    @Result(property = "urlPath", column = "url_path")
    @Result(property = "contentText", column = "content_text")
    @Result(property = "contentHtml", column = "content_html")
    @Result(property = "structuredData", column = "structured_data")
    @Result(property = "ogTitle", column = "og_title")
    @Result(property = "ogDescription", column = "og_description")
    @Result(property = "ogImage", column = "og_image")
    @Result(property = "ogType", column = "og_type")
    @Result(property = "canonicalUrl", column = "canonical_url")
    @Result(property = "sitemapPriority", column = "sitemap_priority")
    @Result(property = "sitemapChangefreq", column = "sitemap_changefreq")
    @Result(property = "isDeleted", column = "is_deleted")
    @Result(property = "createdAt", column = "created_at")
    @Result(property = "updatedAt", column = "updated_at")
    @Result(property = "lastCrawledAt", column = "last_crawled_at")
    @Result(property = "crawlCount", column = "crawl_count")
    SeoPage selectSeoPageByUrlPath(String urlPath);

    @Update("UPDATE seo_pages SET title = #{title}, description = #{description}, keywords = #{keywords}, " +
            "content_text = #{contentText}, content_html = #{contentHtml}, structured_data = #{structuredData}, " +
            "og_title = #{ogTitle}, og_description = #{ogDescription}, og_image = #{ogImage}, og_type = #{ogType}, " +
            "canonical_url = #{canonicalUrl}, robots = #{robots}, sitemap_priority = #{sitemapPriority}, " +
            "sitemap_changefreq = #{sitemapChangefreq}, status = #{status}, is_deleted = #{isDeleted}, " +
            "last_crawled_at = #{lastCrawledAt}, crawl_count = #{crawlCount} WHERE url_path = #{urlPath}")
    @Result(property = "urlPath", column = "url_path")
    @Result(property = "contentText", column = "content_text")
    @Result(property = "contentHtml", column = "content_html")
    @Result(property = "structuredData", column = "structured_data")
    @Result(property = "ogTitle", column = "og_title")
    @Result(property = "ogDescription", column = "og_description")
    @Result(property = "ogImage", column = "og_image")
    @Result(property = "ogType", column = "og_type")
    @Result(property = "canonicalUrl", column = "canonical_url")
    @Result(property = "sitemapPriority", column = "sitemap_priority")
    @Result(property = "sitemapChangefreq", column = "sitemap_changefreq")
    @Result(property = "isDeleted", column = "is_deleted")
    @Result(property = "createdAt", column = "created_at")
    @Result(property = "updatedAt", column = "updated_at")
    @Result(property = "lastCrawledAt", column = "last_crawled_at")
    @Result(property = "crawlCount", column = "crawl_count")
    int updateSeoPage(SeoPage seoPage);
    @Select("SELECT * FROM seo_pages WHERE user_email = #{userEmail}")
    @Result(property = "urlPath", column = "url_path")
    @Result(property = "contentText", column = "content_text")
    @Result(property = "contentHtml", column = "content_html")
    @Result(property = "structuredData", column = "structured_data")
    @Result(property = "ogTitle", column = "og_title")
    @Result(property = "ogDescription", column = "og_description")
    @Result(property = "ogImage", column = "og_image")
    @Result(property = "ogType", column = "og_type")
    @Result(property = "canonicalUrl", column = "canonical_url")
    @Result(property = "sitemapPriority", column = "sitemap_priority")
    @Result(property = "sitemapChangefreq", column = "sitemap_changefreq")
    @Result(property = "isDeleted", column = "is_deleted")
    @Result(property = "createdAt", column = "created_at")
    @Result(property = "updatedAt", column = "updated_at")
    @Result(property = "lastCrawledAt", column = "last_crawled_at")
    @Result(property = "crawlCount", column = "crawl_count")
    List<SeoPage> selectSeoPageByUserEmail(String userEmail);

}
