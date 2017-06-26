package com.example.jpapostgis;

import com.example.jpapostgis.domain.City;
import com.example.jpapostgis.repository.CityRepository;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
@Slf4j
public class JpaPostgisApplication {
	@Bean
	public WKTReader wktReader(){
		return new WKTReader();
	}

	//@Bean
	CommandLineRunner geometrySave(CityRepository cityRepository){

		return e ->{
			City city = new City();
			city.setName("合肥");
			Geometry point = wktReader().read("POINT (117.2 31.8)");
			Point pointToSave = point.getInteriorPoint();
			pointToSave.setSRID(4326);
			city.setPoint(pointToSave);
			cityRepository.save(city);
		};

	}
	//@Bean
	CommandLineRunner geometryRead(CityRepository cityRepository){

		return e -> {
			City city = cityRepository.findOne(3l);
			Point point = city.getPoint();
			log.info("经度:" + point.getX() + " 维度:" + point.getY() + " 坐标系统:" + point.getSRID());
		};

	}

	@Bean
	CommandLineRunner geometryQuery(CityRepository cityRepository){
		return e -> {
			Geometry point = wktReader().read("POINT (117.2 31.8)");
			Point pointToQuery = point.getInteriorPoint();
			pointToQuery.setSRID(4326);

			List<City> cities = cityRepository.findByPoint(pointToQuery);

			for (City city : cities) {
				log.info("查询结果为:" + city.getId() + "/" +city.getName() + "/" +city.getPoint());
			}

		};
	}

	public static void main(String[] args) {
		SpringApplication.run(JpaPostgisApplication.class, args);
	}
}
