package com.allen.demo;

/**
 * @Auther: allen
 * @Date: 2021-08-27 22:27
 * @Description: 告警功能管理类，Two-harse.ThreadOwner
 */
public class AlarmMgr {

    //告警管理类唯一实例
    private static final AlarmMgr INSTANCE = new AlarmMgr();

    private volatile boolean shutdownRequested = false;

    //告警发送线程
    private final AlarmSendingThread alarmSendingThread;

    public AlarmMgr() {
        alarmSendingThread = new AlarmSendingThread();
    }

    public static AlarmMgr getInstance() {
        return INSTANCE;
    }

    public int sendAlarm(AlarmType type, String id, String extraInfo) {
        int duplicationSubmissionCount = 0;

        try {
            AlarmInfo alarmInfo = new AlarmInfo( id, type);
            alarmInfo.setExtraInfo(extraInfo);
            duplicationSubmissionCount = alarmSendingThread.sendAlarm(alarmInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return duplicationSubmissionCount;
    }

    public void init() {
        alarmSendingThread.start();
    }

    public synchronized void shutdown() {
        if( shutdownRequested ) {
            throw new IllegalStateException("shutdown already requested!");
        }

        alarmSendingThread.terminate();
        shutdownRequested = true;
    }
}
