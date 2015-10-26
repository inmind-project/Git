package com.yahoo.inmind.commons.rules.control;


import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

/**
 * Created by oscarr on 10/16/15.
 */
public class AnnotationExclusionStrategy implements ExclusionStrategy {

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return f.getAnnotation(Exclude.class) != null;
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}