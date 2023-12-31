package bootiful.demo;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

@ImportRuntimeHints(AppSpecificHints.class)
@SpringBootApplication
public class DemoApplication {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	ApplicationRunner cities(CityDao cityDao) {
		return args -> {
			var newCity = new City(null, "NYC", "NY", "USA");
			cityDao.insert(newCity);
			log.info("New city: {}", newCity);
			cityDao.findAll().forEach(x -> log.info("{}", x));
		};
	}

	@Bean
	ApplicationRunner customers(CustomerMapper customerMapper) {
		return args -> {
			Set.of("A", "B", "C").forEach(name -> customerMapper.save(new Customer(null, name)));
			customerMapper.find().forEach(System.out::println);
			// the second time should be faster than the first b/c of the caching
			customerMapper.find().forEach(System.out::println);
		};
	}

}

class AppSpecificHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {

		// there's no easy way for Spring to know at compile time that
		// we're using City with mybatis so you'll have to tell it

		for (var c : Set.of(City.class))
			hints.reflection().registerType(c, MemberCategory.values());

	}

}

@Component
class CityDao {

	private final SqlSession sqlSession;

	CityDao(SqlSession sqlSession) {
		this.sqlSession = sqlSession;
	}

	void insert(City city) {
		sqlSession.insert("bootiful.demo.CityDao.insert", city);
	}

	Collection<City> findAll() {
		return sqlSession.selectList("bootiful.demo.CityDao.findAll");
	}

}

class City {

	Integer id;

	String name;

	String state;

	String country;

	City(Integer id, String name, String state, String country) {
		this.id = id;
		this.name = name;
		this.state = state;
		this.country = country;
	}

	@Override
	public String toString() {
		return "City{" + "id=" + id + ", name='" + name + '\'' + ", state='" + state + '\'' + ", country='" + country
				+ '\'' + '}';
	}

}

// for caching
class Customer implements Serializable {

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

@Mapper
@CacheNamespace
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
