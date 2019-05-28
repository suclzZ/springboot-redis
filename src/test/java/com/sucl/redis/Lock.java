package com.sucl.redis;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author sucl
 * @date 2019/5/24
 */
public class Lock {
    private boolean islock;

    public synchronized void getLock(){
        while (islock){
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        islock = true;
    }

    public synchronized void reloeadLock(){
        if(islock){
            notifyAll();
        }
        islock = false;
    }

    public static void main(String[] args){
        //10 个测试线程
        for(int i=0;i<10;i++){
            new Thread(()->{
                try {
                    test();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }

    }

    public static void test() throws InterruptedException {
        Lock lock = new Lock();
        Value value = lock.new Value(0);
        List<Thread> threads = new ArrayList<>();
        //10个线程竞争
        for(int i=0;i<10;i++){
            threads.add(new Thread(() -> {
                lock.incr(value);
            }));
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

        System.out.println("value : "+value.getI());
    }

    public void incr(Value val){
        getLock();
        try {
            TimeUnit.MILLISECONDS.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int i1 = val.getI(),i2 = i1+1;
        val.setI(i2);
        reloeadLock();
    }

    @Data
    private class Value{
        private int i;

        public Value(int i){
            this.i = i;
        }
    }
}
