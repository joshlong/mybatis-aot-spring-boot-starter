package com.example.demo.spring;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.javassist.util.proxy.ProxyFactory;
import org.apache.ibatis.javassist.util.proxy.RuntimeSupport;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.logging.log4j.Log4jImpl;
import org.apache.ibatis.logging.log4j2.Log4j2Impl;
import org.apache.ibatis.logging.nologging.NoLoggingImpl;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.apache.ibatis.scripting.defaults.RawLanguageDriver;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.slf4j.Logger;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import java.util.*;

class MybatisRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        try {

            var memberCategories = MemberCategory.values();

            var clzzesForReflection = Set.of(

                    ArrayList.class, HashSet.class, Set.class, List.class, Map.class, HashMap.class,
                    //
                    org.apache.ibatis.builder.CacheRefResolver.class,
                    org.apache.ibatis.parsing.XNode.class,
                    org.apache.ibatis.mapping.ResultFlag.class,
                    org.apache.ibatis.builder.ResultMapResolver.class,
                    MapperScannerConfigurer.class,
                    org.apache.ibatis.builder.annotation.MethodResolver.class,
                    org.apache.ibatis.builder.annotation.ProviderMethodResolver.class,
                    org.apache.ibatis.builder.annotation.ProviderContext.class,
                    org.apache.ibatis.builder.annotation.MapperAnnotationBuilder.class,
                    Select.class, Update.class, Insert.class, Delete.class, SelectProvider.class, UpdateProvider.class,
                    InsertProvider.class, DeleteProvider.class, Options.class,
                    Logger.class,
                    LogFactory.class,
                    RuntimeSupport.class,
                    Log.class,
                    StdOutImpl.class,
                    NoLoggingImpl.class,
                    SqlSessionTemplate.class,
                    SqlSessionFactory.class,
                    SqlSessionFactoryBean.class,
                    ProxyFactory.class, XMLLanguageDriver.class,
                    Log4jImpl.class,
                    Log4j2Impl.class,
                    org.apache.ibatis.logging.jdk14.Jdk14LoggingImpl.class,
                    org.apache.ibatis.logging.slf4j.Slf4jImpl.class,
                    RawLanguageDriver.class, org.apache.ibatis.session.Configuration.class );

            for (var c : clzzesForReflection)
                hints.reflection().registerType(c, memberCategories);

        }//
        catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }


}
