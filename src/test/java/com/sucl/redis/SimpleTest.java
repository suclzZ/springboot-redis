package com.sucl.redis;

import redis.clients.jedis.BinaryClient;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ZParams;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sucl
 * @date 2019/5/21
 */
public class SimpleTest {

    public static void main(String[] args) {
        Jedis jedis = new Jedis("localhost",6379);
        jedis.auth("123456");
        System.out.println("连接成功："+jedis);
//        System.out.println(jedis.isConnected());
        jedis.connect();

        System.out.println(jedis.set("key","value","NX","EX",3000));
        System.out.println(jedis.set("key","value","XX","EX",3000));

//        stringTest(jedis);
//        hashTest(jedis);
//        listTest(jedis);
//        setTest(jedis);
//        sortedSetTest(jedis);
    }

    /**
     * 可以做计数 incr decr
     * @param jedis
     */
    public static void stringTest(Jedis jedis){
        jedis.set("s1","hello");//存值
        System.out.println(jedis.get("s1"));//取值
        System.out.println(jedis.type("s1"));//key对应值的类型
        System.out.println(jedis.keys("*"));//查看所有key
        jedis.rename("s1","s11");//重命名key
        System.out.println(jedis.keys("*"));
        System.out.println(jedis.get("s11"));
        jedis.del("s11");//删除key
        System.out.println(jedis.keys("*"));
    }

    /**
     * k-v
     * @param jedis
     */
    public static void hashTest(Jedis jedis){
        jedis.hset("h1","hk1","hv1");
        jedis.hset("h1","hk1","hv2");//将上面的替换了
        System.out.println("keys : " +jedis.keys("*"));
        System.out.println(jedis.hget("h1","hk1"));
        jedis.hdel("h1","hk1");//删除,只有一个域，删除了则key也没了
//        jedis.del("h1");
        System.out.println("keys : " +jedis.keys("*"));

        Map<String, String> hash = new HashMap<>();
        hash.put("field1","v1");
        hash.put("field2","v2");
        jedis.hmset("h2",hash);
        System.out.println(jedis.hget("h2","field1"));
        System.out.println(jedis.hmget("h2","field1","field1"));
        jedis.hdel("h2","field1");
        System.out.println(jedis.hgetAll("h2"));
        jedis.del("h2");

        //是否存在
        jedis.hexists("key1", "field1");
        //返回哈希表key中的所有域
        jedis.hkeys("key1");
        //返回哈希表key中的所有值
        jedis.hvals("key1");
    }

    /**
     * 双向队列
     * 总数限制
     * @param jedis
     */
    public static void listTest(Jedis jedis){
        //排序v4 v3 v 2 v1
        jedis.lpush("l1","v1","v2","v3","v4");//往列表头插入,因此v4在第一个
        System.out.println(jedis.lindex("l1",0));
        System.out.println(jedis.lpop("l1"));//取出并删除第一个
        System.out.println(jedis.lrange("l1",1,5));//取值，如果超出范围值，则循环
        Long linsert = jedis.linsert("l1", BinaryClient.LIST_POSITION.BEFORE, "v2", "v99");

        System.out.println(jedis.lrange("l1",0,-1));

        jedis.del("l1");
    }

    /**
     * set 的内部实现是一个 value永远为null的HashMap
     * 非重复值，交并集
     * @param jedis
     */
    public static void setTest(Jedis jedis){
        jedis.sadd("s1", "value0");
        jedis.sadd("s1", "value1");//和上面的hash不同，set存储的的是k-vs
        System.out.println(jedis.smembers("s1"));

        //判断元素是否是集合key的成员
        System.out.println(jedis.sismember("s1", "value2"));

        //返回集合key的元素的数量
        System.out.println(jedis.scard("s1"));

        //返回一个集合的全部成员，该集合是所有给定集合的交集
        System.out.println(jedis.sinter("s1","s2"));

        //返回一个集合的全部成员，该集合是所有给定集合的并集
        System.out.println(jedis.sunion("s1","s2"));

        //返回一个集合的全部成员，该集合是所有给定集合的差集
        System.out.println(jedis.sdiff("s1","s2"));

        jedis.del("s1");
    }

    public static void sortedSetTest(Jedis jedis){
        jedis.zadd("z1",1,"v1");
        jedis.zadd("z1",1,"v2");
        jedis.zadd("z1",2,"v3");
        jedis.zadd("z1",3,"v4");
        jedis.zadd("z1",4,"v5");
        jedis.zadd("z1",5,"v6");
        System.out.println(jedis.zcard("z1"));
        System.out.println(jedis.zcount("z1",2,4));
        System.out.println( jedis.zrange("z1",0,-1));
        //为有序集 key 的成员 member 的 score 值加上增量 increment,如果member不存在，则新增
        System.out.println(jedis.zincrby("z1",1,"v1"));
        System.out.println( jedis.zrange("z1",0,-1));
        //指定区间数量 [* [*([a [b;[1 [2)
        System.out.println(jedis.zlexcount("z1","-", "+"));
        //将两个set的相同值的score相加，生成第三个set，没有的值则不管
        jedis.zadd("z2",5,"v1");
        jedis.zadd("z2",8,"v3");
        jedis.zinterstore("z3","z1","z2");
        System.out.println(jedis.zrange("z3",0,-1));
        jedis.zrem("z1","v1","v2");
        System.out.println(jedis.zrange("z1",0,-1));
//        System.out.println(jedis);

        jedis.del("z1","z2","z3");
    }

    public static void other(Jedis jedis){
        jedis.select(0);//redis一共有16个db，默认为0
        jedis.expire("key1",5);//存活时间
        jedis.ttl("key1");//剩余存活时间
        jedis.setnx("key2","10");//不存在则添加，存在不作处理

        // NX/XX,EXPX
        /**
         * setnx
         * NX 没有则设置,存在则返回null，不存在则返回OK；XX 存在则设置，返回OK,否则返回null
         * EX 秒;PX 毫秒
         */
        jedis.set("key","value","NX","EX",30);

        //....
    }
}
