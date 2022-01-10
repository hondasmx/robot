//package ru.tinkoff.piapi.robot.db.configuration;
//
//import org.postgresql.jdbc3.Jdbc3PoolingDataSource;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
//import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
//import org.springframework.context.annotation.*;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.orm.jpa.JpaTransactionManager;
//import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
//import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
//import org.springframework.transaction.PlatformTransactionManager;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//
//import javax.sql.DataSource;
//import java.util.HashMap;
//import java.util.Map;
//
//@ComponentScan("ru.tinkoff.piapi")
//@Configuration
//@Import({JacksonAutoConfiguration.class, DBConfigurationProperties.class})
//@EnableTransactionManagement
//@EnableJpaRepositories(basePackages = "ru.tinkoff.piapi"
//        )
//public class DBConfiguration {
//
//    @Bean
//    @Primary
//    public DataSource piapiDatasource(DBConfigurationProperties config) {
//        var datasource = new Jdbc3PoolingDataSource();
//        datasource.setDatabaseName(config.getDatabase());
//        datasource.setUser(config.getUsername());
//        datasource.setPassword(config.getPassword());
//        datasource.setPortNumber(config.getPort());
//        datasource.setServerName(config.getServerName());
//        datasource.setMaxConnections(2);
//        return datasource;
//    }
//
//
//    private Map<String, String> additionalProperties() {
//        Map<String, String> configuration = new HashMap<>();
//        configuration.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
//        configuration.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQL10Dialect");
//
//        return configuration;
//    }
//
//    @Bean
//    public LocalContainerEntityManagerFactoryBean piapiEntityManagerFactory(@Qualifier("piapiEntityManagerFactoryBuilder") EntityManagerFactoryBuilder builder,
//                                                                            @Qualifier("piapiDatasource") DataSource dataSource) {
//        return builder
//                .dataSource(dataSource)
//                .packages("ru.tinkoff.automation.publicApi.db.piapi.entities")
//                .properties(additionalProperties())
//                .build();
//    }
//
////    @Bean
////    public PlatformTransactionManager piapiTransactionManager(
////            @Qualifier("piapiEntityManagerFactory") EntityManagerFactory emf) {
////        return new JpaTransactionManager(emf);
////    }
//
//    @Bean
//    public EntityManagerFactoryBuilder piapiEntityManagerFactoryBuilder() {
//        return new EntityManagerFactoryBuilder(new HibernateJpaVendorAdapter(), new HashMap<>(), null);
//    }
//}
