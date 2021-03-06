package com.allen.demo;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Auther: 20190598
 * @Date: 2021/8/26 17:22
 * @Description: Two-phaseTermination.ConcreteTerminatableThread
 */
public class AlarmSendingThread  extends AbstractTerminatableThread{
    private final AlarmAgent alarmAgent = new AlarmAgent();

    /**
     * 告警队列
     */
    private final BlockingQueue<AlarmInfo> alarmQueue;

    private final ConcurrentMap<String, AtomicInteger> submittedAlarmRegistry;

    public AlarmSendingThread() {
        this.alarmQueue = new ArrayBlockingQueue<AlarmInfo>(100);
        this.submittedAlarmRegistry = new ConcurrentHashMap<>();
    }

    @Override
    protected void doRun() throws Exception {
        AlarmInfo alarm;

        alarm = alarmQueue.take();
        terminationToken.reservations.getAndDecrement();

        try {
            //将告警发送到告警服务器
            alarmAgent.sendAlarm(alarm);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /**
         * 处理恢复告警，将相应的故障告警从注册表中删除，使得相应故障恢复后若再次出现相同故障，
         * 该故障能上报到服务器
         */
        if( AlarmType.RESUME == alarm.type) {
            String key = AlarmType.FAULT.toString() + ":" + alarm.getId();

            submittedAlarmRegistry.remove(key);

            key = AlarmType.RESUME.toString() + ":" + alarm.getId() + "@"
                    + alarm.getExtraInfo();
            submittedAlarmRegistry.remove(key);
        }

    }

    public int sendAlarm(final  AlarmInfo alarmInfo) {
        AlarmType type = alarmInfo.type;
        String id = alarmInfo.getId();
        String extraInfo = alarmInfo.getExtraInfo();

        if( terminationToken.isShutdown() ) {
            //记录告警
            System.err.println("reject id:" + id + "," + extraInfo);
            return -1;
        }

        int duplicationSubmissionCount = 0;

        try {
            AtomicInteger prevSubmittedCounter;

            prevSubmittedCounter = submittedAlarmRegistry.putIfAbsent(
                    type.toString()+":"+ id + + '@' + extraInfo, new AtomicInteger(0));

            if( prevSubmittedCounter != null ) {
                terminationToken.reservations.getAndIncrement();
                alarmQueue.put(alarmInfo);
            } else {
                // 故障未恢复，不用重复发送告警信息给服务器，故仅增加计数
                duplicationSubmissionCount = prevSubmittedCounter.incrementAndGet();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return duplicationSubmissionCount;
    }

    @Override
    protected void doCleanup(Exception cause) {
        if( cause != null && !(cause instanceof InterruptedException)) {
            cause.printStackTrace();
        }

        alarmAgent.disconnect();
    }
}
