package com.ashcollege;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors();

        // 1) כיבוי CSRF
        http.csrf().disable();

        // 2) הגדרת הרשאות בגישה הקלאסית:
        http.authorizeRequests()
                .antMatchers("/api/register", "/api/login").permitAll() // פתוח לכולם
                .anyRequest().authenticated() // השאר מחייב התחברות
                .and()
                .formLogin().disable(); // אין טופס לוגין ברירת מחדל
    }
}
