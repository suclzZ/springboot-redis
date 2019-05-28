package com.sucl.redis.lock;

import redis.clients.jedis.Jedis;

import java.util.*;

/**
 * 通过redis setnx 实现分布式锁
 *原理分析：
 *  1、加锁
 *      如果redis里有该键，则取锁失败，进行阻塞
 *  2、开锁
 *      如果redis有键，则删除该键，否则无需释放锁
 *可靠性：
 *  1、互斥性。在任意时刻，只有一个客户端能持有锁。
 *  2、不会发生死锁。即使有一个客户端在持有锁的期间崩溃而没有主动解锁，也能保证后续其他客户端能加锁。
 *  3、具有容错性。只要大部分的Redis节点正常运行，客户端就可以加锁和解锁。
 *  4、解铃还须系铃人。加锁和解锁必须是同一个客户端，客户端自己不能把别人加的锁给解了。
 *问题：
 *  1、如果竞争较激烈，加锁时可能存在长时间阻塞，可以添加超时时间，但是要保证业务正常执行，需要自动延期
 *  2、如果拿到锁之后由于各种原因一直没有释放锁，导致其他业务无法进行，添加超时时间，超过时间则自动释放锁，但要保证当前业务正常
 *  3、如果redis故障，锁失效，如果用其他手段保证系统可靠性
 *  4、
 *
 * @author sucl
 * @date 2019/5/24
 */
public class RedisLock {
    //场景 锁名
    private String scene;
    private long timeout = 30;
    private static String TIME_UNIT  = "EX";//PX：毫秒

    public RedisLock(){
        this.scene = UUID.randomUUID().toString();
    }

    public RedisLock(String name){
        this.scene = name;
    }

    public void lock(String clientId){
        long lockTimeout = 10*1000;//如果10秒没有获取锁，则返回
        long start = System.currentTimeMillis();
        while (!getLock1(clientId)){
            if(start+lockTimeout<=System.currentTimeMillis()){
                return;
            }
        }
//        Thread t1 = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (jedis().ttl(scene)<1){
//                    //续期
//                    extraTime(clientId);
//                }
//            }
//        });
//        t1.setDaemon(true);
        //TODO 业务代码
//        releaseLock(clientId);
    }

    public static Jedis jedis(){
        Jedis jedis = new Jedis("localhost",6379);
        jedis.auth("123456");
        return jedis;
    }

    public boolean getLock1(String clientId){
        //多系统需要保证时间一致
        //多客户端同时加锁，setnx会出现混乱，其他客户端可能会覆盖
        //没有客户端标识
//        Long r = jedis().setnx(scene, String.valueOf(new Date().getTime()));//没有超时时间，设置为值
//        if(r==1){
//            jedis().expire(scene,30);//如果这里异常了，则死锁了
//        }


        String v = jedis().set(scene, clientId, "nx", TIME_UNIT, timeout);//30秒超时
        //如果没有值，且设置成功，则返回OK，否则返回null
        if("OK".equals(v)){
            return true;
        }
        return false;
    }

    public static boolean getLock2( String lockKey, int expireTime) {

        long expires = System.currentTimeMillis() + expireTime;
        String expiresStr = String.valueOf(expires);

        // 如果当前锁不存在，返回加锁成功
        if (jedis().setnx(lockKey, expiresStr) == 1) {
            return true;
        }

        // 如果锁存在，获取锁的过期时间
        String currentValueStr = jedis().get(lockKey);
        if (currentValueStr != null && Long.parseLong(currentValueStr) < System.currentTimeMillis()) {
            // 锁已过期，获取上一个锁的过期时间，并设置现在锁的过期时间
            String oldValueStr = jedis().getSet(lockKey, expiresStr);
            if (oldValueStr != null && oldValueStr.equals(currentValueStr)) {
                // 考虑多线程并发的情况，只有一个线程的设置值和当前值相同，它才有权利加锁
                return true;
            }
        }

        // 其他情况，一律返回加锁失败
        return false;
    }

    public boolean releaseLock(String clientId){
//        if (clientId.equals(jedis().get(scene))) {
//            // 若在此时，这把锁突然不是这个客户端的，则会误解锁，比如因为超时被删除，其他 客户端拿到了锁，那么则相当于把其他客户端的锁删除了
//            jedis().del(scene);
//        }


        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis().eval(script, Collections.singletonList(scene), Collections.singletonList(clientId));

        if ("1".equals(result.toString())) {
            return true;
        }
        return false;
    }

    /**
     * 延期
     * @param clientId
     */
    public void extraTime(String clientId){
        jedis().set(scene, clientId, "xx", TIME_UNIT, timeout);//30秒超时
    }

    /**
     * 10个线程模拟10个应用
     */
    public static void useLock(){
        List<Thread> threads = new ArrayList<>();
        Random r = new Random();
        for(int i=0;i<10;i++){
            Thread app = new Thread(()->{
                RedisLock lock = new RedisLock("lock");
                lock.lock(lock.hashCode()+"");

                double v = r.nextDouble();
                System.out.println("设值："+v);
                RedisLock.jedis().set("key1",v+"");
                RedisLock.jedis().set("key2",v+1+"");

                lock.releaseLock(lock.hashCode()+"");
            },i+"");
            threads.add(app);
        }
        threads.forEach(t->{
            t.start();
        });
        threads.forEach(t->{
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        System.out.println(RedisLock.jedis().get("key1"));
        System.out.println(RedisLock.jedis().get("key2"));
    }

    public static void main(String[] args) {
//        RedisLock lock = new RedisLock("lock");
        useLock();
    }
}
