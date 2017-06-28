## 利用hibernate-spatial让Spring Data JPA支持空间数据
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

线的映射使用:
```java
 @Column(columnDefinition = "geometry(LineString,4326)")
    private LineString line;
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

## Spring Boot下空间字段(Geometry)与geojson的自动转换

在上一篇文章[利用hibernate-spatial让Spring Data JPA支持空间数据](http://www.wisely.top/2017/06/26/hibernate-spatial-spring-data-jpa/)，我们使用`hibernate spatial`,`spring data jpa`成功支持空间字段的映射与增删查改。但是我们目前面临的问题是客户端传过来的json数据如何转换为Geometry(Point、LineString)，后台的Geometry如何直接转换为JSON。在GIS的世界里有一个标准的GIS JSON格式叫做`geojson`。在本文将使用`geojson`格式与Geometry对象互相转换。
这里我们的思路是Spring Boot为我们自动注册了`MappingJackson2HttpMessageConverter`，在`org.springframework.boot.autoconfigure.web.JacksonHttpMessageConvertersConfiguration`如：
```java
@Bean
@ConditionalOnMissingBean(value = MappingJackson2HttpMessageConverter.class, ignoredType = {
        "org.springframework.hateoas.mvc.TypeConstrainedMappingJackson2HttpMessageConverter",
        "org.springframework.data.rest.webmvc.alps.AlpsJsonHttpMessageConverter" })
public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(
        ObjectMapper objectMapper) {
    return new MappingJackson2HttpMessageConverter(objectMapper);
}

```
据此看出Spring Boot是使用当前的Message Converter来实现对象(Geometry)和json之间转换的，我们只需要自定义objectMapper让其支持geojson即可。

添加第三方的依赖：
```xml
<dependency>
    <groupId>com.bedatadriven</groupId>
    <artifactId>jackson-datatype-jts</artifactId>
    <version>2.4</version>
</dependency>

<repositories>
    <repository>
        <id>sonatype-oss</id>
        <url>https://oss.sonatype.org/content/groups/public</url>
    </repository>
</repositories>
```

自定义object让其支持Geometry与geojson之间的准换:
```java
@Bean
public ObjectMapper objectMapper(){
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper().registerModule(new JtsModule());
    return objectMapper;

}
```
这个第三方目前有个bug就是不支持空间坐标系，在一个单一的系统里一般情况下坐标系应该也是一定的，所以不支持问题也不大，所以我们将前面例子的字段映射修改为，：
```java
//@Column(columnDefinition = "geometry(Point,4326)")
@Column(columnDefinition = "geometry(Point)")
private Point point;
```
测试控制器：
```java
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
```

测试数据，使用request body向后台`POST`如下结果：
```javascript
{
    "name": "南京",
    "point": {
      "type": "Point",
      "coordinates": [
        110.4,
        20.1
      ]
    }
  
  }

```
返回值为：
```javascript
{
  "id": 58,
  "name": "南京",
  "point": {
    "type": "Point",
    "coordinates": [
      110.4,
      20.1
    ]
  }
}

```
这时我们实现了自动的Geometry和geojson数据的转换。
