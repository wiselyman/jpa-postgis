package com.example.jpapostgis.controller;

import com.example.jpapostgis.domain.City;
import com.example.jpapostgis.repository.CityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by wangyunfei on 2017/6/28.
 */
@RestController
@Slf4j
@RequestMapping("/cities")
public class CityController {
    @Autowired
    CityRepository cityRepository;

    @PostMapping
    public City testIn(@RequestBody City city){
        log.info(city.getPoint().getSRID()
                + "/" + city.getPoint().getX()
                + "/" +city.getPoint().getY());
        return cityRepository.save(city);

    }
}
