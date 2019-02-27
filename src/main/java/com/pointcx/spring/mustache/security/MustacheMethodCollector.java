package com.pointcx.spring.mustache.security;

import com.samskivert.mustache.Mustache.VariableFetcher;
import org.springframework.boot.autoconfigure.mustache.MustacheEnvironmentCollector;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MustacheMethodCollector extends MustacheEnvironmentCollector {

    private static final Map<String, ClassMethodVariableFetcher> variableFetcherRegistry = new ConcurrentHashMap<>();

    static final Pattern FETCHER_MATCH_PATTERN = Pattern.compile("([^\\(].*)\\(([^\\)].*)\\)");

    static String parseTokenForMethodName(String token) {
        Matcher matcher = FETCHER_MATCH_PATTERN.matcher(token);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public MustacheMethodCollector mapVariableFetcher(String prefixName, Class handlerClass, String methodName, Class ... params) throws NoSuchMethodException{
        if (variableFetcherRegistry.containsKey(prefixName)) return this;/*only register once*/
        Method method = handlerClass.getDeclaredMethod(methodName, params);
        try {
            variableFetcherRegistry.put(prefixName, new ClassMethodVariableFetcher(handlerClass.newInstance(), method));
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public VariableFetcher createFetcher(Object ctx, String name) {
        VariableFetcher fetcher = super.createFetcher(ctx, name);
        if (fetcher != null) {
            return fetcher;
        }

        String methodName = parseTokenForMethodName(name);
        fetcher = variableFetcherRegistry.get(methodName);
        if (fetcher != null) {
            return fetcher;
        }

        return null;
    }

}
