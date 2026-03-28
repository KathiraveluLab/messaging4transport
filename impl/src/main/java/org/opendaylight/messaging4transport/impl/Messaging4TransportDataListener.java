/*
 * Copyright (c) 2015 Pradeeban Kathiravelu and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.messaging4transport.impl;

import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.sal.core.api.data.DOMDataChangeListener;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener for MD-SAL DOM Data Changes.
 * Capture every data change and publishes it to the AMQP broker.
 */
public class Messaging4TransportDataListener implements DOMDataChangeListener {
    private static final Logger LOG = LoggerFactory.getLogger(Messaging4TransportDataListener.class);

    @Override
    public void onDataChanged(AsyncDataChangeEvent<YangInstanceIdentifier, NormalizedNode<?, ?>> change) {
        LOG.debug("Data changed: {}", change.getCreatedData());
        // For simplicity, we publish the whole set of created data as a string
        if (!change.getCreatedData().isEmpty()) {
            Publisher.publish("Created Data: " + change.getCreatedData().toString());
        }
        if (!change.getUpdatedData().isEmpty()) {
            Publisher.publish("Updated Data: " + change.getUpdatedData().toString());
        }
        if (!change.getRemovedPaths().isEmpty()) {
            Publisher.publish("Removed Paths: " + change.getRemovedPaths().toString());
        }
    }
}
