package com.equal;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.equal.client.MqttClientImpl;
import com.equal.controller.TestController;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@SpringBootApplication(scanBasePackages = {"com.yushu"})
public class CallGraphApplication {
    private static final JedisPool pool;
    public static MqttClientImpl mqttClientpublish;
    public ExecutorService executorService = Executors.newFixedThreadPool(4);

    static {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(10); // 最大连接数
        config.setMaxIdle(5);   // 最大空闲连接
        // 初始化连接池
        pool = new JedisPool(config, "192.168.101.64", 32679, 0, "bdtp@2022"); // 最后一个参数是密码
    }

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(CallGraphApplication.class, args);
        if (args.length==0){
            log.info("参数为空");
            return;
        }
        // parm appName=diagnosis
        String appName = args[0];
        log.info("解析对比appName={}",appName);
        TestController.rundiff(appName);
        //genMqttClient();
    }

    public static void genMqttClient(){
        mqttClientpublish = new MqttClientImpl("tcp://192.168.101.64:31883", "mqttx_tool");
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setUserName("admin");
        mqttConnectOptions.setPassword("XXXX".toCharArray());
        mqttClientpublish.connect(mqttConnectOptions);
        System.out.println("mqtt send start");
    }
}
