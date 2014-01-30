Nuxeo Java Agent
================
Nuxeo Java Agent allows computes object size in memory.

It uses java.lang.instrument.Intrumentation.

How to use
----------

1. Run mvn clean install

2. Copy main/target/nuxeo-javaagent-main-{version}.jar to $NUXEO_HOME/bin

3. Copy bridge/target/nuxeo-javaagent-bridge-{version}.jar to $NUXEO_HOME/nxsever/bundle

4. Copy $JAVA_HOME/lib/tools.jar to $NUXEO_HOME/nxsever/lib

5. Add JAVA_OPTS=$JAVA_OPTS -Djavaagent=nuxeo-javaagent-main-{version}.jar to your nuxeo.conf


In your code, call :
 - AgentLoader.INSTANCE.getSizer().deepSizeOf(object);
 - AgentLoader.INSTANCE.getSizer().sizeOf(object);

