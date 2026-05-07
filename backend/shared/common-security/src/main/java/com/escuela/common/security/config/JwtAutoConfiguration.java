package com.escuela.common.security.config;

import com.escuela.common.security.jwt.JwtProperties;
import com.escuela.common.security.jwt.JwtTokenProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuracion del subsistema JWT.
 *
 * <p>Se carga automaticamente cuando {@code common-security} esta en el
 * classpath, gracias al archivo {@code AutoConfiguration.imports}. Registra
 * el {@link JwtTokenProvider} y mapea {@link JwtProperties} desde
 * {@code application.yml}.</p>
 *
 * <p>El bean SOLO se crea si la propiedad {@code escuela.security.jwt.secret}
 * esta definida (sino el provider lanzaria al construir la SecretKey).</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(JwtTokenProvider.class)
    @ConditionalOnProperty(prefix = "escuela.security.jwt", name = "secret")
    public JwtTokenProvider jwtTokenProvider(JwtProperties properties) {
        return new JwtTokenProvider(properties);
    }
}
