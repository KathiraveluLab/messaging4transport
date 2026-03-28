/*
 * Copyright (c) 2015 Pradeeban Kathiravelu and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.messaging4transport.impl;

import org.opendaylight.controller.sal.core.api.dom.DOMNotification;
import org.opendaylight.controller.sal.core.api.dom.DOMNotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener for MD-SAL DOM Notifications.
 * Capture every notification and publishes it to the AMQP broker.
 */
public class Messaging4TransportNotificationListener implements DOMNotificationListener {
    private static final Logger LOG = LoggerFactory.getLogger(Messaging4TransportNotificationListener.class);

    @Override
    public void onNotification(DOMNotification notification) {
        LOG.debug("Received notification: {}", notification.getType());
        String payload = notification.getBody().toString(); // Basic string representation for now
        Publisher.publish(payload);
    }
}
