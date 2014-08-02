
package com.lovocal.chat;

import com.lovocal.utils.AppConstants;
import com.lovocal.utils.Logger;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Base class for objects that connect to a RabbitMQ Broker
 */
public abstract class AbstractRabbitMQConnector {

    private static final String  TAG = "AbstractRabbitMQConnector";

    protected final String       mServer;
    protected final String       mExchange;
    protected final String       mVirtualHost;
    protected final int          mPort;
    protected final ExchangeType mExchangeType;

    private OnDisconnectCallback mOnDisconnectCallback;

    public enum ExchangeType {

        DIRECT("direct"),
        TOPIC("topic"),
        FANOUT("fanout");

        public final String key;

        private ExchangeType(final String key) {
            this.key = key;
        }
    }

    protected Channel mChannel;
    protected Connection mConnection;

    private boolean      mRunning;

    public AbstractRabbitMQConnector(final String server, final int port, final String virtualHost, final String exchange, final ExchangeType exchangeType) {
        mServer = server;
        mPort = port;
        mVirtualHost = virtualHost;
        mExchange = exchange;
        mExchangeType = exchangeType;
    }

    /**
     * Disconnect from the broker
     * 
     * @param manual <code>true</code> if the disconnection is manual(logout),
     *            <code>false</code> if it happened through an error/loss of
     *            network etc
     */
    public void dispose(final boolean manual) {
        mRunning = false;

        try {
            if (mConnection != null) {
                mConnection.close();
                Logger.d(TAG, "connection is closed");
            }
            if (mChannel != null) {
                
                Logger.d(TAG, "channel is aborted");
                mChannel.abort();
            }
        } catch (final IOException e) {
            Logger.d(TAG, "connection is closed");
            e.printStackTrace();
        } finally {

            if (mOnDisconnectCallback != null) {
                mOnDisconnectCallback.onDisconnect(manual);
            }
        }

    }

    public void setOnDisconnectCallback(final OnDisconnectCallback callback) {
        mOnDisconnectCallback = callback;
    }

    public OnDisconnectCallback getOnDisconnectCallback() {
        return mOnDisconnectCallback;
    }

    public boolean isRunning() {
        return mRunning;
    }

    protected void setIsRunning(final boolean running) {
        mRunning = running;
    }

    /**
     * Connect to the broker and create the exchange
     * 
     * @return success
     */
    protected boolean connectToRabbitMQ(final String userName,
                    final String password) {
        if ((mChannel != null) && mChannel.isOpen()) {
            return true;
        }
        try {
            final ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(mServer);
            connectionFactory.setUsername(userName);
            connectionFactory.setPassword(password);
            connectionFactory.setVirtualHost(mVirtualHost);
            connectionFactory.setPort(mPort);
            connectionFactory.setRequestedHeartbeat(AppConstants.HEART_BEAT_INTERVAL);
            mConnection = connectionFactory.newConnection();
            mChannel = mConnection.createChannel();
            mChannel.exchangeDeclare(mExchange, mExchangeType.key);

            return true;
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Publish a message to a queue
     * 
     * @param queueName The Queue to publish to
     * @param routingKey The routing key
     * @param message The message to publish
     */
    public void publish(final String queueName, final String routingKey,
                    final String message) {
        if ((mChannel != null) && mChannel.isOpen()) {
            try {
                mChannel.basicPublish(queueName, routingKey, null, message
                                .getBytes(HTTP.UTF_8));
            } catch (final UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Callback interface for when the Chat consumer gets disconnected
     */
    public static interface OnDisconnectCallback {

        /**
         * Callback method to be triggered when the connector disconnects
         * 
         * @param manual <code>true</code> if the chat was manually
         *            disconnected(user logout), <code>false</code> if it
         *            happened due to an error/loss of network
         */
        public void onDisconnect(boolean manual);
    }
}
