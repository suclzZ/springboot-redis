package com.sucl.redis.sblock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

/**
 * @author sucl
 * @date 2019/5/26
 */
public class SbRedisLock {
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 加锁
     * 如果多个系统时间不一致，导致A系统取锁的时间与B系统去取锁的时间刚好一致，那么在getAndSet时很可能本该由A取的锁被B拿到了，
     * 那么后面的逻辑这没法保证一致性了
     * @param key
     * @param value 时间+超时时间
     */
    public boolean lock(String key, String value){
        //不存在则设置，并返回
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, value);
        if(ok){
            return  true;
        }
        Object _value = redisTemplate.opsForValue().get(key);
        //没有超时
        if(!StringUtils.isEmpty(_value) && Long.parseLong(_value.toString()) < System.currentTimeMillis()){
            Object oldValue = redisTemplate.opsForValue().getAndSet(key, value);
            if(!StringUtils.isEmpty(oldValue) && oldValue.equals(_value)){
                return  true;
            }
        }
        return false;
    }

    /**
     * 解锁
     * @param key
     * @param value
     */
    public void unLock(String key,String value){
        Object _value = redisTemplate.opsForValue().get(key);
        if(!StringUtils.isEmpty(_value) && _value.equals(value)){
            redisTemplate.delete(key);
        }
    }
}
