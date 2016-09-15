package com.azavea.androidvalidatedforms;

/**
 * Created by kat on 9/15/16.
 */
public class FormObjectModel extends FormModelEnclosure.FormModel {

    private Object modelObject;
    private Class modelObjectClass;

    public FormObjectModel() {
        super();
    }

    public static FormObjectModel newInstance(Object modelObject) {
        FormObjectModel fragment = new FormObjectModel();
        fragment.modelObject = modelObject;
        fragment.modelObjectClass = modelObject.getClass();
        return fragment;
    }

    @Override
    protected void setBackingValue(String name, Object newValue) {
        try {
            modelObjectClass.getField(name).set(modelObject, newValue);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Class getBackingModelClass(String fieldName) {
        try {
            return modelObjectClass.getField(fieldName).getType();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected Object getBackingValue(String name) {
        try {
            return modelObjectClass.getField(name).get(modelObject);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object getBackingModelObject() {
        return modelObject;
    }
}
