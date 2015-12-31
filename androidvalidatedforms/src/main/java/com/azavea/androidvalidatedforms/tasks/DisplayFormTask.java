package com.azavea.androidvalidatedforms.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.azavea.androidvalidatedforms.FormActivityBase;

import java.lang.ref.WeakReference;

/**
 * Initialize form in background, displaying loader until it's ready.
 *
 * Created by kathrynkillebrew on 12/29/15.
 */
public class DisplayFormTask extends AsyncTask<Void, Void, Void> {

    WeakReference<FormActivityBase> activity;

    public DisplayFormTask(FormActivityBase activity) {
        this.activity = new WeakReference<>(activity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        FormActivityBase form = activity.get();
        if (form != null) {
            form.showProgress(true);
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        FormActivityBase form = activity.get();
        if (form != null) {
            form.initForm();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        FormActivityBase form = activity.get();
        if (form != null) {
            form.showProgress(false);
            form.displayForm();
            Log.d("DisplayTask", "Form loaded");
        }
    }
}
