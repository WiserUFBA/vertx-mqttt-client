package br.ufba.dcc.wiser;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;

import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  *
 * @author Cleber Lira
 * @version 1.0
 * @since 01242018
 */
public class ClientMicroserviceMqtt extends AbstractVerticle {

    public static final String MQTT_SERVER_HOST = "localhost";
    public static final int MQTT_SERVER_PORT = 1883;

    private static final Logger LOG = LoggerFactory.getLogger(ClientMicroserviceMqtt.class);

    @Override
    public void start() {

        MqttClientOptions options = new MqttClientOptions();

        MqttClient client = MqttClient.create(vertx);

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

            client.publish("REACTIVE",
                    Buffer.buffer("reactive microservices"),
                    MqttQoS.AT_MOST_ONCE,
                    true,
                    true);

            // client.disconnect(); 
        });

        options.setAutoKeepAlive(false);

    }

}