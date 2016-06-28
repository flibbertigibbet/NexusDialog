package com.azavea.androidvalidatedforms;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;

import com.azavea.androidvalidatedforms.controllers.ImageController;
import com.azavea.androidvalidatedforms.tasks.DisplayFormTask;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * <code>FormWithAppCompatActivity</code> is provides a default Activity implementation for using NexusDialog. If you'd
 * like the Activity to be based on the standard Android <code>Activity</code>, you can use {@link FormActivity}
 */
public abstract class FormWithAppCompatActivity extends AppCompatActivity implements FormActivityBase {

    private static final String LOG_LABEL = "AppCompatFormActivity";
    private static final String MODEL_BUNDLE_KEY = "android_validated_forms_model";
    private FormController formController;
    private View progressBar;
    private View scrollView;

    private boolean formReady;
    private FormReadyListener formReadyListener;
    private HashMap<Integer, WeakReference<IntentResultListener>> intentListeners;
    private WeakReference<ExternalWriteRequest> externalWriteRequestListener;

    // form layout may be overridden, if it contains the expected components with matching IDs
    protected int formLayout = R.layout.form_activity;

    @Override
    public void setExternalWriteRequestListener(ExternalWriteRequest listener) {
        this.externalWriteRequestListener = new WeakReference<>(listener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        ExternalWriteRequest listener = externalWriteRequestListener.get();
        if (listener == null) {
            Log.w(LOG_LABEL, "External write permissions listener has gone; not sending result");
            return;
        }

        switch (requestCode) {
            case ImageController.EXTERNAL_STORAGE_WRITE_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d(LOG_LABEL, "User granted permission to write external storage");
                    listener.gotResult(true);

                } else {
                    Log.e(LOG_LABEL, "User denied permission to write external storage!");
                    listener.gotResult(false);
                }
                return;
            }

            default:
                Log.w(LOG_LABEL, "Got unrecognized permission request result for code " + String.valueOf(requestCode));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(formLayout);
        progressBar = findViewById(R.id.form_progress);
        scrollView = findViewById(R.id.form_scrollview);

        getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_RESIZE | LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        formReady = false;
        intentListeners = new HashMap<>();
        new DisplayFormTask(this).execute();
    }

    /**
     * Reconstructs the form element views. This must be called after form elements are dynamically added or removed.
     */
    protected void recreateViews() {
        ViewGroup containerView = (ViewGroup) findViewById(R.id.form_elements_container);
        getFormController().recreateViews(containerView);
    }
    
    public void displayForm() {
        FragmentManager fm = getSupportFragmentManager();
        FormModel retainedModel = (FormModel) fm.findFragmentByTag(MODEL_BUNDLE_KEY);

        if (retainedModel == null) {
            retainedModel = getFormController().getModel();
            fm.beginTransaction().add(retainedModel, MODEL_BUNDLE_KEY).commit();
        }

        recreateViews();
    }

    public boolean isFormReady() {
        return formReady;
    }

    public void formIsReady() {
        this.formReady = true;

        if (formReadyListener != null) {
            formReadyListener.formReadyCallback();
        }
    }

    public void setFormReadyListener(FormReadyListener listener) {
        formReadyListener = listener;
    }

    /**
     * Responsible for creating a formController with the model object.
     * formController = new FormController(this, someObj);
     */
    public abstract FormController createFormController();

    /**
     * An abstract method that must be overridden by subclasses where the form fields are initialized.
     */
    public abstract void initForm();

    /**
     * Returns the associated form controller
     */
    public FormController getFormController() {
        if (formController == null) {
            formController = createFormController();
        }
        return formController;
    }

    /**
     * Returns the associated form model
     */
    public FormModel getModel() {
        return getFormController().getModel();
    }

    /**
     * Shows the progress UI and hides the form. Taken from login form sample.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            scrollView.setVisibility(show ? View.GONE : View.VISIBLE);
            scrollView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    scrollView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            progressBar.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            scrollView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void launchIntent(Intent intent, int requestCode, IntentResultListener listener) {
        intentListeners.put(requestCode, new WeakReference<>(listener));
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        WeakReference<IntentResultListener> listenerReference = intentListeners.get(requestCode);
        if (listenerReference != null) {
            IntentResultListener listener = listenerReference.get();
            if (listener != null) {
                listener.gotIntentResult(requestCode, resultCode, data);
            }
        }
    }
}
