package com.pointcx.spring.mustache.security.internal;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Mustache.Collector;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class MustacheCompilerBeanPostProcessor implements BeanPostProcessor {

    @Autowired
    Collector collector;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof Mustache.Compiler){
            Mustache.Compiler compiler = (Mustache.Compiler) bean;
            return compiler.withCollector(collector);
        }
        return bean;
    }
}
