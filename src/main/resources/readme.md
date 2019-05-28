重温springboot缓存：
1、引入依赖：
    springboot支持多种缓存机制，引入不同的缓存类则执行不同的缓存
    比如引入redis:
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
    
2、添加注解：     
    @EnableCaching   
3、使用
   @Cacheable 如果有，则从缓存中取，如果没有则存入
        key：支持从参数取值spel表达式
            支持之定义生成策略，实现接口org.springframework.cache.interceptor.KeyGenerator
   @CachePut 每次都会执行方法，并将结果缓存
   @CacheEvict 删除
4、原理
    @EnableCaching
    CacheConfigurations
    按顺序CacheType加载，除非配置文件制定cacheType
5、redis缓存
    在org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration中已经帮助我们配置了
6、redis管理session

7、redis分布式锁
    