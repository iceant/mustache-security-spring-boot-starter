package com.pointcx.spring.mustache.security;

import com.samskivert.mustache.Mustache.VariableFetcher;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

class ClassMethodVariableFetcher implements VariableFetcher {
    private final Object handler;
    private final Method method;

    public ClassMethodVariableFetcher(Object handler, Method method) {
        this.handler = handler;
        this.method = method;
    }

    @Override
    public Object get(Object ctx, String name) throws Exception {
        Matcher matcher = MustacheMethodCollector.FETCHER_MATCH_PATTERN.matcher(name);
        if(matcher.find()){
            String params = matcher.group(2); /* fetch params in function(`params`)*/

            List<String> paramValues = new LinkedList<>();
            for(String param : params.split(",")){
                paramValues.add(param);
            }

            Class<?>[] paramTypes = method.getParameterTypes();
            Object[] args = new Object[paramTypes.length];
            for(int i=0; i<paramTypes.length; i++){
                Class<?> paramType = paramTypes[i];
                String paramValue = paramValues.get(i);
                args[i] = TypeValueConverterUtil.as(paramValue, paramType);
            }

            return method.invoke(handler, args);
        }
        return null;
    }
}
