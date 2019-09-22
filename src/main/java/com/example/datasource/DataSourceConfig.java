package com.example.datasource;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

/**
 * 数据源配置
 * 
 * @ClassName DataSourceConfig
 * @Description TODO
 * @author lide
 * @date 2018年2月27日 下午1:21:39
 */
@Configuration
public class DataSourceConfig {

	@Bean(name = "master")
	@ConfigurationProperties(prefix = "datasource.master") 
	public DataSource dataSource1() {
		System.out.println("主配");
		return DataSourceBuilder.create().build();
	}

	@Bean(name = "slave")
	@ConfigurationProperties(prefix = "datasource.slave") 
	public DataSource dataSource2() {
		System.out.println("从配");
		return DataSourceBuilder.create().build();
	}
	
	@Bean(name="dynamicDataSource")
	@Primary	//优先使用，多数据源
	public DataSource dataSource() {
		DynamicDataSource dynamicDataSource = new DynamicDataSource();
		DataSource master = dataSource1();
		DataSource slave = dataSource2();
		//设置默认数据源
		dynamicDataSource.setDefaultTargetDataSource(master);	
		//配置多数据源
		Map<Object,Object> map = new HashMap<>();
		map.put(DataSourceType.Master.getName(), master);	//key需要跟ThreadLocal中的值对应
		map.put(DataSourceType.Slave.getName(), slave);
		dynamicDataSource.setTargetDataSources(map);			
		return dynamicDataSource;
	}

	/**
	 * 多数据源需要自己设置sqlSessionFactory
	 */
	@Bean
	public SqlSessionFactory sqlSessionFactory() throws Exception {
		SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
		bean.setDataSource(dataSource());
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		// 实体类对应的位置
		bean.setTypeAliasesPackage("com.example.game.domain.pojo");
		// mybatis的XML的配置
		bean.setMapperLocations(resolver.getResources("classpath*:mapper/*.xml"));
		//mybatis-configuration.xml
		bean.setConfigLocation(resolver.getResource("classpath:mybatis-config.xml"));
		return bean.getObject();
	}

	/**
	 * 设置事务，事务需要知道当前使用的是哪个数据源才能进行事务处理
	 */
	@Bean
	public DataSourceTransactionManager dataSourceTransactionManager() {
		return new DataSourceTransactionManager(dataSource());
	}
	
}
