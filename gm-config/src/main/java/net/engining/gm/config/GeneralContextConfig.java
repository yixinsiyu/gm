package net.engining.gm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import net.engining.gm.config.props.GmCommonProperties;
import net.engining.pg.param.props.PgParamAndCacheProperties;
import net.engining.pg.parameter.JsonLocalCachedParameterFacility;
import net.engining.pg.parameter.LocalCachedParameterFacility;
import net.engining.pg.parameter.ParameterFacility;
import net.engining.pg.parameter.ProcessesProvider4Organization;
import net.engining.pg.parameter.Provider4Organization;
import net.engining.pg.parameter.RedisCachedParameterFacility;
import net.engining.pg.parameter.RedisJsonCachedParameterFacility;
import net.engining.pg.props.CommonProperties;
import net.engining.pg.support.core.context.ApplicationContextHolder;

/**
 * 通用Context配置
 * @author Eric Lu
 *
 */
@Configuration
public class GeneralContextConfig {
	
	@Autowired
	CommonProperties commonProperties;
	
	@Autowired
	PgParamAndCacheProperties pgParamAndCacheProperties;
	
	@Autowired
	GmCommonProperties gmCommonProperties;
	
	/**
	 * ApplicationContext的静态辅助Bean，建议项目必须注入
	 * @return
	 */
	@Bean
	@Lazy(value=false)
	public ApplicationContextHolder applicationContextHolder(){
		return new ApplicationContextHolder();
	}
	
	/**
	 * 参数体系辅助Bean，建议项目必须注入
	 * @return
	 */
	@Bean
	public ParameterFacility parameterFacility(){
		if (pgParamAndCacheProperties.isEnableRedisCache()) {
			if(pgParamAndCacheProperties.isJsonParameterFacility()){
				RedisJsonCachedParameterFacility redisCachedParameterFacility = new RedisJsonCachedParameterFacility();
				redisCachedParameterFacility.setExpireDuration(pgParamAndCacheProperties.getExpireDuration());
				redisCachedParameterFacility.setExpireTimeUnit(pgParamAndCacheProperties.getExpireTimeUnit());
				return redisCachedParameterFacility;
			}
			RedisCachedParameterFacility redisCachedParameterFacility = new RedisCachedParameterFacility();
			redisCachedParameterFacility.setExpireDuration(pgParamAndCacheProperties.getExpireDuration());
			redisCachedParameterFacility.setExpireTimeUnit(pgParamAndCacheProperties.getExpireTimeUnit());
			return redisCachedParameterFacility;
		}
		
		if(pgParamAndCacheProperties.isJsonParameterFacility()){
			JsonLocalCachedParameterFacility localCachedParameterFacility = new JsonLocalCachedParameterFacility();
			localCachedParameterFacility.setExpireDuration(pgParamAndCacheProperties.getExpireDuration());
			localCachedParameterFacility.setExpireTimeUnit(pgParamAndCacheProperties.getExpireTimeUnit());
			return localCachedParameterFacility;
		}
		LocalCachedParameterFacility localCachedParameterFacility = new LocalCachedParameterFacility();
		localCachedParameterFacility.setExpireDuration(pgParamAndCacheProperties.getExpireDuration());
		localCachedParameterFacility.setExpireTimeUnit(pgParamAndCacheProperties.getExpireTimeUnit());
		return localCachedParameterFacility;
	}
	
	@Bean
	public Provider4Organization provider4Organization(){
		ProcessesProvider4Organization processesProvider4Organization = new ProcessesProvider4Organization();
		//从配置文件获取默认机构ID
		processesProvider4Organization.setCurrentOrganizationId(gmCommonProperties.getDefaultOrgId());
		return processesProvider4Organization;
	}
}
