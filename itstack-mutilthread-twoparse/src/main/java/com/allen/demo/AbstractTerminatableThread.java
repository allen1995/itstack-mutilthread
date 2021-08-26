package com.allen.demo;

/**
 * @Auther: 20190598
 * @Date: 2021/8/26 17:02
 * @Description:
 */
public abstract class AbstractTerminatableThread extends Thread implements Terminatable {

    /**
     * 线程间共享的线程终止标志实例
     */
    public final TerminationToken terminationToken;

    public AbstractTerminatableThread() {
        this(new TerminationToken());
    }

    /**
     *
     * @param terminationToken:
     */
    public AbstractTerminatableThread(TerminationToken terminationToken) {
        super();
        this.terminationToken = terminationToken;
        terminationToken.register(this);
    }

    @Override
    public void terminate() {
        terminationToken.setToShutdown(true);

        try {
            doTerminate();
        } finally {

            //若无处理的任务，则试图强制终止线程
            if( terminationToken.reservations.get() <= 0 ) {
                super.interrupt();
            }
        }
    }

    public void terminate(boolean waitUtilThreadTerminated) {
        terminate();

        if( waitUtilThreadTerminated ) {
            try {
                this.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    protected abstract void doRun() throws Exception;

    protected void doCleanup(Exception cause) {

    }

    protected void doTerminate() {

    }

    @Override
    public void run() {
        Exception ex = null;

        try {
            for(;;) {

                /**
                 * 运行程序业务逻辑前先判断线程终止状态
                 */
                if( terminationToken.isShutdown()
                    && terminationToken.reservations.get() <= 0 ) {
                    break;
                }

                doRun();
            }
        } catch (Exception e) {
            ex = e;
        } finally {
        }
    }

    @Override
    public void interrupt() {
        terminate();
    }

}
