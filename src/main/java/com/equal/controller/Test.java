package com.equal.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.equal.domain.TypeInfo;
import com.equal.utils.InterfaceRequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.equal.CallGraphApplication.mqttClientpublish;

@Slf4j
@RestController
@RequestMapping("/test")
public class Test {
    private static final JedisPool pool;

    static {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(10); // 最大连接数
        config.setMaxIdle(5);   // 最大空闲连接
        // 初始化连接池
        pool = new JedisPool(config, "192.168.101.64", 32679, 0, "bdtp@2022"); // 最后一个参数是密码
    }

    public static Map<String, TypeInfo> readPoinInfo(){
        File file = new File("信息点表.xlsx");
        List<TypeInfo> listRule = EasyExcel.read(file).head(TypeInfo.class).sheet(0).doReadSync();
        Map<String, TypeInfo> dicMap = listRule.stream().distinct().collect(Collectors.toMap(info -> info.getClassCode()+"_"+info.getCode(), Function.identity()));

        return dicMap;
    }
    @PostMapping("query")
    public String queryredis(@RequestBody List<String> list) throws InterruptedException {
        log.info("start.............");
        Map<String, TypeInfo> stringTypeInfoMap = readPoinInfo();
        // 使用 try-with-resources：无论代码正常还是报错，jedis 都会自动归还给 pool
        try (Jedis jedis = pool.getResource()) {
            jedis.select(3);
            System.out.println("成功连接 Redis，正在查询...");
            Set<String> keys = new HashSet<>();
            if (list.size()==0){
                list = Arrays.asList(
                        "Eq110101001178a9714ad2b74abb9c0b22e5e73daebb",
                        "Eq11010100116aa743b7447e4bf78b930e80bebcd4ff",
                        "Eq1101010011c816ac17b1ec4b5ebb44e935966db4d8",
                        "Eq1101010011b74ae60827244742af91813945c6f510",
                        "Eq11010100113e922e9cfc6d47f09b5a9140836b3a55",
                        "Eq11010100117b2679128a9245529d9267bfd5c54617",
                        "Eq1101010011237a1f09cb0843d1990b64ae2b339260",
                        "Eq11010100117588687cda16451db3bd8f7a49b68302",
                        "Eq110101001174c32f913fe54583a246fb311dd8585a",
                        "Eq110101001111301e7e0ff840629aca6d596143fd61",
                        "Eq110101001176d949ae195040958e60f40ada1b9c02",
                        "Eq1101010011c70d6d3abd5d45e5ba62f81ed6d5a87d",
                        "Sp1101010011ec92825b2f2e48abae8e516ed998ae7d",
                        "Sp1101010011f5cfc0d9d3fd4ed29ce7075e31b491f4",
                        "Sp11010100110f0a12e942ab4c07abd99714713393cf",
                        "Sp11010100113d3fb945f1ac49ed8b85c1a3d95a785a",
                        "Sp1101010011002007c48ebf490ea7de28ede7c9894c");
            }
            for (String s : list) {
                Set<String> key = jedis.keys("twin:data:"+s+":*"); // 查找所有以 device: 开头的键
                keys.addAll(key);
            }
            System.out.println("匹配到的 Keys: " + keys.size());
            for (String key : keys) {
                String myValue = jedis.get(key);
                String[] split = myValue.split(",");
                String classCode = split[2];
                String objectId = split[3];
                String infoCode = split[4];
                String infoValue = split[8];

                TypeInfo typeInfo = stringTypeInfoMap.get(classCode + "_" + infoCode);
                if (typeInfo!=null){
                    String dataSource = typeInfo.getDataSource();
                    if (StringUtils.isNotBlank(dataSource)){
                        JSONArray array = JSON.parseArray(dataSource);
                        HashMap<String, String> map = new HashMap<>();
                        int size = array.size();
                        for (int i = 0; i < size; i++) {
                            JSONObject jsonObject = array.getJSONObject(i);
                            map.put(jsonObject.getString("code"),jsonObject.getString("name"));

                        }
                        log.info(classCode +"\t"+objectId+"\t"+ infoCode+"\t"+typeInfo.getName()+"\t"+infoValue+"\t"+map);
                    }else {
                        log.info(classCode +"\t"+objectId+"\t"+ infoCode+"\t"+typeInfo.getName()+"\t"+infoValue);

                    }
                }


            }
        } catch (Exception e) {
            // 不要只打 e.getMessage()，否则容易看到 "null"
            System.err.println("发生错误！详细堆栈如下：");
            e.printStackTrace();
        }
        return "ok";
    }
    @PostMapping("setTwinValue")
    public String setTwinValue(@RequestBody String par) throws InterruptedException {
        String url = "https://br-twin.meosedge.com/dtp-twin-server-edge/twc/auto/send";
        /**
         [
            {
                "objectId":"Sp1101010011f5cfc0d9d3fd4ed29ce7075e31b491f4",
                "params":[
                    {"spaceHumidityLowThreshold":"25"},
                    {"spaceHumidityHighThreshold":"75"}

                ]
            },
            {
                "objectId":"Sp1101010011002007c48ebf490ea7de28ede7c9894c",
                "params":[
                    {"spaceHumidityLowThreshold":"25"},
                    {"spaceHumidityHighThreshold":"75"}

                ]
            }
         ]
         */
        JSONArray jsonArray = JSON.parseArray(par);
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String objectId = jsonObject.getString("objectId");
            JSONArray paramsArrary = jsonObject.getJSONArray("params");
            for (int i1 = 0; i1 < paramsArrary.size(); i1++) {
                JSONObject params = paramsArrary.getJSONObject(i1);
                JSONObject postBody = new JSONObject();
                postBody.put("objectId",objectId);
                postBody.put("params",params);
                InterfaceRequestUtil.doPost(url,postBody.toJSONString(),"aaa");
                Thread.sleep(5000);
            }
        }
        return "ok";
    }

    @PostMapping("setSpaceValue")
    public String setValue(@RequestBody String par) throws InterruptedException {
        /**
         [
         {
         "objectId": "Sp1101010011f5cfc0d9d3fd4ed29ce7075e31b491f4",
         "command": {
         "infoCode": "tdb",
         "val": "23"
         }
         },
         {
         "objectId": "Sp1101010011f5cfc0d9d3fd4ed29ce7075e31b491f4",
         "command": {
         "infoCode": "tdb",
         "val": "23"
         }
         }
         ]
         */

        JSONArray jsonArray = JSON.parseArray(par);
        List<String> msg = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String objectId = jsonObject.getString("objectId");
            JSONObject command = jsonObject.getJSONObject("command");
            String infoCode = command.getString("infoCode");
            String val = command.getString("val");
            String s = genStrMqtt(objectId, infoCode, val);
            msg.add(s);
        }
        for (String s : msg) {
            log.info("send time is {}  msg is : {}", System.currentTimeMillis(), s);
            MqttMessage mqttMessage = new MqttMessage(s.getBytes());
            mqttMessage.setQos(0);
            mqttClientpublish.publish("command/Pj2025122415/dtp", mqttMessage);
        }

        return "ok";
    }


    private String orStr = "  {\n" +
            "  \"cmd\": \"set\",\n" +
            "  \"groupCode\": \"LCZYEG\",\n" +
            "  \"projectId\": \"Pj2025122415\",\n" +
            "  \"classCode\": \"GeneralZone\",\n" +
            "  \"gwId\": \"bacnet\",\n" +
            "  \"objectId\": \"##objectId\",\n" +
            "  \"reportTime\": ##time,\n" +
            "  \"msgId\": \"##msgId\",\n" +
            "  \"sessionId\": \"##sessionId\",\n" +
            "  \"command\": {\n" +
            "        \"infoCode\": \"##infoCode\",\n" +
            "        \"val\": \"##val\"\n" +
            "          }\n" +
            "}";
    public String genStrMqtt(String objectId,String infoCode,String value){
        String st = orStr.replaceAll("##objectId",objectId).replaceAll("##time",getTimeStr()).replaceAll("##msgId",getMsgAndSessionIdStr()).replaceAll("##sessionId",getMsgAndSessionIdStr()).replaceAll("##infoCode",infoCode).replaceAll("##val",value).replaceAll("\\n","");
        return st;
    }
    private String getTimeStr(){
        return String.valueOf(System.currentTimeMillis());
    }

    private String getMsgAndSessionIdStr(){
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replaceAll("-","");
    }

}
