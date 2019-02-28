package com.pointcx.spring.mustache.security;

import org.springframework.context.ApplicationContext;

import java.util.NoSuchElementException;

public interface DomainObjectResolver {
    default Object resolve(ApplicationContext applicationContext, Object mustacheContext, String express)throws NoSuchElementException {
        throw new NoSuchElementException(express);
    }
}
