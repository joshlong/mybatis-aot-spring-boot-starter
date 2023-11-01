package com.example.demo.spring;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.TypeReference;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

class MyBatisBeanFactoryInitializationAotProcessor implements BeanFactoryInitializationAotProcessor {

	@Override
	public BeanFactoryInitializationAotContribution processAheadOfTime(ConfigurableListableBeanFactory beanFactory) {

		if (!ClassUtils.isPresent("org.mybatis.spring.mapper.MapperFactoryBean", beanFactory.getBeanClassLoader()))
			return null;

		var classesToRegister = new HashSet<Class<?>>();
		var proxiesToRegister = new HashSet<Class<?>>();
		var resourcesToRegister = new HashSet<Resource>();

		var beanNames = beanFactory.getBeanNamesForType(MapperFactoryBean.class);
		for (var beanName : beanNames) {
			var beanDefinition = beanFactory.getBeanDefinition(beanName.substring(1));
			var mapperInterface = beanDefinition.getPropertyValues().getPropertyValue("mapperInterface");
			if (mapperInterface != null && mapperInterface.getValue() != null) {
				var mapperInterfaceType = (Class<?>) mapperInterface.getValue();
				if (mapperInterfaceType != null) {
					proxiesToRegister.add(mapperInterfaceType);
					resourcesToRegister
						.add(new ClassPathResource(mapperInterfaceType.getName().replace('.', '/').concat(".xml")));
					registerReflectionTypeIfNecessary(mapperInterfaceType, classesToRegister);
					registerMapperRelationships(mapperInterfaceType, classesToRegister);
				}
			}
		}

		return (generationContext, beanFactoryInitializationCode) -> {

			var mcs = MemberCategory.values();
			var runtimeHints = generationContext.getRuntimeHints();

			AotUtils.debug("proxies", proxiesToRegister);
			AotUtils.debug("classes for reflection", classesToRegister);
			AotUtils.debug("resources", resourcesToRegister);

			for (var c : proxiesToRegister) {
				runtimeHints.proxies().registerJdkProxy(c);
				runtimeHints.reflection().registerType(c, mcs);
			}

			for (var c : classesToRegister) {
				runtimeHints.reflection().registerType(c, mcs);
				if (AotUtils.isSerializable(c))
					runtimeHints.serialization().registerType(TypeReference.of(c.getName()));
			}

			for (var r : resourcesToRegister) {
				if (r.exists()) {
					runtimeHints.resources().registerResource(r);
				}
			}
		};
	}

	@SafeVarargs
	private <T extends Annotation> void registerSqlProviderTypes(Method method, Set<Class<?>> registry,
			Class<T> annotationType, Function<T, Class<?>>... providerTypeResolvers) {
		for (T annotation : method.getAnnotationsByType(annotationType)) {
			for (Function<T, Class<?>> providerTypeResolver : providerTypeResolvers) {
				registerReflectionTypeIfNecessary(providerTypeResolver.apply(annotation), registry);
			}
		}
	}

	private void registerReflectionTypeIfNecessary(Class<?> type, Set<Class<?>> registry) {
		if (!type.isPrimitive() && !type.getName().startsWith("java")) {
			registry.add(type);
		}
	}

	private void registerMapperRelationships(Class<?> mapperInterfaceType, Set<Class<?>> registry) {
		var methods = ReflectionUtils.getAllDeclaredMethods(mapperInterfaceType);
		for (var method : methods) {
			if (method.getDeclaringClass() != Object.class) {

				ReflectionUtils.makeAccessible(method);

				registerSqlProviderTypes(method, registry, SelectProvider.class, SelectProvider::value,
						SelectProvider::type);
				registerSqlProviderTypes(method, registry, InsertProvider.class, InsertProvider::value,
						InsertProvider::type);
				registerSqlProviderTypes(method, registry, UpdateProvider.class, UpdateProvider::value,
						UpdateProvider::type);
				registerSqlProviderTypes(method, registry, DeleteProvider.class, DeleteProvider::value,
						DeleteProvider::type);

				var returnType = MyBatisMapperTypeUtils.resolveReturnClass(mapperInterfaceType, method);
				registerReflectionTypeIfNecessary(returnType, registry);

				MyBatisMapperTypeUtils.resolveParameterClasses(mapperInterfaceType, method)
					.forEach(x -> registerReflectionTypeIfNecessary(x, registry));
			}
		}
	}

}
