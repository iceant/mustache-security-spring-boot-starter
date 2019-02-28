package com.pointcx.spring.mustache.security.internal;

import com.pointcx.spring.mustache.security.MustacheSecurityProperties;
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
        if(name.equalsIgnoreCase("this") || name.equalsIgnoreCase(".")) return THIS_FETCHER;
        VariableFetcher parentFetcher = super.createFetcher(ctx, name);
        CompositVariableFetcher fetcher = new CompositVariableFetcher();
        if (parentFetcher != null) {
            fetcher.addVariableFetcher(parentFetcher);
        }

        if (name.startsWith(properties.getPrefix())) {
            VariableFetcher securityExpressFetcher = new SpringSecurityExpressionVariableFetcher(applicationContext, properties);
            fetcher.addVariableFetcher(securityExpressFetcher);
        }
        if(fetcher.size()==0) return null;
        return fetcher;
    }
}
