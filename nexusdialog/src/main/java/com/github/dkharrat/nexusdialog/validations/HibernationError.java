package com.github.dkharrat.nexusdialog.validations;

import android.content.res.Resources;

import javax.validation.ConstraintViolation;

/**
 * Created by kathrynkillebrew on 12/11/15.
 */
public class HibernationError extends ValidationError {

    private ConstraintViolation violation;

    public HibernationError(String fieldName, String fieldLabel, ConstraintViolation violation) {
        super(fieldName, fieldLabel);
        this.violation = violation;
    }

    @Override
    public String getMessage(Resources resources) {
        return violation.getMessage();
    }
}
