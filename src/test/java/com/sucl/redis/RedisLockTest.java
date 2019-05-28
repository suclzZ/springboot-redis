package com.sucl.redis;

import redis.clients.jedis.Jedis;

/**
 * @author sucl
 * @date 2019/5/24
 */
public class RedisLockTest implements Runnable{
    private String price;
    private boolean isLock;

    public RedisLockTest(String price){
        this.price = price;
    }

    public static Jedis jedis(){
        Jedis jedis = new Jedis("localhost",6379);
        jedis.auth("123456");
        return jedis;
    }

    public static void main(String[] args) throws InterruptedException {
        jedis().set("price", "1000");
        System.out.println("初始价格："+jedis().get("price"));

        //两个线程修改价格，模拟分布式场景
        Thread t1 = new Thread(new RedisLockTest("1200"));
        Thread t2 = new Thread(new RedisLockTest("1500"));
        t1.start();t2.start();

        t1.join();t2.join();

        /**
         * 如果没有锁 你会发现有以下N中情况出现：
         *  价格改为1200，可以改变值为200；
         *  价格改为1500，可以改变值为500；
         *  价格改为1200，可以改变值为500；
         *  价格改为1500，可以改变值为200；
         *  ...
         *  加锁后预期值：
         *  价格改为1200，可以改变值为-300；
         *  价格改为1500，可以改变值为300；
         */
        System.out.println("修改后的价格："+jedis().get("price"));
        System.out.println("修改价格："+jedis().get("change"));

    }

    @Override
    public void run() {
        getLock();
        System.out.println("设置价格为："+this.price);
        String oldPrice = jedis().get("price");
        jedis().set("price",this.price);
        jedis().set("change",String.valueOf(Integer.valueOf(this.price)-Integer.valueOf(oldPrice)));
        releaseLock();
    }

    public synchronized void getLock(){
        String value = jedis().set("priceLock", "lock", "NX", "EX", 30);
        while (!"OK".equals(value)){
            value = jedis().set("priceLock", "lock", "NX", "EX", 30);
        }
        isLock = true;
    }

    public synchronized void releaseLock(){
//        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
//        Object result = jedis().eval(script, Collections.singletonList("priceLock"), Collections.singletonList(""));
        if(isLock){
            Long result = jedis().del("priceLock");
            if (1 == result) {
                isLock = false;
            }
        }

    }
}
