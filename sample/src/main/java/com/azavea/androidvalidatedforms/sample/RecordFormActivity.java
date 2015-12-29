package com.azavea.androidvalidatedforms.sample;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.azavea.androidvalidatedforms.FormController;
import com.azavea.androidvalidatedforms.FormWithAppCompatActivity;
import com.azavea.androidvalidatedforms.controllers.EditTextController;
import com.azavea.androidvalidatedforms.controllers.FormSectionController;
import com.azavea.androidvalidatedforms.controllers.SelectionController;

import java.util.ArrayList;

public class RecordFormActivity extends FormWithAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewGroup containerView = (ViewGroup) findViewById(R.id.form_elements_container);

        Button goBtn = new Button(this);
        goBtn.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT));

        goBtn.setText(getString(R.string.record_save_button));
        containerView.addView(goBtn);

        goBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("RecordFormActivity", "Button clicked");
                FormController controller = getFormController();
                new ValidateTask(controller).execute();
            }
        });
    }

    @Override
    protected FormController createFormController() {
        return new FormController(this, new TestModel());
    }

    @Override
    protected void initForm() {
        final FormController formController = getFormController();
        formController.addSection(addSectionModel());
    }

    private FormSectionController addSectionModel() {
        FormSectionController section = new FormSectionController(this, "Test Model");
        section.addElement(new EditTextController(this, "FirstName", "first name", "first name"));
        section.addElement(new EditTextController(this, "LastName", "last name", "last name"));

        ArrayList<String> colors = new ArrayList<>(3);
        colors.add("red");
        colors.add("blue");
        colors.add("green");

        section.addElement(new SelectionController(this, "FavoriteColor", "favorite color",
                true, "Select", colors, colors));

        return section;
    }

    private class ValidateTask extends AsyncTask<Void, Void, Void> {

        FormController controller;
        public ValidateTask(FormController controller) {
            this.controller = controller;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressBar();
            controller.resetValidationErrors();
        }

        @Override
        protected Void doInBackground(Void... params) {
            controller.validateInput();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            controller.showValidationErrors();
            hideProgressBar();
            Log.d("ValidationTask", "Validation done!");
        }
    }

}
