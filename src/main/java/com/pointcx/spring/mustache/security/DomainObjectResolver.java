package com.pointcx.spring.mustache.security;

import org.springframework.context.ApplicationContext;

import java.util.NoSuchElementException;

/**
 * Template are evaluate in mustache side first, then the mustache Template find there are some tags it don't understand,
 * for example, {{sec:xxx}}, it will ask the Mustache.Collector to create a VariableFetcher to resolve these tags.
 * We hacked Mustache.Compiler to provide custom Collector in it.
 * The collector will create a SpringSecurityExpressionVariableFetcher to resolve these parameters.
 * spring's org.springframework.expression.Expression used in backend.
 * Back to spring side, we need to resolve the value for parameters in the expression, for example, 'this'
 * Mustache provided VariableFetcher to do this. so, we need to go back to mustache side, use the VariableFetcher to resolve these variables.
 * This interface used for this purpose.
 */
public interface DomainObjectResolver {
    default Object resolve(ApplicationContext applicationContext, Object mustacheContext, String express)throws NoSuchElementException {
        throw new NoSuchElementException(express);
    }
}
