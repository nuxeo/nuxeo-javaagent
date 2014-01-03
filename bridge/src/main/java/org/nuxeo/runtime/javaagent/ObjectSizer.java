package org.nuxeo.runtime.javaagent;

public interface ObjectSizer {

    long sizeOf(Object o);

    long deepSizeOf(Object o);

    String humanReadable(long size);
}