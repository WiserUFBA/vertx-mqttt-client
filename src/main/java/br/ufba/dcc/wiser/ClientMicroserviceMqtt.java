package br.ufba.dcc.wiser;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.buffer.Buffer;

import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mqtt.MqttConnectionException;
import io.vertx.mqtt.MqttException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  *
 * @author Cleber Lira
 * @version 1.0
 * @since 01242018
 */
public class ClientMicroserviceMqtt extends AbstractVerticle {

    public static final String MQTT_SERVER_HOST = "192.168.0.25";
    public static final int MQTT_SERVER_PORT = 1883;
    private static int counter;

    private static final Logger LOG = LoggerFactory.getLogger(ClientMicroserviceMqtt.class);

    @Override
    public void start() {

        MqttClientOptions opt = new MqttClientOptions();
        
        opt.setUsername("karaf");
        opt.setPassword("karaf");
        MqttClient client = MqttClient.create(vertx,opt );

        
        
        System.out.println("Publicando em" + MQTT_SERVER_HOST);
        client.connect(MQTT_SERVER_PORT, MQTT_SERVER_HOST, s -> {

            client.publishHandler(s1 -> {

                System.out.println("There are new message in topic: " + s1.topicName());
                System.out.println("Content(as string) of the message: " + s1.payload().toString());
                System.out.println("QoS: " + s1.qosLevel());

                LOG.info("There are new message in topic: " + s1.topicName());
                LOG.info("Content(as string) of the message: " + s1.payload().toString());
                LOG.info("QoS: " + s1.qosLevel());

            })
          .subscribe("REACTIVE", 0);

            vertx.setPeriodic(1000, time -> {

                client.publish("REACTIVE",
                        Buffer.buffer("reactive microservices" + counter++),
                        MqttQoS.AT_LEAST_ONCE,
                        false,
                        false);

            });

            // client.disconnect();
            // vertx.close();
        });

      
    }
}
