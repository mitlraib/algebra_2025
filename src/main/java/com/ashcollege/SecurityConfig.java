package com.ashcollege;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * הגדרת אבטחה בסיסית
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors();
        http.csrf().disable();

        http.authorizeRequests()
                // פתוח:
                .antMatchers("/api/register", "/api/login").permitAll()
                // נתיבים למנהלים בלבד:
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                // כל שאר הנתיבים דורשים התחברות (כלשהי)
                .anyRequest().authenticated()
                .and()
                .formLogin().disable();
    }
}
