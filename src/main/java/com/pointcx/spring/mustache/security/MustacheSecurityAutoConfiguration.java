package com.pointcx.spring.mustache.security;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Mustache.Collector;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.mustache.MustacheAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;

@Configuration
@ConditionalOnClass(value = {Mustache.Compiler.class, Authentication.class})
@ConditionalOnWebApplication(type = Type.SERVLET)
@EnableConfigurationProperties(value = {MustacheSecurityProperties.class})
@AutoConfigureAfter(MustacheAutoConfiguration.class)
public class MustacheSecurityAutoConfiguration {

    private final MustacheSecurityProperties properties;
    private final Environment environment;
    private final ApplicationContext applicationContext;

    public MustacheSecurityAutoConfiguration(
            MustacheSecurityProperties properties
            , Environment environment
            , ApplicationContext applicationContext
    ) {
        this.properties = properties;
        this.environment = environment;
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConditionalOnMissingBean
    public Collector mustacheCollector(){
        Collector collector =  new SpringSecurityExpressionCollector(applicationContext, properties);
        return collector;
    }

    @Bean
    public MustacheCompilerBeanPostProcessor mustacheCompilerBeanPostProcessor(){
        return new MustacheCompilerBeanPostProcessor();
    }
}
