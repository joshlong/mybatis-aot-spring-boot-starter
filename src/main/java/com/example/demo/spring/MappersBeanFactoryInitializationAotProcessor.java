package com.example.demo.spring;

import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.FileCopyUtils;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Josh Long
 */
class MappersBeanFactoryInitializationAotProcessor implements BeanFactoryInitializationAotProcessor {

	private final PathMatchingResourcePatternResolver resolver = MyBatisAotAutoConfiguration.patternResolver();

	private Set<Resource> persistenceResources(String rootPackage) throws Exception {

		var folderFromPackage = new StringBuilder();
		for (var c : rootPackage.toCharArray())
			folderFromPackage.append(c == '.' ? '/' : c);

		var patterns = Stream//
			.of(folderFromPackage + "/**/mappings.xml")//
			.map(path -> ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + path)//
			.flatMap(p -> {
				try {
					return Stream.of(this.resolver.getResources(p));
				} //
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			})
			.map(MappersBeanFactoryInitializationAotProcessor::newResourceFor)
			.toList();

		var resources = new HashSet<Resource>();
		for (var p : patterns) {
			var mappers = mappers(p);
			resources.add(p);
			resources.addAll(mappers);
		}
		return resources.stream().filter(Resource::exists).collect(Collectors.toSet());
	}

	private static Resource newResourceFor(Resource in) {
		try {
			var marker = "jar!";
			var p = in.getURL().toExternalForm();
			var rest = p.substring(p.lastIndexOf(marker) + marker.length());
			return new ClassPathResource(rest);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public BeanFactoryInitializationAotContribution processAheadOfTime(ConfigurableListableBeanFactory beanFactory) {
		try {
			var packages = AutoConfigurationPackages.get(beanFactory);

			var resources = new HashSet<Resource>();
			for (var pkg : packages) {
				resources.addAll(persistenceResources(pkg));
			}
			return (generationContext, beanFactoryInitializationCode) -> {
				for (var r : resources)
					if (r.exists())
						generationContext.getRuntimeHints().resources().registerResource(r);
			};
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Set<Resource> mappers(Resource mapping) throws Exception {
		var resources = new HashSet<Resource>();
		try (var in = new InputStreamReader(mapping.getInputStream())) {
			var xml = FileCopyUtils.copyToString(in);
			resources.addAll(mapperResources(xml));
		}
		resources.add(mapping);
		return resources;

	}

	private Set<Resource> mapperResources(String xml) {
		try {
			var set = new HashSet<Resource>();
			var dbf = DocumentBuilderFactory.newInstance();
			var db = dbf.newDocumentBuilder();
			var is = new InputSource(new StringReader(xml));
			var doc = db.parse(is);
			var mappersElement = (Element) doc.getElementsByTagName("mappers").item(0);
			var mapperList = mappersElement.getElementsByTagName("mapper");
			for (var i = 0; i < mapperList.getLength(); i++) {
				var mapperElement = (Element) mapperList.item(i);
				var resourceValue = mapperElement.getAttribute("resource");
				set.add(new ClassPathResource(resourceValue));
			}
			return set;
		} //
		catch (Throwable throwable) {
			throw new RuntimeException(throwable);
		}

	}

}
