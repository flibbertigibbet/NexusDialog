package com.azavea.androidvalidatedforms;

/**
 * Created by kathrynkillebrew on 12/29/15.
 */
public interface FormActivityBase {

    FormModel getModel();

    FormController getFormController();

    void initForm();

    void displayForm();

    FormController createFormController();

    void showProgress(final boolean show);
}
