package com.pointcx.spring.mustache.security.internal;

import com.samskivert.mustache.Mustache.VariableFetcher;

import static com.samskivert.mustache.Template.NO_FETCHER_FOUND;

public abstract class DelegateVariableFetcher implements VariableFetcher {
    private VariableFetcher parent;

    public DelegateVariableFetcher() {
    }

    public DelegateVariableFetcher(VariableFetcher parent) {
        this.parent = parent;
    }

    public VariableFetcher getParent() {
        return parent;
    }

    public void setParent(VariableFetcher parent) {
        this.parent = parent;
    }

    @Override
    public Object get(Object ctx, String name) throws Exception {
        Object value = null;
        if(parent!=null){
            value = parent.get(ctx, name);
        }
        if(value==NO_FETCHER_FOUND){
            value =fetch(ctx, name);
        }
        return value;
    }

    protected abstract Object fetch(Object ctx, String name);
}
