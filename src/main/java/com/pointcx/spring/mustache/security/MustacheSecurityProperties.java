package com.pointcx.spring.mustache.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.mustache.security")
public class MustacheSecurityProperties {
    public static final String DEFAULT_PREFIX = "sec:";

    private String prefix;

    public String getPrefix() {
        return prefix==null?DEFAULT_PREFIX:prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
