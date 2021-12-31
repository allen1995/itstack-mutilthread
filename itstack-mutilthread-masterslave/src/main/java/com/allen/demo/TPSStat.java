package com.allen.demo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Auther: 20190598
 * @Date: 2021/12/31 16:37
 * @Description:
 */
public class TPSStat {

    public static void main(String[] args) {
        // 接口日志文件所在目录
        String logBaseDir = args[0];

        // 忽略的操作名列表
        String excludedOperationNames = "";

        // 指定要统计在内的操作名列表
        String includedOperationNames = "*";

        // 指定要统计在内的目标设备名
        String destinationSysNames = "*";


        int argc = args.length;

        if( argc >= 2 ) {
            excludedOperationNames = args[1];
        }

        if( argc >= 3 ) {
            includedOperationNames = args[2];
        }

        if( argc >= 4 ) {
            destinationSysNames = args[3];
        }

        Master processor = new Master(logBaseDir, excludedOperationNames, includedOperationNames, destinationSysNames);

        BufferedReader fileNamesReader = new BufferedReader(new InputStreamReader(System.in));
        ConcurrentMap<String, AtomicInteger> result = processor.calculate(fileNamesReader);

        for( String timeRange : result.keySet() ) {
            System.out.println(timeRange + "," + result.get(timeRange));
        }
    }
}
