package org.hospital.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        // 配置 ObjectMapper 保留数值类型信息
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // QMG cache regions
        cacheConfigurations.put("qmg-config", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("qmg-stats", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("qmg-patient", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("qmg-doctor", defaultConfig.entryTtl(Duration.ofHours(1)));

        // Neuroimmune cache regions
        cacheConfigurations.put("neuro-followup", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("neuro-stats", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("neuro-patient", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("neuro-doctor", defaultConfig.entryTtl(Duration.ofHours(1)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}