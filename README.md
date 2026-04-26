Messaging4Transport
========
Message Oriented Middleware Bindings for MD-SAL

> [!TIP]
> For a detailed walkthrough of setup, verification, and troubleshooting, refer to the [USER-GUIDE.md](USER-GUIDE.md).

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

### Prerequisites
*   **Java**: JDK 1.8
*   **Maven**: 3.1.1 or higher
*   **Docker**: Optional (required for `setup.sh` to start a broker)
*   **AMQP Broker**: An AMQP 1.0 compatible broker (e.g., ActiveMQ) running on port `5672`.

### Quick Start
You can use the provided setup script to initialize the environment (start a broker via Docker if none is found) and build the project:
```bash
./setup.sh
```

### Building
To build the project manually using Maven:
```bash
mvn clean install -U -DskipTests -Dmaven.javadoc.skip=true -Dcheckstyle.skip
```

### Execution

1. **Start the Controller**:
   The setup script automatically downloads and configures Java 8. You must use the provided wrapper script to run Karaf with this specific Java environment:
   ```bash
   ./run.sh
   ```

2. **Install the Feature**:
   Once you're at the `opendaylight-user@root>` prompt in Karaf, install the core messaging transport feature:
   ```bash
   feature:install odl-messaging4transport
   ```

### Configuration
The project uses environment variables for broker connection settings. If not provided, defaults for a local ActiveMQ instance are used.

| Variable | Description | Default |
| :--- | :--- | :--- |
| `ACTIVEMQ_HOST` | Hostname of the AMQP broker | `localhost` |
| `ACTIVEMQ_PORT` | Port of the AMQP broker | `5672` |
| `ACTIVEMQ_USER` | Broker username | `admin` (or `karaf` in Karaf) |
| `ACTIVEMQ_PASSWORD` | Broker password | `password` (or `karaf` in Karaf) |

### Sample Usage
Sample publisher and listener implementations are provided in the `impl` module.

#### Running the Listener
To listen for events published by the controller:
```bash
mvn exec:java -pl impl -Dexec.mainClass="org.opendaylight.messaging4transport.sample.Listener"
```

#### Running the Publisher (Manual Test)
To publish a test message to the broker:
```bash
mvn exec:java -pl impl -Dexec.mainClass="org.opendaylight.messaging4transport.sample.PublisherMain"
```

---
The project does not affect any other existing projects of OpenDaylight.
