package com.example.demo.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author Josh Long
 */
final class AotUtils {

	private static final Logger log = LoggerFactory.getLogger(AotUtils.class);

	private AotUtils() {
		// noop
	}

	static boolean isSerializable(Class<?> clazz) {
		return Serializable.class.isAssignableFrom(clazz);
	}

	static <T> void debug(String message, Collection<T> tCollection) {
		log.info(message + System.lineSeparator());
		for (var t : tCollection)
			log.info('\t' + t.toString());
		log.info(System.lineSeparator());
	}

}
