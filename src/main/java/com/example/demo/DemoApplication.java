package com.example.demo;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

@SpringBootApplication
@RegisterReflectionForBinding(City.class)
public class DemoApplication {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    ApplicationRunner debug(SqlSessionFactory sqlSessionFactory) {
        return arg -> {
            var config = sqlSessionFactory.getConfiguration();
            var ms = config.getMappedStatements();
            System.out.println("mapped statements size: " + ms.size());



        };
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
            customerMapper.find().forEach(System.out::println);//two times; the second should be faster b/c of caching
        };
    }

}

@Component
class CityDao {

    private final SqlSession sqlSession;

    CityDao(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    void insert(City city) {
        sqlSession.insert("com.example.demo.CityDao.insert", city);
    }

    Collection<City> findAll() {
        return sqlSession.selectList("com.example.demo.CityDao.findAll");
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
        return "City{" + "id=" + id + ", name='" + name + '\'' + ", state='" + state + '\'' + ", country='" + country + '\'' + '}';
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
    @Results(value = {@Result(property = "id", column = "id"), @Result(property = "name", column = "name")})
    Customer findById(@Param("id") Integer id);

    @Select("SELECT  id, name FROM customer ")
    @Results(value = {@Result(property = "id", column = "id"), @Result(property = "name", column = "name")})
    Collection<Customer> find();

}
