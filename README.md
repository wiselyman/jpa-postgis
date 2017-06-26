`Spring Data JPA`的极度简便的使用方式让我们爱不释手，但是我们在项目中经常会有使用空间数据的场景，而不同的数据库对空间数据实现的方式不同，而这些不仅是`JPA`或者`Spring Data JPA`都是不支持的这时我们需要引入`hibernate-spatial`来去除数据库支持的异构性。
在本例中集成了`Spring Data JPA`、`hibernate -spatial`、`PostGIS`一起的使用方式。
### 1. 添加依赖
```xml
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-spatial</artifactId>
    <version>5.2.10.Final</version>
</dependency>

```
### 2. 数据库方言指定

```yaml
spring.jpa.database-platform: org.hibernate.spatial.dialect.postgis.PostgisPG9Dialect

```
`org.hibernate.spatial.dialect`下还有`h2`,`mysql`,`oracle`,`sqlserver`的方言。

### 3. 属性映射
```java
 @Column(columnDefinition = "geometry(Point,4326)")
 private Point point;

```

### 4. 定义Spring Data Repository

```java
public interface CityRepository extends JpaRepository<City,Long> {
    @Query("select city from City as city where equals(city.point,:point) = TRUE")
    List<City> findByPoint(@Param("point") Point point);
}
```

### 5. 保存测试
```java
@Bean
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
```

### 6. 读取测试
```java

@Bean
CommandLineRunner geometryRead(CityRepository cityRepository){

    return e -> {
        City city = cityRepository.findOne(3l);
        Point point = city.getPoint();
        log.info("经度:" + point.getX() + " 维度:" + point.getY() + " 坐标系统:" + point.getSRID());
    };

}

```

### 7. 查询测试

```java
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
```

我们在`cityRepository`中使用了`hibernate-spatial`中的空间函数`equals`，具体空间函数列表请查看
[http://docs.jboss.org/hibernate/orm/5.2/userguide/html_single/Hibernate_User_Guide.html#spatial-configuration-dialect](http://docs.jboss.org/hibernate/orm/5.2/userguide/html_single/Hibernate_User_Guide.html#spatial-configuration-dialect)

### 8 源码地址
[https://github.com/wiselyman/jpa-postgis](https://github.com/wiselyman/jpa-postgis)