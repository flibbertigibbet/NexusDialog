package com.azavea.androidvalidatedforms.controllers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.azavea.androidvalidatedforms.FormElementController;
import com.azavea.androidvalidatedforms.FormModelEnclosure.FormModel;
import com.azavea.androidvalidatedforms.R;
import com.azavea.androidvalidatedforms.validations.HibernateValidatorInstance;
import com.azavea.androidvalidatedforms.validations.HibernationError;
import com.azavea.androidvalidatedforms.validations.RequiredField;
import com.azavea.androidvalidatedforms.validations.ValidationError;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

/**
 * An abstract class that represents a generic form field with an associated label.
 */
public abstract class LabeledFieldController extends FormElementController {
    protected final String labelText;
    private boolean required;
    private View fieldView;
    protected TextView errorView;
    private boolean needsValidation;
    private List<ValidationError> errors;

    /**
     * Creates a labeled field.
     *
     * @param ctx           the Android context
     * @param name          the name of the field
     * @param labelText     the label to display beside the field. If null, no label is displayed and the field will
     *                      occupy the entire length of the row.
     * @param isRequired    indicates whether this field is required. If true, this field checks for a non-empty or
     *                      non-null value upon validation. Otherwise, this field can be empty.
     */
    public LabeledFieldController(Context ctx, String name, String labelText, boolean isRequired) {
        super(ctx, name);
        this.labelText = labelText;
        required = isRequired;
        needsValidation = true;
    }

    /**
     * Returns the associated label for this field.
     *
     * @return the associated label for this field
     */
    public String getLabel() {
        return labelText;
    }

    /**
     * Sets whether this field is required to have user input.
     *
     * @param required  if true, this field checks for a non-empty or non-null value upon validation. Otherwise, this
     *                  field can be empty.
     */
    protected void setIsRequired(boolean required) {
        this.required = required;
    }

    /**
     * Marks field as needing to be validated. Flag cleared when validation is run.
     *
     * Call this in implementations whenever the field value changes.
     */
    public void setNeedsValidation() {
        this.needsValidation = true;
    }

    /**
     * Indicates whether this field requires an input value.
     *
     * @return  true if this field is required to have input, otherwise false
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Indicates whether the input of this field has any validation errors.
     *
     * @return  true if there are some validation errors, otherwise false
     */
    public boolean isValidInput() {
        return validateInput().isEmpty();
    }

    /**
     * Runs a validation on the user input and returns all the validation errors of this field.
     * Previous error messages are removed when calling {@code validateInput()}.
     *
     * @return  a list containing all the validation errors
     */
    public List<ValidationError> validateInput() {

        // only validate field if it has changed since last validation
        if (!needsValidation) {
            return errors;
        }

        errors = new ArrayList<>();
        String name = getName();
        String label = getLabel();
        final FormModel model = this.getModel();
        Object value = model.getValue(name);

        if (value != null) {
            // cannot run Hibernate Validator on null object
            Validator validator = HibernateValidatorInstance.getValidator();
            Object modelObject = model.getBackingModelObject();

            Set<ConstraintViolation<Object>> violations = validator.validateProperty(modelObject, name);
            for (ConstraintViolation violation: violations) {
                errors.add(new HibernationError(name, label, violation));
            }
        } else if (isRequired()) {
            // have null required field
            errors.add(new RequiredField(name, label));
        }

        needsValidation = false;
        return errors;
    }

    /**
     * Returns the associated view for the field (without the label view) of this element.
     *
     * @return          the view for this element
     */
    public View getFieldView() {
        if (fieldView == null) {
            fieldView = createFieldView();
        }
        return fieldView;
    }

    /**
     * Constructs the view associated with this field without the label. It will be used to combine with the label.
     *
     * @return          the newly created view for this field
     */
    protected abstract View createFieldView();

    @Override
    protected View createView() {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.form_labeled_element, null);
        errorView = (TextView) view.findViewById(R.id.field_error);

        TextView label = (TextView)view.findViewById(R.id.field_label);
        if (labelText == null) {
            label.setVisibility(View.GONE);
        } else {
            label.setText(labelText);
        }

        FrameLayout container = (FrameLayout)view.findViewById(R.id.field_container);
        container.addView(getFieldView());

        return view;
    }

    @Override
    public void setError(String message) {
        if (message == null) {
            errorView.setVisibility(View.GONE);
        } else {
            errorView.setText(message);
            errorView.setVisibility(View.VISIBLE);
        }
    }
}
