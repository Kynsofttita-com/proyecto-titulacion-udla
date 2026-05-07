package com.escuela.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Habilita el binding de {@link AuthProperties}. */
@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class AuthPropertiesConfig {
}
