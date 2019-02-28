package com.pointcx.spring.mustache.security.internal;

import com.pointcx.spring.mustache.security.DomainObjectResolver;
import com.samskivert.mustache.Mustache.Collector;
import com.samskivert.mustache.Mustache.VariableFetcher;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.util.NoSuchElementException;

import static com.samskivert.mustache.Template.NO_FETCHER_FOUND;

public class DefaultDomainObjectResolver implements DomainObjectResolver {

    Collector collector;

    public DefaultDomainObjectResolver(Collector collector) {
        this.collector = collector;
    }

    /**
     *
     * @param applicationContext
     * @param mustacheContext
     * @param express
     * @return
     */
    public Object resolve(ApplicationContext applicationContext, Object mustacheContext, String express)throws NoSuchElementException {
       HttpServletRequest request = SecurityUtil.getCurrentHttpRequest();
       Object object = request.getAttribute(express);
       if(object==null){
           object = request.getSession().getAttribute(express);
       }
       if(object==null){
           object = request.getServletContext().getAttribute(express);
       }
       if(object==null){
           VariableFetcher fetcher = collector.createFetcher(mustacheContext, express);
           if(fetcher!=null){
               try {
                   object = fetcher.get(mustacheContext, express);
               } catch (Exception e) {}
           }
       }
       if(object==null || object==NO_FETCHER_FOUND) throw new NoSuchElementException(express);
       return object;
    }
}
