package com.azavea.androidvalidatedforms.sample;

import android.content.Context;

import com.azavea.androidvalidatedforms.controllers.ImageController;

/**
 * Demonstrate subclassing ImageController for a different model.
 *
 * Created by kathrynkillebrew on 2/4/16.
 */
public class SampleImageController extends ImageController {
    public SampleImageController(Context ctx, String name, String labelText, boolean isRequired) {
        super(ctx, name, labelText, isRequired);
    }

    @Override
    protected void setModelValue(String newImagePath) {
        ImageHolder newHolder = null;

        if (newImagePath != null && !newImagePath.isEmpty()) {
            newHolder = new ImageHolder();
            newHolder.path = newImagePath;
        }

        getModel().setValue(getName(), newHolder);
    }

    @Override
    protected Object getModelValue() {
        Object current = super.getModelValue();

        if (current != null && current.getClass().equals(ImageHolder.class)) {
            return ((ImageHolder)current).path;
        }

        return null;
    }
}
