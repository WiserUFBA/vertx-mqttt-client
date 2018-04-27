/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufba.dcc.wiser;

/**
 *
 * @author elisama
 */
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttTopicSubscription;
import java.util.ArrayList;
import java.util.List;

public class VertxMQTTServer extends AbstractVerticle {

    public static void main(String... args) {

        final Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new VertxMQTTServer());
    }

    @Override
    public void start(final Future<Void> startFuture) throws Exception {

        final MqttServer mqttServer = MqttServer.create(vertx);
        mqttServer.endpointHandler(endpoint -> {
            System.out.printf("MQTT client [%s] request to ct, clean session = %s %n",
                    endpoint.clientIdentifier(),
                    endpoint.isCleanSession());

            endpoint.subscribeHandler(subscribe -> {

                List<MqttQoS> grantedQosLevels = new ArrayList<>();
                for (MqttTopicSubscription s : subscribe.topicSubscriptions()) {
                    System.out.println("Subscription for " + s.topicName() + " with QoS " + s.qualityOfService());
                    grantedQosLevels.add(s.qualityOfService());
                }
                // ack the subscriptions request
                endpoint.subscribeAcknowledge(subscribe.messageId(), grantedQosLevels);

//// specifing handlers for handling QoS 1 and 2
                endpoint.publishAcknowledgeHandler(messageId -> {

                    System.out.println("Received ack for message = " + messageId);

                }).publishReceivedHandler(messageId -> {

                    endpoint.publishRelease(messageId);

                }).publishCompletionHandler(messageId -> {

                    System.out.println("Received ack for message = " + messageId);
                });
            });

            endpoint.publishHandler(message -> {
                //final JsonObject msg = message.payload().toJsonObject();
                String msg = message.payload().toString();

                System.out.println("Just received message on [" + message.topicName() + "] payload [" + message.payload() + "] with QoS [" + message.qosLevel() + "]");

                endpoint.publish(message.topicName(),
                        message.payload(),
                        message.qosLevel(),
                        false,
                        false);

                //TODO react to the message
                if (message.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
                    endpoint.publishAcknowledge(message.messageId());
                } else if (message.qosLevel() == MqttQoS.EXACTLY_ONCE) {
                    endpoint.publishRelease(message.messageId());
                }
            }).publishReleaseHandler(endpoint::publishComplete);
            endpoint.accept(false);

        }).listen(ar -> {
            if (ar.succeeded()) {
                System.out.println("MQTT server is listening on port " + ar.result().actualPort());
                startFuture.complete();
            } else {
                System.out.println("Error on starting the server");
                startFuture.fail(ar.cause());
            }
        });
    }
}
