/*
 * Copyright (c) 2015 Pradeeban Kathiravelu  and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.messaging4transport.impl;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.core.api.Broker.ProviderSession;
import org.opendaylight.controller.sal.core.api.Provider;
import org.opendaylight.controller.sal.core.api.dom.DOMDataBroker;
import org.opendaylight.controller.sal.core.api.dom.DOMNotificationService;
import org.opendaylight.controller.sal.core.api.dom.DOMRpcService;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.messaging4transport.rev150105.Messaging4transportService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Messaging4transportProvider implements BindingAwareProvider, Provider, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(Messaging4transportProvider.class);
    private RpcRegistration<Messaging4transportService> messaging4transportService;

    private DOMDataBroker domDataBroker;
    private DOMNotificationService domNotificationService;
    private DOMRpcService domRpcService;

    private ListenerRegistration<?> dataChangeListener;
    private Messaging4TransportRpcConsumer rpcConsumer;

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("Messaging4transportProvider Binding-Aware Session Initiated");
        messaging4transportService = session.addRpcImplementation(Messaging4transportService.class,
                new Messaging4TransportImpl());
    }

    @Override
    public void onSessionInitiated(ProviderSession session) {
        LOG.info("Messaging4transportProvider DOM Session Initiated");
        this.domDataBroker = session.getService(DOMDataBroker.class);
        this.domNotificationService = session.getService(DOMNotificationService.class);
        this.domRpcService = session.getService(DOMRpcService.class);

        if (domNotificationService != null) {
            domNotificationService.registerNotificationListener(new Messaging4TransportNotificationListener());
        }

        if (domDataBroker != null) {
            dataChangeListener = domDataBroker.registerDataChangeListener(LogicalDatastoreType.CONFIGURATION,
                    YangInstanceIdentifier.builder().build(), new Messaging4TransportDataListener(),
                    DOMDataBroker.DataChangeScope.SUBTREE);
        }

        if (domRpcService != null) {
            rpcConsumer = new Messaging4TransportRpcConsumer(domRpcService);
        }

        Publisher.publish("Messaging4Transport DOM Session Initiated");
    }

    @Override
    public void close() throws Exception {
        LOG.info("Messaging4transportProvider Closed");
        if (messaging4transportService != null) {
            messaging4transportService.close();
        }
        if (dataChangeListener != null) {
            dataChangeListener.close();
        }
        if (rpcConsumer != null) {
            rpcConsumer.close();
        }
    }
}
