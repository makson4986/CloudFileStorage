package com.makson.cloudfilestorage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import java.time.Duration;

@Configuration
public class SessionConfig {
    @Bean
    public CookieSerializer cookieSerializer(@Value("${spring.session.timeout}") Duration sessionTimeOut) {
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setCookieName("JSESSIONID");
        cookieSerializer.setCookieMaxAge((int) sessionTimeOut.getSeconds());
        cookieSerializer.setCookiePath("/");
        return cookieSerializer;
    }
}
