package com.azavea.androidvalidatedforms.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.azavea.androidvalidatedforms.FormActivityBase;
import com.azavea.androidvalidatedforms.FormController;

import java.lang.ref.WeakReference;

/**
 * Validate a form in the background. Show progress indicator while validating.
 *
 * Created by kathrynkillebrew on 12/29/15.
 */
public class ValidationTask extends AsyncTask<Void, Void, Boolean> {

    public interface ValidationCallback {
        void validationComplete(boolean isValid);
    }

    WeakReference<FormActivityBase> activity;

    public ValidationTask(FormActivityBase activity) {
        this.activity = new WeakReference<>(activity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        FormActivityBase activityBase = activity.get();
        if (activityBase != null) {

            // do not attempt validating form that has not finished loading yet
            if (!activityBase.isFormReady()) {
                cancel(true);
                return;
            }

            activityBase.showProgress(true);
            activityBase.getFormController().resetValidationErrors();
        }
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        FormActivityBase activityBase = activity.get();
        if (activityBase != null) {
            FormController controller = activityBase.getFormController();
            return controller.isValidInput();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean isValid) {
        super.onPostExecute(isValid);

        FormActivityBase activityBase = activity.get();
        if (activityBase != null) {
            activityBase.getFormController().showValidationErrors();
            activityBase.showProgress(false);
            Log.d("ValidationTask", "Validation done!");
            // call back to activity to let it know if form valid or not
            activityBase.validationComplete(isValid);
        }
    }
}
