package com.github.leeonky;

import java.util.Map;

class ExtendedFactory<T> extends AbstractFactory<T> {
    private final Factory<T> parent;
    private final TriConsumer<T, Integer, Map<String, Object>> consumer;

    ExtendedFactory(Factory<T> parent, TriConsumer<T, Integer, Map<String, Object>> consumer) {
        this.parent = parent;
        this.consumer = consumer;
    }

    @Override
    public int getSequence() {
        return parent.getSequence();
    }

    @Override
    public T createObject(int sequence, Map<String, Object> params) {
        T object = parent.createObject(sequence, params);
        consumer.accept(object, sequence, params);
        return object;
    }
}