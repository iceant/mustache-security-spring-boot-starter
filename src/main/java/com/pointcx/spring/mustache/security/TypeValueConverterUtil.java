package com.pointcx.spring.mustache.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class TypeValueConverterUtil {

    private static Map<Class<?>, Function<String, ?>> converterFunctionMap = new ConcurrentHashMap<>();

    static {
        converterFunctionMap.put(String.class, new Function<String, String>() {
            @Override
            public String apply(String s) {
                return s;
            }
        });

        converterFunctionMap.put(Integer.class, new Function<String, Integer>() {
            @Override
            public Integer apply(String s) {
                return Integer.parseInt(s);
            }
        });
        converterFunctionMap.put(int.class, new Function<String, Integer>() {
            @Override
            public Integer apply(String s) {
                return Integer.parseInt(s);
            }
        });

        converterFunctionMap.put(Float.class, new Function<String, Float>() {
            @Override
            public Float apply(String s) {
                return Float.parseFloat(s);
            }
        });
        converterFunctionMap.put(float.class, new Function<String, Float>() {
            @Override
            public Float apply(String s) {
                return Float.parseFloat(s);
            }
        });

        converterFunctionMap.put(Double.class, new Function<String, Double>() {
            @Override
            public Double apply(String s) {
                return Double.parseDouble(s);
            }
        });
        converterFunctionMap.put(double.class, new Function<String, Double>() {
            @Override
            public Double apply(String s) {
                return Double.parseDouble(s);
            }
        });

    }

    public static Object as(String paramValue, Class<?> paramType) {
        Function<String, ?> fn = converterFunctionMap.get(paramType);
        if(fn!=null){
            return fn.apply(paramValue);
        }
        return paramValue;
    }
}
