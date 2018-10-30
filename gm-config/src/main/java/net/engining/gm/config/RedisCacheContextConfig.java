package net.engining.gm.config;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.engining.gm.config.props.CommonProperties;

/**
 * 
 * @author luxue
 *
 */
@Configuration
@EnableCaching
public class RedisCacheContextConfig extends CachingConfigurerSupport {

	@Autowired
	CommonProperties commonProperties;
	
	/**
	 * 根据类名，方法名，参数组合生成缓存Key
	 * 
	 * @return
	 */
	@Bean
	@Override
	public KeyGenerator keyGenerator() {
		return new KeyGenerator() {
			@Override
			public Object generate(Object target, Method method, Object... params) {
				StringBuilder sb = new StringBuilder();
				sb.append(target.getClass().getName());
				sb.append(method.getName());
				for (Object obj : params) {
					sb.append(obj.toString());
				}
				return sb.toString();
			}
		};
	}

	/**
	 * 主缓存Manager；专门针对各微服务的缓存，便于区分
	 * @param factory
	 * @return
	 */
	@Bean
	@Primary
	public CacheManager cacheManager(RedisConnectionFactory factory) {
		RedisSerializer<String> redisSerializer = new StringRedisSerializer();
		Jackson2JsonRedisSerializer<Serializable> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Serializable>(Serializable.class);

		// 解决查询缓存转换异常的问题
		ObjectMapper om = new ObjectMapper();
		om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		jackson2JsonRedisSerializer.setObjectMapper(om);

		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
		// 整体缓存过期时间, 默认过期时间5分钟
		defaultExpiration(config);
		
		// 配置序列化（解决乱码的问题）
		config = config
				.computePrefixWith(new RedisPrefix(commonProperties.getAppname()))
				.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer))
				.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer))
				.disableCachingNullValues();

		RedisCacheManager cacheManager = RedisCacheManager.builder(factory)
				.cacheDefaults(config)
				.transactionAware()
				.build();
		
		return cacheManager;
	}
	
	/**
	 * FIXME 迁移到pg-parameter；统一的共享参数缓存Manager；各微服务间共享的参数缓存
	 * @param factory
	 * @return
	 */
	@Bean("cacheParameterManager")
    public CacheManager cacheParameterManager(RedisConnectionFactory factory) {
		RedisSerializer<String> redisSerializer = new StringRedisSerializer();
		Jackson2JsonRedisSerializer<Serializable> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Serializable>(Serializable.class);

		// 解决查询缓存转换异常的问题
		ObjectMapper om = new ObjectMapper();
		om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		jackson2JsonRedisSerializer.setObjectMapper(om);

		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
		// 整体缓存过期时间, 默认过期时间5分钟
		defaultExpiration(config);
		
		// 配置序列化（解决乱码的问题）
		config = config
				.computePrefixWith(new RedisPrefix())
				.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer))
				.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer))
				.disableCachingNullValues();

		RedisCacheManager cacheManager = RedisCacheManager.builder(factory)
				.cacheDefaults(config)
				.transactionAware()
				.build();
		
		return cacheManager;
    }
	
	private void defaultExpiration(RedisCacheConfiguration config){
		// 整体缓存过期时间, 默认过期时间5分钟
		long expriation = commonProperties.getExpireDuration();
		TimeUnit expireTimeUnit = commonProperties.getExpireTimeUnit();
		if (commonProperties.getExpireDuration() > 0) {
			if (commonProperties.getExpireTimeUnit() != null) {
				expireTimeUnit = commonProperties.getExpireTimeUnit();
				switch (expireTimeUnit) {
				case DAYS:
					expriation = commonProperties.getExpireDuration() * 24 * 60 * 60;
					config.entryTtl(Duration.ofDays(expriation));
					break;
				case HOURS:
					expriation = commonProperties.getExpireDuration() * 60 * 60;
					config.entryTtl(Duration.ofHours(expriation));
					break;
				case MINUTES:
					expriation = commonProperties.getExpireDuration() * 60;
					config.entryTtl(Duration.ofMinutes(expriation));
					break;
				case SECONDS:
					expriation = commonProperties.getExpireDuration();
					config.entryTtl(Duration.ofSeconds(expriation));
					break;
				default:
					//默认过期时间5分钟
					config.entryTtl(Duration.ofMinutes(expriation));
					break;
				}
			}
		}
	}

	public class RedisPrefix implements CacheKeyPrefix {
		private final String delimiter;

		public RedisPrefix() {
			this(null);
		}

		public RedisPrefix(String delimiter) {
			this.delimiter = delimiter;
		}

		@Override
		public String compute(String cacheName) {
			return this.delimiter != null
					? this.delimiter.concat(":").concat(cacheName).concat(":") : cacheName.concat(":");
		}
	}

}
