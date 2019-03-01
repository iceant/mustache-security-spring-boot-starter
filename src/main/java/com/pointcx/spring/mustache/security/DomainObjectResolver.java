package com.pointcx.spring.mustache.security;

import org.springframework.context.ApplicationContext;

import java.util.NoSuchElementException;

/**
 * Back to spring side, we need to resolve the value for parameters in the expression, for example, 'this'
 * Mustache provided VariableFetcher to do this. so, we need to go back to mustache side, use the VariableFetcher to resolve these variables.
 * This interface used for this purpose.
 */
public interface DomainObjectResolver {
    default Object resolve(ApplicationContext applicationContext, Object mustacheContext, String express)throws NoSuchElementException {
        throw new NoSuchElementException(express);
    }
}
