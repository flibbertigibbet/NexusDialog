package com.azavea.androidvalidatedforms;

import com.azavea.androidvalidatedforms.tasks.ValidationTask;

/**
 * Expected interface for FormWithAppCompatActivity and FormActivity.
 *
 * Created by kathrynkillebrew on 12/29/15.
 */
public interface FormActivityBase extends ValidationTask.ValidationCallback {

    FormModel getModel();

    FormController getFormController();

    void initForm();

    void displayForm();

    FormController createFormController();

    void showProgress(final boolean show);
}
