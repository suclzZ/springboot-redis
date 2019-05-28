package com.sucl.redis.service;

import com.sucl.redis.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Cacheable 如果没有则存，如果有则从缓存去
 * @CachePut 不管缓存没有，都会去方法取，然后缓存
 * @CacheEvict 删除
 *
 * @author sucl
 * @date 2019/5/23
 */
@Slf4j
@Service
public class UserService {
    private static List<User> users = new ArrayList<>();
    private static Map<String,User>  userMap = new HashMap<>();

    public static final String CACHE_USER = "cache_user";

    static {
        users.add(new User("1","TOM1",26,"tom@123.com"));
        users.add(new User("2","JACK",27,"jack@123.com"));
        users.add(new User("3","ROSE",28,"rose@123.com"));
        users.add(new User("4","LILY",29,"lily@123.com"));
        users.add(new User("5","JONSEN",30,"jonsen@123.com"));
        users.forEach(u->{
            userMap.put(u.getUserId(),u);
        });
    }

    @Cacheable(value = CACHE_USER,key = "#id")
    public User getUser(String id){
        log.info("getUser by {}",id);
        return userMap.get(id);
    }

    public List<User> getUsers(){
        log.info("getUsers");
        return users;
    }

    public User saveUser(User user){
        log.info("saveUser {}",user);
        if(user.getUserId()!=null && !"".equals(user.getUserId())){
            updateUser(user);
        }else{
            user.setUserId(UUID.randomUUID().toString());
            users.add(user);
            userMap.put(user.getUserId(),user);
        }
        return user;
    }

    @CachePut(value = CACHE_USER,key = "#user.userId")
    public User updateUser(User user){
        log.info("updateUser {}",user);
        String id = user.getUserId();
        if(user.getUserId()!=null && !"".equals(user.getUserId())){
            if(userMap.get(user.getUserId())==null){
                throw new RuntimeException(String.format("id:【%s】没有对应对象！" ,user.getUserId()));
            }else{
                User oldUser = userMap.get(user.getUserId());
                userMap.put(user.getUserId(),user);
                users.remove(oldUser);
                users.add(user);
            }
            userMap.put(user.getUserId(),user);
        }else{
            throw new RuntimeException("用户id不能为空！");
        }
        return user;
    }

    @CacheEvict(value = CACHE_USER,key = "#id")
    public int deleteUser(String id){
        log.info("deleteUser by {}",id);
        User user = userMap.get(id);
        users.remove(user);
        userMap.remove(id);
        return 1;
    }
}
