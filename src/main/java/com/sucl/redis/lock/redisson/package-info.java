/**
 * 其实上面那种方案最大的问题，就是如果你对某个redis master实例，写入了myLock这种锁key的value，此时会异步复制给对应的master slave实例。
 *
 * 但是这个过程中一旦发生redis master宕机，主备切换，redis slave变为了redis master。
 *
 * 接着就会导致，客户端2来尝试加锁的时候，在新的redis master上完成了加锁，而客户端1也以为自己成功加了锁。
 *
 * 此时就会导致多个客户端对一个分布式锁完成了加锁。
 *
 * 这时系统在业务语义上一定会出现问题， 导致各种脏数据的产生 。
 *
 * 所以这个就是redis cluster，或者是redis master-slave架构的 主从异步复制 导致的redis分布式锁的最大缺陷：在redis master实例宕机的时候，可能导致多个客户端同时完成加锁
 *
 * @author sucl
 * @date 2019/5/24
 */
package com.sucl.redis.lock.redisson;