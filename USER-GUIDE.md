# Messaging4Transport User Guide

Messaging4Transport provides a highly flexible and automatic Northbound binding for OpenDaylight MD-SAL using Message Oriented Middleware (MOM). This guide explains how to set up, build, run, and verify the service.

---

## 1. Prerequisites

- **Java**: JDK 1.8 (Java 8) is strictly required for the OpenDaylight Lithium build process.
- **Maven**: Version 3.1.1 or higher.
- **Docker**: Optional, but recommended for running the ActiveMQ broker via the automated setup script.
- **AMQP Broker**: An AMQP 1.0 compatible broker (e.g., ActiveMQ) running on port `5672`.

---

## 2. Automated Setup and Build

The project includes an automated `setup.sh` script that prepares the environment and builds the project.

### Step 1: Run Setup
```bash
./setup.sh
```
This script will:
1. Detect or start an ActiveMQ broker via Docker.
2. Automatically download a local **OpenJDK 8** distribution if your system Java is incompatible.
3. Patch Maven settings to handle modern HTTPS repository requirements.
4. Build the entire project.

### Step 2: Manual Build (Optional)
If you prefer to build manually (ensure `JAVA_HOME` is set to Java 8):
```bash
mvn clean install -s settings.xml -DskipTests -Dmaven.javadoc.skip=true -Dcheckstyle.skip
```

---

## 3. Running the Controller

Since the OpenDaylight Lithium Karaf container is incompatible with modern Java versions (Java 11+), you must use the provided wrapper script to launch it:

```bash
./run.sh
```
This script ensures that the correct Java 8 environment is used and patches the internal Karaf configuration for repository access.

---

## 4. Installing the Feature

Once the Karaf console is ready (`opendaylight-user@root>`), install the Messaging4Transport feature:

```karaf
feature:install odl-messaging4transport
```

---

## 5. Verification

Follow these steps to ensure the service is correctly running in the OSGi container.

### Check Feature Status
```karaf
feature:list -i | grep messaging4transport
```
*Expected Output:*
```text
odl-messaging4transport | 1.0-SNAPSHOT | x | odl-messaging4transport-1.0-SNAPSHOT | ...
```
> [!NOTE]
> The **`x`** in the third column indicates that the feature is successfully **Installed**.

### Check OSGi Bundles (The "Golden" Check)
To ensure the code is actually running:
```karaf
bundle:list | grep messaging4transport
```
*Expected Output:*
```text
ID | State  | Lvl | Version        | Name
---|--------|-----|----------------|--------------------------
XX | Active |  80 | 1.0.0.SNAPSHOT | messaging4transport-api
YY | Active |  80 | 1.0.0.SNAPSHOT | messaging4transport-impl
```
> [!TIP]
> **`Active`** status is the most important indicator. It means the bundle has successfully resolved all dependencies and started its MD-SAL provider session.

### Check Initialization Logs
```karaf
log:display | grep Messaging4transport
```
*Expected Output:*
```text
Messaging4transportProvider Session Initiated
```
This confirms that the MD-SAL Data Object Model (DOM) services have been successfully retrieved and the provider is ready to handle AMQP messages.

### Diagnose Issues
If a bundle is in `GracePeriod` or `Failure`, use the diagnostic command:
```karaf
diag <bundle_id>
```
*(Replace `<bundle_id>` with the ID found in `bundle:list`)*

---

## 6. Troubleshooting

### "Unrecognized VM option 'UnsyncloadClass'"
This error occurs if you try to run Karaf with Java 11 or newer. **Fix**: Use `./run.sh` which forces the use of Java 8.

### "501 Not Implemented" during feature install
This occurs when Karaf tries to download dependencies via HTTP. **Fix**: Ensure you have run `./setup.sh` at least once and are using `./run.sh` to start the controller, as it patches the configuration to use HTTPS.

### "Can't resolve jsr173-ri:jar:1.0"
This is a known issue with legacy dependencies. **Fix**: The `./setup.sh` script automatically installs a mock version of this artifact to your local repository to bypass the error.

---

## 7. Sample Applications

Two sample applications are provided in the `impl` module to test end-to-end connectivity.

### Run the Listener
Subscribe to events published by the controller:
```bash
mvn exec:java -pl impl -Dexec.mainClass="org.opendaylight.messaging4transport.sample.Listener"
```

### Run the Publisher
Manually publish a message to the broker:
```bash
mvn exec:java -pl impl -Dexec.mainClass="org.opendaylight.messaging4transport.sample.PublisherMain"
```
