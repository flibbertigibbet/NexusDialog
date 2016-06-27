package com.azavea.androidvalidatedforms.controllers;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.azavea.androidvalidatedforms.FormController;
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar;
import com.github.msarhan.ummalqura.calendar.UmmalquraDateFormatSymbols;

import net.alhazmy13.hijridatepicker.HijriCalendarDialog;
import net.alhazmy13.hijridatepicker.HijriCalendarView;

/**
 * Represents a field that allows selecting a specific date via a date picker.
 * <p/>
 * For the field value, the associated FormModel must return a {@link Date} instance. No selected date can be
 * represented by returning {@code null} for the value of the field.
 */
public class DatePickerController extends LabeledFieldController {
    private final int editTextId = FormController.generateViewId();

    private DatePickerDialog datePickerDialog = null;
    private TimePickerDialog timePickerDialog = null;
    private final SimpleDateFormat displayFormat;
    private Calendar calendar;
    private boolean showTimePicker = false;
    private boolean useHijri = false;
    private long maxDateTime = 0;

    /**
     * Constructs a new instance of a date picker field.
     *
     * @param ctx               the Android context
     * @param name              the name of the field
     * @param labelText         the label to display beside the field. Set to {@code null} to not show a label.
     * @param isRequired        indicates if the field is required or not
     * @param displayFormat     the format of the date to show in the text box when a date is set
     */
    public DatePickerController(Context ctx, String name, String labelText, boolean isRequired, SimpleDateFormat displayFormat) {
        super(ctx, name, labelText, isRequired);
        this.displayFormat = displayFormat;

        // default to use Hijri Ummalqura calendar if system language is Arabic or location is Saudi Arabia
        Locale locale = Locale.getDefault();
        this.useHijri = locale.getLanguage().startsWith("ar") || locale.getCountry().startsWith("SA");

        if (useHijri) {
            this.calendar = getUmmalquraCalendar();
        } else {
            this.calendar = Calendar.getInstance(locale);
        }

        this.calendar.setTimeZone(displayFormat.getTimeZone());
    }

    /**
     * Constructs a new instance of a date picker field, with the selected date displayed in "MMM d, yyyy" format.
     *
     * @param name              the name of the field
     * @param labelText         the label to display beside the field
     */
    public DatePickerController(Context context, String name, String labelText) {
        this(context, name, labelText, false, new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()));
    }

    /**
     * Constructor that takes parameter to show time picker.
     *
     * @param context           the Android context
     * @param name              the name of the field
     * @param labelText         the label to display beside the field. Set to {@code null} to not show a label.
     * @param isRequired        indicates if the field is required or not
     * @param showTimePicker    if true, show time picker after date component dismissed
     */
    public DatePickerController(Context context, String name, String labelText, boolean isRequired, boolean showTimePicker) {
        this(context, name, labelText, isRequired, new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()));
        this.showTimePicker = showTimePicker;
    }

    /**
     * Constructs a new instance of a date picker field, that optionally also displays a time picker.
     *
     * @param ctx               the Android context
     * @param name              the name of the field
     * @param labelText         the label to display beside the field. Set to {@code null} to not show a label.
     * @param isRequired        indicates if the field is required or not
     * @param displayFormat     the format of the date to show in the text box when a date is set
     * @param showTimePicker    if true, display a time picker after the date picker dismisses
     */
    public DatePickerController(Context ctx, String name, String labelText, boolean isRequired,
                                SimpleDateFormat displayFormat, boolean showTimePicker) {

        this(ctx, name, labelText, isRequired, displayFormat);
        this.showTimePicker = showTimePicker;
    }

    /**
     * Call to set calendar and date-picker to Hijri.
     */
    public void setUseHijri() {
        if (useHijri) {
            return; // already using Hijri date picker; nothing to do
        }

        useHijri = true;
        getUmmalquraCalendar();
    }

    /**
     * Set up the calendar for Hijri date symbols and formatting.
     *
     * @return Hijri calendar
     */
    public Calendar getUmmalquraCalendar() {
        Locale locale = Locale.getDefault();
        calendar = new UmmalquraCalendar(locale);
        calendar.setTime(new Date());
        displayFormat.setCalendar(calendar);
        // explicitly set the date format symbols; otherwise months display as incorrect Gregorian
        UmmalquraDateFormatSymbols ummalquara = new UmmalquraDateFormatSymbols();
        DateFormatSymbols symbols = new DateFormatSymbols(locale);
        symbols.setMonths(ummalquara.getMonths());
        symbols.setShortMonths(ummalquara.getShortMonths());
        displayFormat.setDateFormatSymbols(symbols);
        return calendar;
    }

    /**
     * Set the maximum allowed date in the date picker. Note that the time picker may still allow
     * setting to a future time, so it will be necessary to validate as well.
     *
     * @param time maximum allowed time (POSIX milliseconds) in the picker
     */
    public void setMaxDate(long time) {
        maxDateTime = time;
    }

    /**
     * Use the current date as the maximum allowed date in the picker.
     */
    public void setMaxDateToNow() {
        maxDateTime = System.currentTimeMillis();
    }

    @Override
    protected View createFieldView() {
        final EditText editText = new EditText(getContext());
        editText.setId(editTextId);

        editText.setSingleLine(true);
        editText.setInputType(InputType.TYPE_CLASS_DATETIME);
        editText.setKeyListener(null);
        refresh(editText);
        editText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(getContext(), editText);
            }
        });

        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDatePickerDialog(getContext(), editText);
                }
            }
        });

        return editText;
    }

    private void showDatePickerDialog(final Context context, final EditText editText) {
        // don't show dialog again if it's already being shown
        if (datePickerDialog == null) {
            Date date = (Date)getModel().getValue(getName());
            if (date == null) {
                date = new Date();
            }

            calendar.setTime(date);

            if (useHijri) {
                // custom Hijri date-picker
                HijriCalendarDialog.Builder builder = new HijriCalendarDialog.Builder(context)
                        .setMode(HijriCalendarDialog.Mode.Hijri)
                        .setOnDateSetListener(new HijriCalendarView.OnDateSetListener() {
                            @Override
                            public void onDateSet(int year, int monthOfYear, int dayOfMonth) {
                                calendar.set(year, monthOfYear, dayOfMonth);
                                getModel().setValue(getName(), calendar.getTime());
                                editText.setText(displayFormat.format(calendar.getTime()));
                            }
                        });

                if (Locale.getDefault().getLanguage().startsWith("ar")) {
                    builder.setUILanguage(HijriCalendarDialog.Language.Arabic);
                } else {
                    builder.setUILanguage(HijriCalendarDialog.Language.English);
                }

                HijriCalendarView view = builder.build();

                view.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        datePickerDialog = null;
                        if (showTimePicker) {
                            showTimePickerDialog(context, editText);
                        }
                    }
                });

                view.show();

            } else {
                // standard system Gregorian date-picker
                datePickerDialog = new DatePickerDialog(context, new OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(year, monthOfYear, dayOfMonth);
                        getModel().setValue(getName(), calendar.getTime());
                        editText.setText(displayFormat.format(calendar.getTime()));

                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

                datePickerDialog.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        datePickerDialog = null;
                        if (showTimePicker) {
                            showTimePickerDialog(context, editText);
                        }
                    }
                });

                if (maxDateTime > 0) {
                    datePickerDialog.getDatePicker().setMaxDate(maxDateTime);
                }
                datePickerDialog.show();
            }
        }
    }

    private void showTimePickerDialog(final Context context, final EditText editText) {
        // don't show dialog again if it's already being shown
        if (timePickerDialog == null) {
            Date date = (Date)getModel().getValue(getName());
            if (date == null) {
                date = new Date();
            }

            calendar.setTime(date);

            timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    getModel().setValue(getName(), calendar.getTime());
                    editText.setText(displayFormat.format(calendar.getTime()));
                }
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), android.text.format.DateFormat.is24HourFormat(context));

            timePickerDialog.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    timePickerDialog = null;
                }
            });

            timePickerDialog.show();
        }
    }

    private EditText getEditText() {
        return (EditText)getView().findViewById(editTextId);
    }

    private void refresh(EditText editText) {
        Date value = (Date)getModel().getValue(getName());
        editText.setText(value != null ? displayFormat.format(value) : "");
        setNeedsValidation();
    }

    public void refresh() {
        refresh(getEditText());
    }
}
