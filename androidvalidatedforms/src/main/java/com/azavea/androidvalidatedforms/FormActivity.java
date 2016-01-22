package com.azavea.androidvalidatedforms;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;

import com.azavea.androidvalidatedforms.tasks.DisplayFormTask;

/**
 * <code>FormActivity</code> is provides a default Activity implementation for using NexusDialog. It provides simple APIs to quickly
 * create and manage form fields. If you'd like the Activity to be based on <code>AppCompatActivity</code>, you can use
 * {@link FormWithAppCompatActivity}
 */
public abstract class FormActivity extends FragmentActivity implements FormActivityBase {
    private static final String MODEL_BUNDLE_KEY = "android_validated_forms_model";
    private FormController formController;
    private View progressBar;
    private View scrollView;

    private boolean formReady;
    private FormReadyListener formReadyListener;

    // form layout may be overridden, if it contains the expected components with matching IDs
    protected int formLayout = R.layout.form_activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(formLayout);
        progressBar = findViewById(R.id.form_progress);
        scrollView = findViewById(R.id.form_scrollview);

        getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_RESIZE | LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        formReady = false;
        new DisplayFormTask(this).execute();
    }

    /**
     * Reconstructs the form element views. This must be called after form elements are dynamically added or removed.
     */
    protected void recreateViews() {
        ViewGroup containerView = (ViewGroup) findViewById(R.id.form_elements_container);
        getFormController().recreateViews(containerView);
    }

    /**
     *
     */
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
}
