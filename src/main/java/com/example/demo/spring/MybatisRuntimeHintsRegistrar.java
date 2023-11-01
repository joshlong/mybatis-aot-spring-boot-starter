package com.example.demo.spring;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.builder.CacheRefResolver;
import org.apache.ibatis.builder.ResultMapResolver;
import org.apache.ibatis.builder.annotation.MapperAnnotationBuilder;
import org.apache.ibatis.builder.annotation.MethodResolver;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.javassist.util.proxy.ProxyFactory;
import org.apache.ibatis.javassist.util.proxy.RuntimeSupport;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.logging.commons.JakartaCommonsLoggingImpl;
import org.apache.ibatis.logging.jdbc.*;
import org.apache.ibatis.logging.jdk14.Jdk14LoggingImpl;
import org.apache.ibatis.logging.log4j.Log4jImpl;
import org.apache.ibatis.logging.log4j2.Log4j2AbstractLoggerImpl;
import org.apache.ibatis.logging.log4j2.Log4j2Impl;
import org.apache.ibatis.logging.log4j2.Log4j2LoggerImpl;
import org.apache.ibatis.logging.nologging.NoLoggingImpl;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.scripting.defaults.RawLanguageDriver;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.logging.slf4j.SLF4JLogger;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.slf4j.Logger;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.*;

class MybatisRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

	private final PathMatchingResourcePatternResolver resourcePatternResolver = MyBatisAotAutoConfiguration
		.patternResolver();

	void registerResources(RuntimeHints hints) throws IOException {
		//
		for (var r : this.resourcePatternResolver.getResources("org/mybatis/spring/config/.*.xsd"))
			hints.resources().registerResource(r);
	}

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		try {

			registerResources(hints);

			var memberCategories = MemberCategory.values();

			var classesForReflection = Set.of(ArrayList.class, HashSet.class, Set.class, List.class, Map.class,
					HashMap.class, CacheRefResolver.class, XNode.class, ResultFlag.class, ResultMapResolver.class,
					MapperScannerConfigurer.class, MethodResolver.class, ProviderMethodResolver.class,
					ProviderContext.class, MapperAnnotationBuilder.class, Select.class, Update.class, Insert.class,
					Delete.class, SelectProvider.class, UpdateProvider.class, InsertProvider.class,
					DeleteProvider.class, Options.class, Logger.class, LogFactory.class, RuntimeSupport.class,
					Log.class, SqlSessionTemplate.class, SqlSessionFactory.class, SqlSessionFactoryBean.class,
					ProxyFactory.class, XMLLanguageDriver.class,
					// loggers
					Log4jImpl.class, Log4j2Impl.class, Log4j2LoggerImpl.class, Log4j2AbstractLoggerImpl.class,
					NoLoggingImpl.class, SLF4JLogger.class, StdOutImpl.class, BaseJdbcLogger.class,
					ConnectionLogger.class, PreparedStatementLogger.class, ResultSetLogger.class, StatementLogger.class,
					Jdk14LoggingImpl.class, JakartaCommonsLoggingImpl.class, Slf4jImpl.class,
					//
					RawLanguageDriver.class, org.apache.ibatis.session.Configuration.class, String.class, int.class,
					Integer.class, long.class, Long.class, short.class, Short.class, byte.class, Byte.class,
					float.class, Float.class, boolean.class, Boolean.class, double.class, Double.class);

			for (var c : classesForReflection)
				hints.reflection().registerType(c, memberCategories);

		} //
		catch (Throwable throwable) {
			throw new RuntimeException(throwable);
		}
	}

}
