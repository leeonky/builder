package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;

import java.util.List;
import java.util.Map;

public interface DataRepository {
    void save(Object object);

    <T> List<T> query(BeanClass<T> beanClass, Map<String, Object> criteria);

    void clear();
}
