package com.sucl.redis.lock;

import java.util.HashSet;
import java.util.Set;

/**
 * 监控锁是否被占用
 * @author sucl
 * @date 2019/5/24
 */
public class RedisLockWatch {

    public static final Set<String> locks = new HashSet<>();

    public RedisLockWatch(){

    }

    public static void add(String name){
        if(locks.contains(name)){
            //名称占用
        }else{
            locks.add(name);
        }
    }

    public static void remove(String name){
        if(locks.contains(name)){
            locks.remove(name);
        }
    }
}
