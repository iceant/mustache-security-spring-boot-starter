package com.pointcx.spring.mustache.security.internal;

import com.samskivert.mustache.Mustache.VariableFetcher;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.samskivert.mustache.Template.NO_FETCHER_FOUND;

public class CompositVariableFetcher implements VariableFetcher {

    private List<VariableFetcher> variableFetcherList = new CopyOnWriteArrayList<>();

    public CompositVariableFetcher addVariableFetcher(VariableFetcher ... variableFetchers){
        variableFetcherList.addAll(Arrays.asList(variableFetchers));
        return this;
    }

    public int size(){
        return variableFetcherList.size();
    }

    public List<VariableFetcher> getVariableFetchers(){
        return variableFetcherList;
    }

    @Override
    public Object get(Object ctx, String name) throws Exception {
        for(VariableFetcher variableFetcher : variableFetcherList){
            try {
                Object value = variableFetcher.get(ctx, name);
                if (value != NO_FETCHER_FOUND) return value;
            }catch (Exception err){/*ignore it*/}
        }
        return NO_FETCHER_FOUND;
    }
}
