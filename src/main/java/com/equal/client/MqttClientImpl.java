package com.equal.client;

import lombok.Data;
import org.eclipse.paho.client.mqttv3.*;

@Data
public class MqttClientImpl {

    private MqttClient client;


    public MqttClientImpl(String broker, String clientId) {
        try {
            client = new MqttClient(broker, clientId);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    public void connect(MqttConnectOptions connOpts){
        try {
            client.connect(connOpts);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }
    public void subscribe(String topic){
        try {
            client.subscribe(topic);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    public void publish(String topic,MqttMessage message){
        try {
            client.publish(topic,message);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    public void setCallBack(MqttCallbackExtended callBack){
        client.setCallback(callBack);
    }

}
