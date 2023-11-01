package com.example.demo.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration
@ImportRuntimeHints(MybatisRuntimeHintsRegistrar.class)
class MyBatisAotAutoConfiguration {

	static PathMatchingResourcePatternResolver patternResolver() {
		return new PathMatchingResourcePatternResolver();
	}

	@Bean
	static MyBatisBeanFactoryInitializationAotProcessor myBatisBeanFactoryInitializationAotProcessor() {
		return new MyBatisBeanFactoryInitializationAotProcessor();
	}

	@Bean
	static MappersBeanFactoryInitializationAotProcessor mappersBeanFactoryInitializationAotProcessor() {
		return new MappersBeanFactoryInitializationAotProcessor();
	}

}
