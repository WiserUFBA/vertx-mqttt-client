/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufba.dcc.wiser;

/**
 *
 * @author Cleber Lira
 */
import io.vertx.core.Vertx;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


import java.io.IOException;


public class PublisherPaho {

   
    private static int counter = 0;

    public static void main(String[] args) {

        Vertx vertx = Vertx.vertx();

        try {

            MemoryPersistence persistence = new MemoryPersistence();
            MqttClient client = new MqttClient(String.format("tcp://%s:%d", "localhost", 1883), "pub-0", persistence);

            client.connect();

            vertx.setPeriodic(1000, time -> {

                try {
                    client.publish("REACTIVE",
                            ("reactive microservices" + counter++).getBytes(),
                            1,
                            false);
                } catch (MqttException ex) {
                    ex.printStackTrace();
                }

            });

            System.in.read();
            client.disconnect();

            vertx.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
