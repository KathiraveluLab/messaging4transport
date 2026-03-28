Messaging4Transport
========
Message Oriented Middleware Bindings for MD-SAL

### Description ###

The OpenDaylight controller is based on MD-SAL, which allows for the modeling of data, RPCs, and notifications. Messaging4Transport provides a highly flexible and automatic Northbound binding for MD-SAL using Message Oriented Middleware (MOM).

By leveraging OpenDaylight's **DOM (Data Object Model)** services, this project achieves a schema-agnostic integration that automatically exposes all MD-SAL APIs without requiring manual Java bindings for every YANG model.

#### Key Features ####
*   **Automatic Data Tree Exposure**: Real-time publishing of MD-SAL Configuration Data Tree changes to AMQP topics.
*   **Automatic Notification Forwarding**: Every MD-SAL notification captured by the controller is automatically forwarded to the AMQP broker.
*   **RPC Proxying via AMQP**: External AMQP clients can invoke MD-SAL RPCs by sending structured requests (JSON/XML) to dedicated request queues.

### Architecture ###

Messaging4Transport is built as an independent Karaf feature (`odl-messaging4transport`). It integrates with standard AMQP 1.0 brokers and uses JMS for message handling. Initial implementation and testing were performed with ActiveMQ.

### Scope ###

*   Developing Bindings for OpenDaylight MD-SAL to integrate with Message-Oriented Middleware.
*   Design and Implementation of Advanced Message Queuing Protocol (AMQP) bindings.
*   Schema-agnostic DOM-based integration for universal YANG model support.
*   Potential extension points for other MOM protocols such as STOMP, MQTT, and OpenWire.

---
The project does not affect any other existing projects of OpenDaylight.
