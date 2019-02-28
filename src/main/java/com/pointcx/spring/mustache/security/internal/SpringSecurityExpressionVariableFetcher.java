package com.pointcx.spring.mustache.security.internal;

import com.pointcx.spring.mustache.security.MustacheSecurityProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;

import static com.samskivert.mustache.Template.NO_FETCHER_FOUND;

public class SpringSecurityExpressionVariableFetcher implements com.samskivert.mustache.Mustache.VariableFetcher {
    private final ApplicationContext applicationContext;
    private final MustacheSecurityProperties mustacheSecurityProperties;

    public SpringSecurityExpressionVariableFetcher(ApplicationContext applicationContext, MustacheSecurityProperties mustacheSecurityProperties) {
        this.applicationContext = applicationContext;
        this.mustacheSecurityProperties = mustacheSecurityProperties;
    }

    @Override
    public Object get(Object ctx, String name) throws Exception {
        String prefix = mustacheSecurityProperties.getPrefix();
        if (name.startsWith(prefix)) {
            String express = name.substring(prefix.length());
            Authentication authentication = SecurityUtil.getAuthentication();
            if(express.equalsIgnoreCase("principal")){
                return authentication.getPrincipal();
            }else if(express.equalsIgnoreCase("authentication")){
                return authentication;
            }else if(express.equalsIgnoreCase("csrfInput")){
                return SecurityUtil.getCsrfInput();
            }else if(express.equalsIgnoreCase("csrfMetaTags")){
                return SecurityUtil.getCsrfMetaTag();
            }
            return SecurityUtil.MustacheAuthUtil.authorizeUsingAccessExpression(applicationContext, express, authentication, ctx);
        }
        return NO_FETCHER_FOUND;
    }
}
