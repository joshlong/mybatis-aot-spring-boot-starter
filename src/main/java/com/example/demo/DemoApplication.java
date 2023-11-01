package com.example.demo;

import org.apache.ibatis.annotations.*;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportRuntimeHints;

import java.util.Collection;
import java.util.Set;

@ImportRuntimeHints(AppSpecificHints.class)
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	ApplicationRunner demoRunner(CustomerMapper customerMapper) {
		return args -> {
			Set.of("Josh", "Stéphane", "Eddù").forEach(name -> customerMapper.save(new Customer(null, name)));
			customerMapper.find().forEach(System.out::println);
		};
	}

}

class AppSpecificHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		var mcs = MemberCategory.values();
		Set.of(CustomerMapper.class, Customer.class).forEach(c -> hints.reflection().registerType(c, mcs));
		hints.proxies().registerJdkProxy(CustomerMapper.class);
	}

}

class Customer {

	Integer id;

	String name;

	Customer(Integer id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public String toString() {
		return "Customer{" + "id=" + id + ", name='" + name + '\'' + '}';
	}

}

@Mapper
interface CustomerMapper {

	@Insert("Insert into customer( name ) values(#{name} )")
	@Options(useGeneratedKeys = true, flushCache = Options.FlushCachePolicy.FALSE)
	Integer save(Customer address);

	@Select("SELECT  id, name FROM customer WHERE  id  = #{id}")
	@Results(value = { @Result(property = "id", column = "id"), @Result(property = "name", column = "name") })
	Customer findById(@Param("id") Integer id);

	@Select("SELECT  id, name FROM customer ")
	@Results(value = { @Result(property = "id", column = "id"), @Result(property = "name", column = "name") })
	Collection<Customer> find();

}