package com.azavea.androidvalidatedforms.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.azavea.androidvalidatedforms.FormActivityBase;

import java.lang.ref.WeakReference;

/**
 * Validate a form in the background. Show progress indicator while validating.
 *
 * Created by kathrynkillebrew on 12/29/15.
 */
public class ValidationTask extends AsyncTask<Void, Void, Void> {

    WeakReference<FormActivityBase> activity;

    public ValidationTask(FormActivityBase activity) {
        this.activity = new WeakReference<>(activity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        FormActivityBase activityBase = activity.get();
        if (activityBase != null) {
            activityBase.showProgress(true);
            activityBase.getFormController().resetValidationErrors();
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        FormActivityBase activityBase = activity.get();
        if (activityBase != null) {
            activityBase.getFormController().validateInput();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        FormActivityBase activityBase = activity.get();
        if (activityBase != null) {
            activityBase.getFormController().showValidationErrors();
            activityBase.showProgress(false);
            Log.d("ValidationTask", "Validation done!");
        }
    }
}
