/*
 * Copyright (c) 2015 Pradeeban Kathiravelu and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.messaging4transport.impl;

import org.apache.qpid.amqp_1_0.jms.impl.ConnectionFactoryImpl;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.controller.md.sal.dom.api.DOMRpcService;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.concurrent.Future;

/**
 * AMQP Consumer that listens for RPC requests and proxies them to MD-SAL DOMRpcService.
 * Reference implementation for mapping AMQP JSON messages to MD-SAL RPCs.
 */
public class Messaging4TransportRpcConsumer implements MessageListener, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(Messaging4TransportRpcConsumer.class);
    private DOMRpcService domRpcService;
    private SchemaContext schemaContext;
    private Connection connection;
    private Session session;

    public Messaging4TransportRpcConsumer(DOMRpcService domRpcService) {
        this.domRpcService = domRpcService;
        init();
    }

    public void onGlobalContextUpdated(SchemaContext context) {
        this.schemaContext = context;
    }

    private void init() {
        // ... (connection initialization code remains the same)
        try {
            String user = AMQPConfig.getUser();
            String password = AMQPConfig.getPassword();
            String host = AMQPConfig.getHost();
            int port = AMQPConfig.getPort();

            ConnectionFactoryImpl factory = new ConnectionFactoryImpl(host, port, user, password);
            this.connection = factory.createConnection(user, password);
            this.connection.start();
            this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
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
                
                // --- Reference Implementation Logic ---
                // 1. Parse the incoming message to identify the RPC and its inputs.
                // For this reference, we expect a simple format or we'll use a hardcoded example.
                // In a production scenario, use yang-data-codec-gson for full JSON parsing.
                
                // Example: Invoking the 'messaging-transport' RPC defined in this project.
                QName rpcQName = QName.create("urn:opendaylight:params:xml:ns:yang:messaging4transport", "2015-01-05", "messaging-transport");
                SchemaPath schemaPath = SchemaPath.create(true, rpcQName);
                
                // 2. Build the input NormalizedNode (ContainerNode for RPC input)
                DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> inputBuilder = 
                        Builders.containerBuilder().withNodeIdentifier(new NodeIdentifier(QName.create(rpcQName, "input")));
                
                // Add a leaf 'name' to the input (based on our yang model)
                inputBuilder.withChild(Builders.leafBuilder()
                        .withNodeIdentifier(new NodeIdentifier(QName.create(rpcQName, "name")))
                        .withValue(text) // Use the raw AMQP text as the 'name' input
                        .build());
                
                ContainerNode input = inputBuilder.build();

                // 3. Invoke the RPC via DOMRpcService
                if (domRpcService != null && schemaContext != null) {
                    LOG.info("Invoking MD-SAL RPC: {}", rpcQName);
                    Future<?> resultFuture = domRpcService.invokeRpc(schemaPath, input);
                    
                    // 4. Send the result back via AMQP (Acknowledge receipt for now)
                    Publisher.publish("RPC " + rpcQName + " invoked with input: " + text);
                } else {
                    LOG.warn("DOMRpcService or SchemaContext not available for RPC invocation");
                }
                
            } catch (Exception e) {
                LOG.error("Error processing AMQP RPC message", e);
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
