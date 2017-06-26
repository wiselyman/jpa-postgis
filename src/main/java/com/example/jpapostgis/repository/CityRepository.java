package com.example.jpapostgis.repository;

import com.example.jpapostgis.domain.City;
import com.vividsolutions.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by wangyunfei on 2017/6/26.
 */
public interface CityRepository extends JpaRepository<City,Long> {
    @Query("select city from City as city where equals(city.point,:point) = TRUE")
    List<City> findByPoint(@Param("point") Point point);
}
