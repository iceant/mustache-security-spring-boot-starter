package com.pointcx.spring.mustache.security;

import org.springframework.context.ApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ParseException;
import org.springframework.security.access.expression.ExpressionUtils;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.WebInvocationPrivilegeEvaluator;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class SecurityUtil {

    /**
     *
     * @param applicationContext
     * @return
     */
    public static SecurityExpressionHandler<FilterInvocation> getExpressionHandler(ApplicationContext applicationContext) {

        final Map<String, SecurityExpressionHandler> expressionHandlers =
                applicationContext.getBeansOfType(SecurityExpressionHandler.class);

        for (SecurityExpressionHandler handler : expressionHandlers.values()) {
            final Class<?> clazz = GenericTypeResolver.resolveTypeArgument(handler.getClass(), SecurityExpressionHandler.class);
            if (FilterInvocation.class.equals(GenericTypeResolver.resolveTypeArgument(handler.getClass(), SecurityExpressionHandler.class))) {
                return handler;
            }
        }

        throw new RuntimeException(
                "No visible SecurityExpressionHandler instance could be found in the application " +
                        "context. There must be at least one in order to support expressions in Spring Security " +
                        "authorization queries.");
    }

    /**
     *
     * @param applicationContext
     * @return
     */
    public static WebInvocationPrivilegeEvaluator getPrivilegeEvaluator(ApplicationContext applicationContext){
        final Map<String, WebInvocationPrivilegeEvaluator> privilegeEvaluators =
                applicationContext.getBeansOfType(WebInvocationPrivilegeEvaluator.class);

        if (privilegeEvaluators.size() == 0) {
            throw new RuntimeException(
                    "No visible WebInvocationPrivilegeEvaluator instance could be found in the application " +
                            "context. There must be at least one in order to support URL access checks in " +
                            "Spring Security authorization queries.");
        }

        return (WebInvocationPrivilegeEvaluator) privilegeEvaluators.values().toArray()[0];
    }

    public static HttpServletRequest getCurrentHttpRequest(){
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
            return request;
        }
        return null;
    }

    public static CsrfToken getCsrfToken(){
        CsrfToken csrfToken = (CsrfToken) getCurrentHttpRequest().getAttribute("_csrf");
        return csrfToken;
    }

    public static HttpServletResponse getCurrentHttpResponse(){
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletResponse response = ((ServletRequestAttributes)requestAttributes).getResponse();
            return response;
        }
        return null;
    }

    public static Authentication getAuthentication(){
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if(securityContext==null)return null;
        return securityContext.getAuthentication();
    }

    static final String CSRF_INPUT_PATTERN = "<input type=\"hidden\" name=\"%s\" value=\"%s\">";
    public static String getCsrfInput() {
        CsrfToken token = getCsrfToken();
        if(token==null){
            return "<input type=\"hidden\" name=\"_csrf\" value=\"\">";
        }
        return String.format(CSRF_INPUT_PATTERN, token.getParameterName(), token.getToken());
    }

    static final String CSRF_META_TAG_PATTERN = "<meta name=\"%s\" content=\"%s\"/><meta name=\"_csrf_header\" content=\"%s\"/>";
    public static String getCsrfMetaTag(){
        CsrfToken token = getCsrfToken();
        String csrfParameterName = "";
        String csrfHeaderName="";
        String csrfToken = "";
        if(token!=null){
            csrfParameterName = token.getParameterName();
            csrfHeaderName = token.getHeaderName();
            csrfToken = token.getToken();
        }
        return String.format(CSRF_META_TAG_PATTERN, csrfParameterName, csrfToken, csrfHeaderName);
    }

    private static class ServletFilterChainHolder {
        // This class will only be loaded when the Servlet API is present, thus avoiding class-loading errors for
        // the FilterChain class in WebFlux apps

        private static final FilterChain DUMMY_CHAIN = new FilterChain() {
            public void doFilter(ServletRequest request, ServletResponse response)
                    throws IOException, ServletException {
                throw new UnsupportedOperationException();
            }
        };
    }

    public static final class MvcAuthUtils{
        public static boolean authorizeUsingAccessExpressionMvc(final ApplicationContext context,
                                                                final String accessExpression,
                                                                final Authentication authentication) {

            final SecurityExpressionHandler<FilterInvocation> handler = getExpressionHandler(context);

            Expression expressionObject = null;
            try {
                expressionObject = handler.getExpressionParser().parseExpression(accessExpression);
            } catch (ParseException e) {
                throw new RuntimeException(
                        "An error happened trying to parse Spring Security access expression \"" +
                                accessExpression + "\"", e);
            }

            final HttpServletRequest request = getCurrentHttpRequest();
            final HttpServletResponse response = getCurrentHttpResponse();

            final FilterInvocation filterInvocation = new FilterInvocation(request, response, ServletFilterChainHolder.DUMMY_CHAIN);
            final EvaluationContext evaluationContext = handler.createEvaluationContext(authentication, filterInvocation);
            return ExpressionUtils.evaluateAsBoolean(expressionObject, evaluationContext);
        }

        public static boolean authorizeUsingUrlCheckMvc(final ApplicationContext context,
                                                        final String url,
                                                        final String method,
                                                        final Authentication authentication)
        {
            String contextPath = getCurrentHttpRequest().getContextPath();
            return getPrivilegeEvaluator(context).isAllowed(contextPath, url, method, authentication);
        }
    }
}
