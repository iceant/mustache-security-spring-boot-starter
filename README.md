# Features
- Support all the built-in expressions
    - [spring-security el-access](https://docs.spring.io/spring-security/site/docs/4.2.x/reference/html/el-access.html)
- Support Referring to Beans in Web Security Expressions
- Support customized SecurityExpressionRoot for WebSecurityExpressionRoot    
    
# Examples
```text
{{#sec:hasRole('ADMIN')}}<li>ADMIN CONTENT</li>{{/sec:hasRole('ADMIN')}}
{{#sec:hasRole('ADMIN') and hasRole('USER')}}<li>ADMIN & USER CONTENT</li>{{/sec:hasRole('ADMIN') and hasRole('USER')}}
{{#sec:hasAnyRole('ADMIN', 'USER')}}<li>ADMIN OR USER CONTENT</li>{{/sec:hasAnyRole('ADMIN', 'USER')}}
{{#sec:hasRole('USER')}}<li>USER CONTENT</li>{{/sec:hasRole('USER')}}
{{#sec:isAnonymous()}}<li>isAnonymous</li>{{/sec:isAnonymous()}}{{^sec:isAnonymous()}}<li>isAnonymous=false</li>{{/sec:isAnonymous()}}
{{#sec:isRememberMe()}}<li>isRememberMe</li>{{/sec:isRememberMe()}}{{^sec:isRememberMe()}}<li>isRememberMe=false</li>{{/sec:isRememberMe()}}
{{#sec:isAuthenticated()}}<li>isAuthenticated</li>{{/sec:isAuthenticated()}}{{^sec:isAuthenticated()}}<li>isAuthenticated=false</li>{{/sec:isAuthenticated()}}
{{#sec:isFullyAuthenticated()}}<li>isFullyAuthenticated</li>{{/sec:isFullyAuthenticated()}}{{^sec:isFullyAuthenticated()}}<li>isFullyAuthenticated=false</li>{{/sec:isFullyAuthenticated()}}
{{#sec:principal}}<li>principal={{username}}{{/sec:principal}}
{{#sec:authentication}}<li>authentication={{.}}{{/sec:authentication}}
{{#sec:permitAll}}<li>permitAll</li>{{/sec:permitAll}}
{{#sec:denyAll}}<li>denyAll</li>{{/sec:denyAll}}{{^sec:denyAll}}<li>denyAll=false</li>{{/sec:denyAll}}
{{#sec:hasIpAddress('0:0:0:0:0:0:0:1')}}<li>hasIpAddress('0:0:0:0:0:0:0:1')</li>{{/sec:hasIpAddress('0:0:0:0:0:0:0:1')}}{{^sec:hasIpAddress('192.168.2.1')}}<li>hasIpAddress('192.168.2.1')=false</li>{{/sec:hasIpAddress('192.168.2.1')}}

{{#sec:hasPermission(user, 'read')}}READ PERMISSION ASSIGNED TO {{user.name}}{{/sec:hasPermission(user, 'read)'}}
{{#sec:hasPermission(1, 'com.xxx.Menu', 'read')}}Current user has 'read' permission for 'com.xxx.Menu' on id '1' {{/sec:sec:hasPermission(1, 'com.xxx.Menu', 'read')}}

{{#sec:hasPermission(foo, 'write') or hasPermission(foo, 'read')}}
READ OR WRITE PERMISSION ASSIGNED TO '{{#sec:principal}}{{username}}{{/sec:principal}}'
{{/sec:hasPermission(foo, 'write') or hasPermission(foo, 'read')}}

{{{sec:csrfInput}}}
{{{sec:csrfMetaTags}}}


{{#sec:@webSecurity.check(authentication,request)}}<li>@webSecurity.check(authentication,request){{/sec:@webSecurity.check(authentication,request)}}

{{#users}}
  {{#sec:hasPermission(this, 'read')}}
    {{this}} // this is user in the users collection
  {{/sec:hasPermission(this, 'read')}}
{{/users}}

```

- WebSecurity Bean
```java
@Component
public class WebSecurity {
    public boolean check(Authentication authentication, HttpServletRequest request) {
        return true;
    }
}
```

- `hasPermission(targetDomainObject, permission)`
    - `targetDomainObject` use `DomainObjectResolver` to resolve the value
    - `DefaultDomainObjectResolver` is the default implementation, it will check the 'request/session/servlet context/passed in evaluate data' in order to resolve the value
        - for example, if the developer use `{{sec:hasPermission(user, 'read')}}` to check the permission, default resolver will try to get object named 'user' in request.attribute, session.attribute, servletContext.attribute and `context` used in Mustache.compiler().execute(template, **context**);  

- Support `hasPermission(...) or hasPermission(...)`, `hasPermission(...) and hasPermission(...)`

# Usage
- `mvn install` compile, package and install this starter in local maven repository
- add dependency in pom.xml
```xml
<dependency>
    <groupId>com.pointcx</groupId>
    <artifactId>mustache-security-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```
- start to use `{{#sec:xxx}}...{{/sec:xxx}}` tag


# Details
 * Template are evaluate in mustache side first, then the mustache Template find there are some tags it don't understand,
 * for example, {{sec:xxx}}, it will ask the `Mustache.Collector` to create a `VariableFetcher` to resolve these tags. 
 * We hacked `Mustache.Compiler` to provide custom Collector in it. Spring's `BeanPostProcessor` technology used here. Please check `MustacheCompilerBeanPostProcessor` for detail.
 * The collector will create a `CompositVarialeFetcher` include parent's VariableFetcher and custom `SpringSecurityExpressionVariableFetcher` to resolve these parameters.
 * spring's `org.springframework.expression.Expression` used in backend. 
 * Back to spring side, we need to resolve the value for parameters in the expression, for example, 'this'
 * Mustache provided VariableFetcher to do this. so, we need to go back to mustache side, use the VariableFetcher to resolve these variables.
 * DomainObjectResolver used as bridge from spring to mustache to resolve these tokens.
