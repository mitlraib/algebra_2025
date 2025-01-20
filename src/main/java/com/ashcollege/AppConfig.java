package com.ashcollege;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

import static com.ashcollege.utils.Constants.*;


@Configuration
@Profile("production")
public class AppConfig {


    @Bean
    public DataSource dataSource() throws Exception {
        String jdbcUrl = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true";
        Class.forName("com.mysql.jdbc.Driver");
        try (Connection connection = DriverManager.getConnection(jdbcUrl, DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement()) {
            String createSchemaSQL = "CREATE SCHEMA IF NOT EXISTS " + SCHEMA;
            statement.executeUpdate(createSchemaSQL);
        }
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass("com.mysql.jdbc.Driver");
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/" + SCHEMA + "?useSSL=false&allowPublicKeyRetrieval=true");
        dataSource.setUser(DB_USERNAME);
        dataSource.setPassword(DB_PASSWORD);
        dataSource.setMaxPoolSize(20);
        dataSource.setMinPoolSize(5);
        dataSource.setIdleConnectionTestPeriod(3600);
        dataSource.setTestConnectionOnCheckin(true);
        return dataSource;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory() throws Exception {
        LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource());
        Properties hibernateProperties = new Properties();
        hibernateProperties.put("hibernate.dialect", "org.hibernate.dialect.MySQL5InnoDBDialect");
        hibernateProperties.put("hibernate.hbm2ddl.auto", "update");
        hibernateProperties.put("hibernate.jdbc.batch_size", 50);
        hibernateProperties.put("hibernate.connection.characterEncoding", "utf8");
        hibernateProperties.put("hibernate.enable_lazy_load_no_trans", "true");
        sessionFactoryBean.setHibernateProperties(hibernateProperties);
        sessionFactoryBean.setMappingResources("objects.hbm.xml");
        return sessionFactoryBean;
    }

    @Bean
    public HibernateTransactionManager transactionManager() throws Exception {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory().getObject());
        return transactionManager;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("*");
            }
        };
    }

}
