package com.github.leeonky.javabuilder.spec;

import java.util.*;
import java.util.function.Consumer;

public class ObjectTree {
    private Map<Object, List<Object>> nodes = new HashMap<>();

    public <T> T addNode(Object parent, T node) {
        nodes.computeIfAbsent(parent, k -> new ArrayList<>()).add(node);
        return node;
    }

    public void foreach(Object root, Consumer<Object> consumer) {
        nodes.getOrDefault(root, Collections.emptyList()).forEach(o -> consume(consumer, o));
    }

    private void consume(Consumer<Object> consumer, Object o) {
        nodes.getOrDefault(o, Collections.emptyList()).forEach(s -> consume(consumer, s));
        consumer.accept(o);
    }
}
