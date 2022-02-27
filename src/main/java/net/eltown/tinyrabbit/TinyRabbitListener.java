package net.eltown.tinyrabbit;

import com.rabbitmq.client.*;
import net.eltown.tinyrabbit.data.Delivery;
import net.eltown.tinyrabbit.data.Request;
import net.eltown.tinyrabbit.handler.CallbackKeyHandler;
import net.eltown.tinyrabbit.handler.KeyHandler;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class TinyRabbitListener {

    final ConnectionFactory factory;
    private boolean throwExceptions = false;

    public TinyRabbitListener(final String host) {
        this.factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setAutomaticRecoveryEnabled(true);
    }

    public void throwExceptions(final boolean value) {
        this.throwExceptions = value;
    }

    public void receive(final KeyHandler handler, final String connectionName, final String queue) {
        this.receive((delivery -> handler.handle(delivery.getKey(), delivery.getData())), connectionName, queue);
    }

    public void receive(final Consumer<Delivery> received, final String connectionName, final String queue) {
        try {
            final Connection connection = factory.newConnection(connectionName);
            final Channel channel = connection.createChannel();
            channel.queueDeclare(queue, false, false, false, null);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                CompletableFuture.runAsync(() -> {
                    try {
                        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                        final String deliveryKey = message.toUpperCase().split("//")[0];
                        received.accept(new Delivery(deliveryKey, message.split("//"), deliveryKey.equalsIgnoreCase("error")));
                    } catch (final Exception ex) {
                        if (this.throwExceptions) ex.printStackTrace();
                    }
                });
            };

            if (this.throwExceptions) {
                channel.addShutdownListener(e -> {
                    e.printStackTrace();
                    System.out.println("Warnung: Ein TinyRabbitListener Receive Channel wurde aufgrund eines Fehlers geschlossen.");
                    System.out.println("Der Channel wird neugestartet.");
                    this.receive(received, connectionName, queue);
                });
            }

            channel.basicConsume(queue, true, deliverCallback, consumerTag -> {
            });
        } catch (final Exception ex) {
            if (this.throwExceptions) ex.printStackTrace();
        }
    }

    public void callback(final CallbackKeyHandler handler, final String connectionName, final String queue) {
        this.callback(request -> {
            handler.handle(request.getKey(), request.getData(), request);
        }, connectionName, queue);
    }

    public void callback(final Consumer<Request> request, final String connectionName, final String queue) {
        try {
            final Connection connection = factory.newConnection(connectionName);
            final Channel channel = connection.createChannel();
            channel.queueDeclare(queue, false, false, false, null);

            final DeliverCallback callback = (tag, delivery) -> {
                CompletableFuture.runAsync(() -> {
                    final AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                            .Builder()
                            .correlationId(delivery.getProperties().getCorrelationId())
                            .build();

                    final String message = new String(delivery.getBody(), StandardCharsets.UTF_8);

                    try {
                        request.accept(new Request(message.toUpperCase().split("//")[0], message.split("//"), channel, delivery, replyProps));
                    } catch (final Exception ex) {
                        new Request(message.toUpperCase().split("//")[0], message.split("//"), channel, delivery, replyProps).answer("ERROR", "error");
                        if (this.throwExceptions) ex.printStackTrace();
                    }
                });
            };

            if (this.throwExceptions) {
                channel.addShutdownListener(e -> {
                    e.printStackTrace();
                    System.out.println("Warnung: Ein TinyRabbitListener Callback Channel wurde aufgrund eines Fehlers geschlossen.");
                    System.out.println("Der Channel wird neugestartet.");
                    this.callback(request, connectionName, queue);
                });
            }

            channel.basicConsume(queue, false, callback, (consumerTag -> {
            }));
        } catch (final Exception ex) {
            if (this.throwExceptions) ex.printStackTrace();
        }
    }

}
