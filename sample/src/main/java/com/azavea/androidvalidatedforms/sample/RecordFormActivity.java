package com.azavea.androidvalidatedforms.sample;

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
import com.azavea.androidvalidatedforms.tasks.ValidationTask;

import java.util.ArrayList;

public class RecordFormActivity extends FormWithAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void displayForm() {
        super.displayForm();

        ViewGroup containerView = (ViewGroup) findViewById(R.id.form_elements_container);

        Button goBtn = new Button(this);
        goBtn.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT));

        goBtn.setText(getString(R.string.record_save_button));
        containerView.addView(goBtn);

        final RecordFormActivity thisActivity = this;
        goBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("RecordFormActivity", "Button clicked");
                new ValidationTask(thisActivity).execute();
            }
        });
    }

    @Override
    public FormController createFormController() {
        return new FormController(this, new TestModel());
    }

    @Override
    public void initForm() {
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
}
