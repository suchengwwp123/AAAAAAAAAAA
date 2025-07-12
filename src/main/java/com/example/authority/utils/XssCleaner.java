package com.example.authority.utils;


import com.example.authority.config.properties.XssProperties;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

/**
 * XSS 清洗工具（根据配置决定是否清洗、清洗方式）
 */
@Component
public class XssCleaner {

    private static XssProperties properties;

    @Autowired
    public XssCleaner(XssProperties props) {
        XssCleaner.properties = props;
    }

    public static String clean(String input) {
        if (input == null) return null;

        if (!properties.isEnabled()) return input;

        return switch (properties.getMode()) {
            case CLEAN -> Jsoup.clean(input, Safelist.relaxed()
                    .addTags("span", "div", "section", "article", "style", "img", "video", "audio", "iframe")
                    .addAttributes(":all", "class", "style", "src", "href", "title", "width", "height"));
            case ESCAPE -> HtmlUtils.htmlEscape(input);
        };
    }
}
