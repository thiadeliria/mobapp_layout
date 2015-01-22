package com.stackbase.mobapp.objects;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

abstract public class Person {

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        Method[] methods = this.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("get")) {
                try {
                    String fieldName = method.getName().substring(3, 4).toLowerCase()
                            + method.getName().substring(4);
                    Field field = this.getClass().getDeclaredField(fieldName);
                    jsonObject.put(field.getName(), method.invoke(this));
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return jsonObject;
    }
}
