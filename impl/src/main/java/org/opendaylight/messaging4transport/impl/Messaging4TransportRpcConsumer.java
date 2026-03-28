/*
 * Copyright (c) 2015 Pradeeban Kathiravelu and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.messaging4transport.impl;

import org.apache.qpid.amqp_1_0.jms.impl.ConnectionFactoryImpl;
import org.opendaylight.controller.sal.core.api.dom.DOMRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

/**
 * AMQP Consumer that listens for RPC requests and proxies them to MD-SAL DOMRpcService.
 */
public class Messaging4TransportRpcConsumer implements MessageListener, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(Messaging4TransportRpcConsumer.class);
    private DOMRpcService domRpcService;
    private Connection connection;
    private Session session;

    public Messaging4TransportRpcConsumer(DOMRpcService domRpcService) {
        this.domRpcService = domRpcService;
        init();
    }

    private void init() {
        try {
            String user = AMQPConfig.getUser();
            String password = AMQPConfig.getPassword();
            String host = AMQPConfig.getHost();
            int port = AMQPConfig.getPort();

            ConnectionFactoryImpl factory = new ConnectionFactoryImpl(host, port, user, password);
            this.connection = factory.createConnection(user, password);
            this.connection.start();
            this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            // Listen on a specific queue for RPC requests
            Destination destination = session.createQueue("rpc-request-queue");
            MessageConsumer consumer = session.createConsumer(destination);
            consumer.setMessageListener(this);
            
            LOG.info("Messaging4TransportRpcConsumer initialized and listening on rpc-request-queue");
        } catch (JMSException e) {
            LOG.error("Failed to initialize AMQP RPC consumer", e);
        }
    }

    @Override
    public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            try {
                String text = ((TextMessage) message).getText();
                LOG.info("Received RPC request via AMQP: {}", text);
                // TODO: Parse text to Method, SchemaPath, and NormalizedNode input.
                // domRpcService.invokeRpc(schemaPath, input);
                
                // For now, just acknowledge receipt
                Publisher.publish("RPC request received: " + text);
            } catch (JMSException e) {
                LOG.error("Error processing AMQP message", e);
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (session != null) {
            session.close();
        }
        if (connection != null) {
            connection.close();
        }
    }
}
