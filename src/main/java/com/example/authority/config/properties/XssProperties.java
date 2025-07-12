package com.example.authority.config.properties;

import com.example.authority.enums.XssMode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ConfigurationProperties(prefix = "xss")
public class XssProperties {

    private boolean enabled = true;

    private XssMode mode = XssMode.CLEAN;

    private List<String> excludes = new ArrayList<>();

    private List<String> urlPatterns = List.of("/*");
}
