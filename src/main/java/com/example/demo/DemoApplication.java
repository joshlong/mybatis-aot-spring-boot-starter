package com.example.demo;

import org.apache.ibatis.annotations.*;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	ApplicationRunner demoRunner(CustomerMapper customerMapper) {
		return args -> {
			Set.of("A", "B", "C").forEach(name -> customerMapper.save(new Customer(null, name)));

			customerMapper.find().forEach(System.out::println);
			// second time should be faster?
			customerMapper.find().forEach(System.out::println);
		};
	}

}

class Customer implements Serializable // for caching
{

	@Serial
	private static final long serialVersionUID = 1L;

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

@CacheNamespace
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