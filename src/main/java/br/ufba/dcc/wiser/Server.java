/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufba.dcc.wiser;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttTopicSubscription;
import io.vertx.mqtt.messages.MqttMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * An example of using the MQTT server
 */
public class Server extends AbstractVerticle {

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        Runner.runExample(Server.class);
    }

    @Override
    public void start() throws Exception {

        MqttServer mqttServer = MqttServer.create(vertx);

        mqttServer
                .endpointHandler(endpoint -> {

                    // shows main connect info
                    System.out.println("MQTT client [" + endpoint.clientIdentifier() + "] request to connect, clean session = " + endpoint.isCleanSession());

                    if (endpoint.auth() != null) {
                        System.out.println("[username = " + endpoint.auth().userName() + ", password = " + endpoint.auth().password() + "]");
                    }
                    if (endpoint.will() != null) {
                        System.out.println("[will flag = " + endpoint.will().isWillFlag() + " topic = " + endpoint.will().willTopic() + " msg = " + endpoint.will().willMessage()
                                + " QoS = " + endpoint.will().willQos() + " isRetain = " + endpoint.will().isWillRetain() + "]");
                    }

                    System.out.println("[keep alive timeout = " + endpoint.keepAliveTimeSeconds() + "]");

                    // accept connection from the remote client
                    endpoint.accept(false);

                    // handling requests for subscriptions
                    endpoint.subscribeHandler(subscribe -> {

                        List<MqttQoS> grantedQosLevels = new ArrayList<>();
                        for (MqttTopicSubscription s : subscribe.topicSubscriptions()) {
                            System.out.println("Subscription for " + s.topicName() + " with QoS " + s.qualityOfService());
                            grantedQosLevels.add(s.qualityOfService());
                        }
                      
                        // ack the subscriptions request
                        endpoint.subscribeAcknowledge(subscribe.messageId(), grantedQosLevels);

                        

                        

// specifing handlers for handling QoS 1 and 2
                        endpoint.publishAcknowledgeHandler(messageId -> {

                            
                            System.out.println("Received ack for message = " + messageId);

                        }).publishReceivedHandler(messageId -> {

                            endpoint.publishRelease(messageId);

                        }).publishCompletionHandler(messageId -> {

                            System.out.println("Received ack for message = " + messageId);
                        });
                    });

                    // handling requests for unsubscriptions
                    endpoint.unsubscribeHandler(unsubscribe -> {

                        for (String t : unsubscribe.topics()) {
                            System.out.println("Unsubscription for " + t);
                        }
                        // ack the subscriptions request
                        endpoint.unsubscribeAcknowledge(unsubscribe.messageId());
                    });

                    // handling ping from client
                    endpoint.pingHandler(v -> {

                        System.out.println("Ping received from client");
                    });

                    // handling disconnect message
                    endpoint.disconnectHandler(v -> {

                        System.out.println("Received disconnect from client");
                    });

                    // handling closing connection
                    endpoint.closeHandler(v -> {

                        System.out.println("Connection closed");
                    });

                    // handling incoming published messages
                    endpoint.publishHandler(message -> {

                       
                        System.out.println("Just received message on [" + message.topicName() + "] payload [" + message.payload() + "] with QoS [" + message.qosLevel() + "]");

                        if (message.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
                            endpoint.publishAcknowledge(message.messageId());
                            
                            System.out.println("aqui");
                            
                        } else if (message.qosLevel() == MqttQoS.EXACTLY_ONCE) {
                            endpoint.publishReceived(message.messageId());
                        }
                        
                 
                    }).publishReleaseHandler(messageId -> {
                        endpoint.publishComplete(messageId);
                    });
                    
                    
                    
                })
                .listen(1883, "0.0.0.0", ar -> {

                    if (ar.succeeded()) {
                        System.out.println("MQTT server is listening on port " + mqttServer.actualPort());
                    } else {
                        System.err.println("Error on starting the server" + ar.cause().getMessage());
                    }
                });
    }
}
