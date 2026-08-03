package com.example.demo.server;

import com.example.demo.mapper.SeoPageMapper;
import com.example.demo.model.SeoPage;
import com.example.demo.util.SeoPageHtmlUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.beans.PropertyDescriptor;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SeoPageServer {
    private final SeoPageMapper seoPageMapper;
    private final UserInfoServer UserInfoServer;

    public SeoPageServer(SeoPageMapper seoPageMapper, UserInfoServer UserInfoServer) {
        this.seoPageMapper = seoPageMapper;
        this.UserInfoServer = UserInfoServer;
    }

    public String getSeoPageByUrlPathToFullHtml(String urlPath) {
        SeoPage seoPage = seoPageMapper.selectSeoPageByUrlPath(urlPath);
        if (seoPage != null) {
            Integer crawlCount = seoPage.getCrawlCount();
            seoPage.setCrawlCount(crawlCount + 1);
            seoPage.setLastCrawledAt(new Date());
            seoPageMapper.updateSeoPage(seoPage);
            return SeoPageHtmlUtil.generateFullHtml(seoPage);
        }
        return null; // Changed from return null; to return null;
    }
    public String getSeoPageByUrlPathToSitemapXml(String urlPath) {
        SeoPage seoPage = seoPageMapper.selectSeoPageByUrlPath(urlPath);
        if (seoPage != null) {
            Integer crawlCount = seoPage.getCrawlCount();
            seoPage.setCrawlCount(crawlCount + 1);
            seoPage.setLastCrawledAt(new Date());
            seoPageMapper.updateSeoPage(seoPage);
            return SeoPageHtmlUtil.generateSitemapXml(seoPage);
        }
        return null; // Changed from return null; to return null;
    }
    public int insertSeoPage(SeoPage seoPage, HttpServletRequest request) {
        seoPage.setCrawlCount(0);
        String emailByAccessToken = getEmailByAccessToken(request);
        if (emailByAccessToken == null) {
            return 0;
        }
        seoPage.setUserEmail(emailByAccessToken);
        return seoPageMapper.insertSeoPage(seoPage);
    }
    public int updateSeoPage(SeoPage seoPage, HttpServletRequest request) {
        // 1. 参数校验
        if (seoPage == null || seoPage.getUrlPath() == null) {
            return 0;
        }

        // 2. 查询现有记录
        String urlPath = seoPage.getUrlPath();
        SeoPage existingSeoPage = seoPageMapper.selectSeoPageByUrlPath(urlPath);
        String emailByAccessToken = getEmailByAccessToken(request);
        if (existingSeoPage == null) {
            // 如果不存在，应该插入还是抛出异常？建议根据业务决定
            return 0;
        }
        if (!existingSeoPage.getUserEmail().equals(emailByAccessToken)) {
            return 0;
        }
        // 3. 使用传入的非空属性覆盖已存在的记录
        // 注意：这里可以指定哪些属性可以被更新
        BeanUtils.copyProperties(seoPage, existingSeoPage,
                getNullAndIgnoredPropertyNames(seoPage)); // 忽略空属性和特定属性

        // 4. 执行更新（确保有主键）
        existingSeoPage.setCrawlCount(existingSeoPage.getCrawlCount()+1);
        return seoPageMapper.updateSeoPage(existingSeoPage);
    }
    /**
     * 获取需要忽略的属性名
     */
    public List<SeoPage> selectByUserEmail(HttpServletRequest request) {
        String emailByAccessToken = getEmailByAccessToken(request);
        return seoPageMapper.selectSeoPageByUserEmail(emailByAccessToken);
    }
    public int deleteSeoPageByUrlPath(String urlPath, HttpServletRequest request) {
        String emailByAccessToken = getEmailByAccessToken(request);
        if (emailByAccessToken == null) {
            return 0;
        }
        return seoPageMapper.deleteSeoPageByUrlPathAndUserEmail(urlPath, emailByAccessToken);
    }
    private static String[] getNullAndIgnoredPropertyNames(Object source) {
        Set<String> ignoredNames = new HashSet<>();

        // 忽略 null 属性
        BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                ignoredNames.add(pd.getName());
            }
        }
        // 额外忽略不应该被更新的属性（如 id、createTime、createUser 等）
        ignoredNames.add("id");
        ignoredNames.add("createTime");
        ignoredNames.add("createUser");
        // 可能还需要忽略 updateTime 等自动维护的字段

        return ignoredNames.toArray(new String[0]);
    }
    private String getEmailByAccessToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        token = token.replace("Bearer ", "");
        return UserInfoServer.getEmailByAccessToken(token);
    }
}
