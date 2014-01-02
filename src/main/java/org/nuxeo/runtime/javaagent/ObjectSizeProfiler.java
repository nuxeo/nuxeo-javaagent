package org.nuxeo.runtime.javaagent;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

public class ObjectSizeProfiler implements ObjectSizeProfilerManagement {

    protected static ObjectSizeProfiler profiler = newProfiler();

    protected static ObjectSizeProfiler newProfiler() {
        ObjectSizeProfiler me = new ObjectSizeProfiler();
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            mbs.registerMBean(me, new ObjectName(
                    "org.nuxeo:type=object-size-profiler"));
        } catch (InstanceAlreadyExistsException | MBeanRegistrationException
                | NotCompliantMBeanException | MalformedObjectNameException cause) {
            Logger.getLogger(ObjectSizeProfiler.class.getName()).log(
                    Level.SEVERE, "Cannot register object size profile mbean",
                    cause);
        }
        return me;
    }

    protected Instrumentation instrumentation;

    public static void premain(String args, Instrumentation inst) {
        profiler.instrumentation = inst;
    }

    public long getObjectSize(Object o) {
        return instrumentation.getObjectSize(o);
    }

    public long getObjectGraphSize(final Object o) {
        if (o == null) {
            throw new NullPointerException();
        }

        return AccessController.doPrivileged(new PrivilegedAction<Long>() {
            public Long run() {
                return new GraphSizeProfiler().visit(o);
            }
        });
    }

    protected class GraphSizeProfiler {

        protected final Map<Object, Object> visited = new IdentityHashMap<Object, Object>();

        protected long visit(Object each) {
            if (each == null) {
                return 0;
            }
            if (visited.containsKey(each)) {
                return 0;
            }
            visited.put(each, each);
            long size = instrumentation.getObjectSize(each);
            Class<?> eachType = each.getClass();
            if (eachType.isArray()) {
                if (eachType.getComponentType().isPrimitive()) {
                    return 0;
                }
                for (int i = 0; i < Array.getLength(each); i++) {
                    size += visit(Array.get(each, i));
                }
            } else {
                size += visit(each, eachType);
            }
            return size;
        }

        protected long visit(Object each, Class<?> eachType) {
            if (eachType.equals(Object.class)) {
                return 0;
            }
            long size = 0;
            for (Field eachField : eachType.getDeclaredFields()) {
                size += visit(each, eachField);
            }
            return size + visit(each, eachType.getSuperclass());
        }

        protected long visit(Object each, Field eachField) {
            if ((eachField.getModifiers() & Modifier.STATIC) != 0) {
                return 0;
            }
            if (eachField.getType().isPrimitive()) {
                return 0;
            }
            boolean oldAccessible = eachField.isAccessible();
            eachField.setAccessible(true);
            try {
                return visit(eachField.get(each));
            } catch (Exception e) {
                throw new RuntimeException("Exception trying to access field "
                        + eachField, e);
            } finally {
                eachField.setAccessible(oldAccessible);
            }
        }
    }

    @Override
    public void activate() {
        if (instrumentation != null) {
            throw new IllegalStateException("Already active");
        }
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        Method addURL;
        try {
            addURL = loader.getClass().getMethod("addURL", URL.class);
            addURL.invoke(loader, new URL("nuxeo-javaagent.jar"));
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
