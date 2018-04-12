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
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class PublisherPaho implements MqttCallback {

    private static int counter = 0;

    public PublisherPaho() {

      
        try {

            MemoryPersistence persistence = new MemoryPersistence();
            MqttClient client = new MqttClient(String.format("tcp://%s:%d", "localhost", 1883), "pub-0", persistence);

            MqttConnectOptions opt = new MqttConnectOptions();

            opt.setCleanSession(false);
            
            opt.setKeepAliveInterval(30);
	    opt.setAutomaticReconnect(true);
        
            client.setCallback(this);

            client.connect(opt);
            
            client.subscribe("topic", 1);
		     
            
            client.publish("topic",
                          ("reactive microservices client paho " + counter++).getBytes(),
                           1,
                            false);
            
            System.out.println("Connected to the broker.. ");
            

            System.in.read();
        //    client.disconnect();

          

        } catch (IOException | MqttException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws MqttException {

        PublisherPaho p = new PublisherPaho();

    }

    @Override
    public void connectionLost(Throwable thrwbl) {
        Vertx vertx = Vertx.vertx();

        try {

            MemoryPersistence persistence = new MemoryPersistence();
            MqttClient client = new MqttClient(String.format("tcp://%s:%d", "localhost", 1883), "pub-0", persistence);

            MqttConnectOptions opt = new MqttConnectOptions();

            opt.setUserName("karaf");
            opt.setPassword("karaf".toCharArray());

            client.setCallback(this);
            client.connect(opt);

                try {

                    client.subscribe("REACTIVE");

                    client.publish("REACTIVE",
                            ("reactive microservices client paho " + counter++).getBytes(),
                            1,
                            false);

                } catch (MqttException ex) {
                    System.out.println("erro 2" + ex.getMessage());
                    ex.printStackTrace();
                }

  
            System.in.read();
          
            vertx.close();

        } catch (IOException | MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void messageArrived(String string, MqttMessage mm) throws Exception {
       System.out.println("-------------------------------------------------");
		System.out.println("| Topic:" + string);
		System.out.println("| Message: " + new String(mm.getPayload()));
		System.out.println("-------------------------------------------------");
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt) {
        try {
            String message = new String(imdt.getMessage().getPayload());
            System.out.println("deliveryComplet " + message);
        } catch (MqttException e) {
            System.out.println(e.getMessage());
        }
    }
}
