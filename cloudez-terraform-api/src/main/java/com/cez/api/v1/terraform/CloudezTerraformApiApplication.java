package com.cez.api.v1.terraform;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Executor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = SearchScriptsRepositoryImpl.class)
public class CloudezTerraformApiApplication
{
  public static void main(String[] args)
  {
    SpringApplication.run(CloudezTerraformApiApplication.class, args);
  }

  @Bean
  public FilterRegistrationBean<CorsFilter> simpleCorsFilter()
  {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(Arrays.asList("*"));
    config.setAllowedHeaders(Arrays.asList("*"));
    config.setAllowedMethods(Collections.singletonList("*"));
    source.registerCorsConfiguration("/**", config);
    FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
    bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return bean;
  }

  @Bean
  public Executor taskExecutor()
  {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(2);
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("terraform-");
    executor.initialize();
    return executor;
  }


  @Bean
  public ApplicationRunner buildIndex(Indexer indexer) throws Exception
  {
    return (ApplicationArguments args) -> {
      indexer.indexPersistedData("com.cez.api.v1.terraform.Scripts");
    };
  }
}
