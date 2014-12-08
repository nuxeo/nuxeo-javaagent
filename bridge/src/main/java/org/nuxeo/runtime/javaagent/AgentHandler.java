package org.nuxeo.runtime.javaagent;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class AgentHandler implements InvocationHandler {

    public static <I> I newHandler(Class<I> type, Object agent) {
        return type.cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { type }, new AgentHandler(agent)));
    }

    protected AgentHandler(Object agent) {
        this.agent = agent;
        type = agent.getClass();
    }

    protected final Class<?> type;

    protected final Object agent;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("humanReadable".equals(method.getName())) {
            return humanReadable((long) args[0]);
        }
        return agentMethod(method).invoke(agent, args);
    }

    protected static String[] units = { "b", "Kb", "Mb" };

    protected String humanReadable(long size) {
        String unit = "b";
        double dSize = size;
        for (String eachUnit : units) {
            unit = eachUnit;
            if (dSize < 1024) {
                break;
            }
            dSize /= 1024;
        }

        return dSize + unit;
    }

    protected final Map<Method, Method> agentMethods = new HashMap<>();

    protected Method agentMethod(Method bridgeMethod) throws NoSuchMethodException, SecurityException {
        Method agentMethod = agentMethods.get(bridgeMethod);
        if (agentMethod == null) {
            agentMethod = type.getDeclaredMethod(bridgeMethod.getName(), bridgeMethod.getParameterTypes());
            agentMethods.put(bridgeMethod, agentMethod);
        }
        return agentMethod;
    }

}
