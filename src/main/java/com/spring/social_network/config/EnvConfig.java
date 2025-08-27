package com.spring.social_network.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:.env", ignoreResourceNotFound = true)
public class EnvConfig {
}
