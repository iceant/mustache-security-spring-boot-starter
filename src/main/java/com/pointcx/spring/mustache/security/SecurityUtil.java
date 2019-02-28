package com.pointcx.spring.mustache.security;

import org.springframework.context.ApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ParseException;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.PermissionEvaluator;
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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

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

    static class PermissionUtil {

        static final String hasPermissionMethodBegin="hasPermission(";
        static final String getHasPermissionMethodEnd = ")";
        static final int hasPermissionMethodBeginLength = hasPermissionMethodBegin.length();

        static class HasPermissionMethodParameter{
            Object domainObject;
            Serializable targetId;
            String targetType;
            Object permission;
            int paramCount=0;

            public HasPermissionMethodParameter() {
            }

            public HasPermissionMethodParameter(Object domainObject, Serializable targetId, String targetType, Object permission) {
                this.domainObject = domainObject;
                this.targetId = targetId;
                this.targetType = targetType;
                this.permission = permission;
            }

            public boolean isTwoParam(){
                return paramCount==2;
            }

            public boolean isThreeParam(){
                return paramCount==3;
            }
        }

        static String removeSingleQuote(String value){
            if(value==null || value.length()<1) return value;
            int start = 0;
            int stop = value.length();
            if(value.charAt(0)=='\''){
                start = 1;
            }
            if(value.charAt(value.length()-1)=='\''){
                stop = value.length()-1;
            }
            return value.substring(start, stop);
        }

        /**
         * - 'name'
         * - name
         * - request.name
         * -
         * @param applicationContext
         * @param ctx
         * @param domainObjectExpress
         * @return
         */
        static Object resolveDomainObject(ApplicationContext applicationContext, Object ctx, String domainObjectExpress){
            DomainObjectResolver domainObjectResolver = applicationContext.getBean(DomainObjectResolver.class);
            return domainObjectResolver.resolve(applicationContext, ctx, domainObjectExpress);
        }

        /**
         *
         * @return
         */
        static HasPermissionMethodParameter parseParameter(ApplicationContext applicationContext, Object ctx, String express){
            String paramContent = express.substring(hasPermissionMethodBeginLength, express.lastIndexOf(getHasPermissionMethodEnd));
            StringTokenizer st = new StringTokenizer(paramContent, ",");
            List<String> params = new ArrayList<>();
            for(;st.hasMoreTokens();){
                params.add(st.nextToken().trim());
            }
            HasPermissionMethodParameter permissionMethodParameter = new HasPermissionMethodParameter();
            if(params.size()==2){
                /*how to define domainObject?*/
                // 1. used as name get from request/context
                //      Object domainObject = request.getAttribute('domainObject');
                //      Object domainObject = ReflectUtil.getFieldValue(ctx, 'domainObject');
                String domainObject = params.get(0);
                permissionMethodParameter.domainObject = resolveDomainObject(applicationContext, ctx, domainObject);
                permissionMethodParameter.permission = removeSingleQuote(params.get(1));
                permissionMethodParameter.paramCount=2;
            }else if(params.size()==3){
                permissionMethodParameter.targetId = removeSingleQuote(params.get(0));
                permissionMethodParameter.targetType = removeSingleQuote(params.get(1));
                permissionMethodParameter.permission = removeSingleQuote(params.get(2));
                permissionMethodParameter.paramCount=3;
            }

            return permissionMethodParameter;

        }

        /**
         * AclPermissionEvaluator used ObjectIdentityRetrievalStrategy to resolve 'domainObject' to ObjectIdentity
         * ObjectIdentityGenerator use 'targetId', 'targetType' to resolve ObjectIdentity
         * AclPermissionEvaluator has resolvePermission(Object permission) method used PermissionFactory to resolve permission
         *
         *
         * @param express
         *  - hasPermission(domainObject, 'read')
         *  - hasPermission(Object targetId, String targetType, Object permission)
         *      - example: hasPermission(1, 'com.example.domain.Message', 'read')
         * @return
         */
        public static boolean hasPermission(ApplicationContext applicationContext, Object ctx, String express) {

            Map<String, PermissionEvaluator> permissionEvaluatorMap = applicationContext.getBeansOfType(PermissionEvaluator.class);

            if(permissionEvaluatorMap==null||permissionEvaluatorMap.size()<1){
                return false;
            }

            if(express.startsWith(hasPermissionMethodBegin)){
                HasPermissionMethodParameter parameter = parseParameter(applicationContext, ctx, express);
                if(parameter.isTwoParam()){
                    for(PermissionEvaluator permissionEvaluator: permissionEvaluatorMap.values()){
                        if(permissionEvaluator.hasPermission(getAuthentication(), parameter.domainObject, parameter.permission)){
                            return true;
                        }
                    }
                }else if(parameter.isThreeParam()){
                    for(PermissionEvaluator permissionEvaluator: permissionEvaluatorMap.values()){
                        if(permissionEvaluator.hasPermission(getAuthentication(), parameter.targetId, parameter.targetType, parameter.permission)){
                            return true;
                        }
                    }
                }
            }

            return false;
        }
    }

    public static class MustacheAuthUtil {

        public static boolean authorizeUsingAccessExpression(ApplicationContext context, String accessExpression, Authentication authentication, Object mustacheContext) {
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
            EvaluationContext evaluationContext = handler.createEvaluationContext(authentication, filterInvocation);

            if(evaluationContext instanceof StandardEvaluationContext){
                StandardEvaluationContext standardEvaluationContext = (StandardEvaluationContext)evaluationContext;
                List<PropertyAccessor> propertyAccessors = standardEvaluationContext.getPropertyAccessors();
                propertyAccessors.add(new MustacheVariableFetcherPropertyAccessor(context, mustacheContext));
            }
            return ExpressionUtils.evaluateAsBoolean(expressionObject, evaluationContext);
        }
    }
}
