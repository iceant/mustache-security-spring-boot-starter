package com.pointcx.spring.mustache.security;

import com.samskivert.mustache.Mustache.VariableFetcher;
import org.springframework.boot.autoconfigure.mustache.MustacheEnvironmentCollector;
import org.springframework.context.ApplicationContext;

public class SpringSecurityExpressionCollector extends MustacheEnvironmentCollector {

    private final ApplicationContext applicationContext;
    private final MustacheSecurityProperties properties;

    public SpringSecurityExpressionCollector(ApplicationContext applicationContext, MustacheSecurityProperties properties) {
        this.applicationContext = applicationContext;
        this.properties = properties;
    }

    @Override
    public VariableFetcher createFetcher(Object ctx, String name) {
        VariableFetcher fetcher = null;
        if(name.startsWith(properties.getPrefix())){
            fetcher = new SpringSecurityExpressionVariableFetcher(applicationContext, properties);
        }
        if(fetcher!=null) return fetcher;

        fetcher =  super.createFetcher(ctx, name);
        if(fetcher!=null) return fetcher;

        return null;
    }
}
