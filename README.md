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


{{{sec:csrfInput}}}
{{{sec:csrfMetaTags}}}


{{#sec:@webSecurity.check(authentication,request)}}<li>@webSecurity.check(authentication,request){{/sec:@webSecurity.check(authentication,request)}}
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