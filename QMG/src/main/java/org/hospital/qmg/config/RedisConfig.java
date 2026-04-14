package org.hospital.qmg.config;

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
        // 默认缓存配置：30分钟过期
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues();

        // 不同缓存区域的TTL配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 问卷配置：1小时（变更少）
        cacheConfigurations.put("qmg-config", defaultConfig.entryTtl(Duration.ofHours(1)));

        // 统计数据：5分钟（需要及时更新）
        cacheConfigurations.put("qmg-stats", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // 患者信息：30分钟
        cacheConfigurations.put("qmg-patient", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // 医生信息：1小时
        cacheConfigurations.put("qmg-doctor", defaultConfig.entryTtl(Duration.ofHours(1)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}