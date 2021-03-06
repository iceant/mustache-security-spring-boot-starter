package com.pointcx.spring.mustache.security.internal;

import com.pointcx.spring.mustache.security.DomainObjectResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.TypedValue;

import static com.samskivert.mustache.Template.NO_FETCHER_FOUND;

public class MustacheVariableFetcherPropertyAccessor implements org.springframework.expression.PropertyAccessor {

    private ApplicationContext applicationContext;
    private Object mustacheContext;

    public MustacheVariableFetcherPropertyAccessor(ApplicationContext context, Object mustacheContext) {
        this.applicationContext = context;
        this.mustacheContext = mustacheContext;
    }

    @Override
    public Class<?>[] getSpecificTargetClasses() {
        return null;
    }

    @Override
    public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
        DomainObjectResolver domainObjectResolver = applicationContext.getBean(DomainObjectResolver.class);
        try {
            Object value = domainObjectResolver.resolve(applicationContext, mustacheContext, name);
            return value != NO_FETCHER_FOUND;
        }catch (Exception err){}
        return false;
    }

    @Override
    public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
        DomainObjectResolver domainObjectResolver = applicationContext.getBean(DomainObjectResolver.class);
        Object value = domainObjectResolver.resolve(applicationContext, mustacheContext, name);
        return new TypedValue(value);
    }

    @Override
    public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
        return false;
    }

    @Override
    public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
        return;
    }
}
