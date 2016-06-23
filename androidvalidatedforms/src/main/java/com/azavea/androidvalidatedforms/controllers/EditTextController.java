package com.azavea.androidvalidatedforms.controllers;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.azavea.androidvalidatedforms.FormController;

/**
 * Represents a field that allows free-form text.
 */
public class EditTextController extends LabeledFieldController {

    private final String LOG_LABEL = "EditTextCtl";
    private final int editTextId = FormController.generateViewId();

    private int inputType;
    private final String placeholder;

    /**
     * Constructs a new instance of an edit text field.
     *
     * @param ctx           the Android context
     * @param name          the name of the field
     * @param labelText     the label to display beside the field. Set to {@code null} to not show a label.
     * @param placeholder   a placeholder text to show when the input field is empty. If null, no placeholder is displayed
     * @param isRequired    indicates if the field is required or not
     * @param inputType     the content type of the text box as a mask; possible values are defined by {@link InputType}.
     *                      For example, to enable multi-line, enable {@code InputType.TYPE_TEXT_FLAG_MULTI_LINE}.
     */
    public EditTextController(Context ctx, String name, String labelText, String placeholder, boolean isRequired, int inputType) {
        super(ctx, name, labelText, isRequired);
        this.placeholder = placeholder;
        this.inputType = inputType;
    }

    /**
     * Constructs a new instance of an edit text field.
     *
     * @param ctx           the Android context
     * @param name          the name of the field
     * @param labelText     the label to display beside the field
     * @param placeholder   a placeholder text to show when the input field is empty. If null, no placeholder is displayed
     * @param isRequired    indicates if the field is required or not
     */
    public EditTextController(Context ctx, String name, String labelText, String placeholder, boolean isRequired) {
        this(ctx, name, labelText, placeholder, isRequired, InputType.TYPE_CLASS_TEXT);
    }

    /**
     * Constructs a new instance of an edit text field.
     *
     * @param ctx           the Android context
     * @param name          the name of the field
     * @param labelText     the label to display beside the field
     * @param placeholder   a placeholder text to show when the input field is empty. If null, no placeholder is displayed
     */
    public EditTextController(Context ctx, String name, String labelText, String placeholder) {
        this(ctx, name, labelText, placeholder, false, InputType.TYPE_CLASS_TEXT);
    }

    /**
     * Constructs a new instance of an edit text field.
     *
     * @param ctx           the Android context
     * @param name          the name of the field
     * @param labelText     the label to display beside the field
     */
    public EditTextController(Context ctx, String name, String labelText) {
        this(ctx, name, labelText, null, false, InputType.TYPE_CLASS_TEXT);
    }

    /**
     * Returns the EditText view associated with this element.
     *
     * @return the EditText view associated with this element
     */
    public EditText getEditText() {
        return (EditText)getView().findViewById(editTextId);
    }

    /**
     * Returns a mask representing the content input type. Possible values are defined by {@link InputType}.
     *
     * @return a mask representing the content input type
     */
    public int getInputType() {
        return inputType;
    }

    private void setInputTypeMask(int mask, boolean enabled) {
        if (enabled) {
            inputType = inputType | mask;
        } else {
            inputType = inputType & ~mask;
        }
        if (isViewCreated()) {
            getEditText().setInputType(inputType);
        }
    }

    /**
     * Indicates whether this text box has multi-line enabled.
     *
     * @return  true if this text box has multi-line enabled, or false otherwise
     */
    public boolean isMultiLine() {
        return (inputType | InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0;
    }

    /**
     * Enables or disables multi-line input for the text field. Default is false.
     *
     * @param multiLine if true, multi-line input is allowed, otherwise, the field will only allow a single line.
     */
    public void setMultiLine(boolean multiLine) {
        setInputTypeMask(InputType.TYPE_TEXT_FLAG_MULTI_LINE, multiLine);
    }

    /**
     * Indicates whether this text field hides the input text for security reasons.
     *
     * @return  true if this text field hides the input text, or false otherwise
     */
    public boolean isSecureEntry() {
        return (inputType | InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0;
    }

    /**
     * Enables or disables secure entry for this text field. If enabled, input will be hidden from the user. Default is
     * false.
     *
     * @param isSecureEntry if true, input will be hidden from the user, otherwise input will be visible.
     */
    public void setSecureEntry(boolean isSecureEntry) {
        setInputTypeMask(InputType.TYPE_TEXT_VARIATION_PASSWORD, isSecureEntry);
    }

    @Override
    protected View createFieldView() {
        final EditText editText = new EditText(getContext());
        editText.setId(editTextId);

        editText.setSingleLine(!isMultiLine());
        if (placeholder != null) {
            editText.setHint(placeholder);
        }
        editText.setInputType(inputType);
        refresh(editText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String editTextString = editText.getText().toString();
                getModel().setValue(getName(), getCastValue(editTextString));
                setNeedsValidation();
            }
        });

        return editText;
    }

    /**
     * Get the value from the text field, cast to the appropriate type for the backing model.
     *
     * @param editTextString String input to the EditText field.
     * @return Object of type modelClass, or null if cast failed
     */
    private Object getCastValue(String editTextString) {
        Object value = editTextString;
        Class modelClass = getModel().getBackingModelClass(getName());

        if (CharSequence.class.isAssignableFrom(modelClass)) {
            // if it is a string type, we're done
            return value != null ? value : "";
        }

        if (editTextString.isEmpty()) {
            return null;
        }

        try {
            // allow use of EditText for numeric types
            if (Integer.class.isAssignableFrom(modelClass)) {
                value = Integer.valueOf(editTextString);
            } else if (Double.class.isAssignableFrom(modelClass)) {
                value = Double.valueOf(editTextString);
            } else if (Float.class.isAssignableFrom(modelClass)) {
                value = Float.valueOf(editTextString);
            } else if (Long.class.isAssignableFrom(modelClass)) {
                value = Long.valueOf(editTextString);
            } else {
                Log.e(LOG_LABEL, "Unidentified edit text backing object class: " + modelClass);
                Log.e(LOG_LABEL, "Edit text backing object should be a string or number type.");
            }
        } catch (NumberFormatException ex) {
            // might happen if user is in middle of entering a value and has only input
            // a negative sign or decimal point
            Log.d(LOG_LABEL, "Could not parse '" + editTextString + "' as a number");
            value = null;
        } catch (Exception ex) {
            // will return original string value if type not recognized
            Log.e(LOG_LABEL, "Failed to check appropriate type on EditText field " + getName());
            ex.printStackTrace();
        }

        return value;
    }

    private void refresh(EditText editText) {
        Object value = getModel().getValue(getName());
        Object newVal = getCastValue(editText.getText().toString());

        if (value == null && newVal == null) {
            return;
        }

        if (value == null || !value.equals(newVal)) {
            editText.setText(newVal != null ? newVal.toString() : "");
            setNeedsValidation();
        }
    }

    @Override
    public void refresh() {
        refresh(getEditText());
    }
}
