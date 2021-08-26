package com.allen.demo;

import java.lang.ref.WeakReference;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Auther: 20190598
 * @Date: 2021/8/26 16:41
 * @Description:
 */
public class TerminationToken {

    /**
     * volatile修饰保证内存可见性
     */
    protected volatile boolean toShutdown = false;

    public final AtomicInteger reservations =  new AtomicInteger(0);

    /**
     * 在多个可停止的多线程实例共享一个TerminationToken的情况下，用来保存Terminatable对象，减少锁的使用
     */
    private final Queue<WeakReference<Terminatable>> coordinatedThreads;

    public TerminationToken() {
        this.coordinatedThreads = new ConcurrentLinkedQueue<>();
    }

    public boolean isShutdown() {
        return toShutdown;
    }

    protected void setToShutdown(boolean toShutdown) {
        this.toShutdown = toShutdown;
    }

    protected void register(Terminatable thread) {
        coordinatedThreads.add(new WeakReference<Terminatable>(thread));
    }

    protected void notifyThreadTermination(Terminatable thread) {
        WeakReference<Terminatable> wrThread;

        Terminatable otherThread;

        while( null != (wrThread = coordinatedThreads.poll())) {
            otherThread = wrThread.get();

            if( otherThread != null && otherThread != thread ) {
                otherThread.terminate();
            }
        }
    }

}
