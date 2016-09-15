package com.azavea.androidvalidatedforms;

import android.content.Intent;

import com.azavea.androidvalidatedforms.FormModelEnclosure.FormModel;
import com.azavea.androidvalidatedforms.tasks.ValidationTask;

/**
 * Expected interface for FormWithAppCompatActivity and FormActivity.
 *
 * Created by kathrynkillebrew on 12/29/15.
 */
public interface FormActivityBase extends ValidationTask.ValidationCallback {

    interface FormReadyListener {
        void formReadyCallback();
    }

    interface ExternalWriteRequest {
        void gotResult(boolean granted);
    }

    void setExternalWriteRequestListener(ExternalWriteRequest listener);

    FormModel getModel();

    FormController getFormController();

    void initForm();

    void displayForm();

    boolean isFormReady();

    void formIsReady();

    void setFormReadyListener(FormReadyListener listener);

    FormController createFormController();

    void showProgress(final boolean show);

    void launchIntent(Intent intent, int requestCode, IntentResultListener listener);
}
