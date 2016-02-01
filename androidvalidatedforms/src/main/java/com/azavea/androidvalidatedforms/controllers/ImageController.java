package com.azavea.androidvalidatedforms.controllers;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.azavea.androidvalidatedforms.FormActivityBase;
import com.azavea.androidvalidatedforms.FormController;
import com.azavea.androidvalidatedforms.IntentResultListener;
import com.azavea.androidvalidatedforms.R;

import java.lang.ref.WeakReference;

/**
 * Control for an image. Can either take a new picture with camera, or select existing image.
 *
 * Created by kathrynkillebrew on 2/1/16.
 */
public class ImageController<T extends Context & FormActivityBase> extends LabeledFieldController
    implements IntentResultListener {

    private final int imageViewId = FormController.generateViewId();
    private final int buttonId = FormController.generateViewId();
    private final int imageHolderId = FormController.generateViewId();

    private ImageView imageView;
    private Button pickImageButton;
    private LinearLayout imageHolder;

    private static final int CAMERA_REQUEST = 11;
    private static final int FILE_REQUEST = 22;

    private WeakReference<T> callingActivity;

    public ImageController(T ctx, String name, String labelText, boolean isRequired) {
        super(ctx, name, labelText, isRequired);
        callingActivity = new WeakReference<>(ctx);
    }

    @Override
    protected View createFieldView() {
        Context context = getContext();

        imageHolder = new LinearLayout(context);
        imageHolder.setLayoutParams(new ViewGroup.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        imageHolder.setId(imageHolderId);

        imageView = new ImageView(context);
        imageView.setId(imageViewId);
        imageHolder.addView(imageView);

        pickImageButton = new Button(context);
        pickImageButton.setId(buttonId);
        pickImageButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        pickImageButton.setText(context.getString(R.string.image_picker_button_label));
        imageHolder.addView(pickImageButton);

        pickImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ImageControl", "Hey, that tickles!");
                promptForImage();
            }
        });

        return imageHolder;
    }

    @Override
    public void refresh() {
        Log.d("ImageControl", "Um, like, go refresh now?");
    }

    private void promptForImage() {
        // TODO: strings, strings everywhere! Put them in strings.xml
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                T caller = callingActivity.get();
                if (caller == null) {
                    Log.e("ImageControl", "Calling activity has gone!");
                    return;
                }

                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    caller.launchIntent(intent, CAMERA_REQUEST, ImageController.this);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    caller.launchIntent(Intent.createChooser(intent, "Select File"), FILE_REQUEST, ImageController.this);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void gotIntentResult(int requestCode, int resultCode, Intent resultData) {
        Log.d("ImageControl", "got intent result for request: " + requestCode);

        if (resultCode != Activity.RESULT_OK) {
            Log.d("ImageControl", "intent result not ok; doing nothing");
            return;
        }

        if (requestCode == CAMERA_REQUEST) {
            Bitmap bitmap = getBitmapFromCameraData(resultData);
            // TODO: use

            // get the thumbnail
            Bundle extras = resultData.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
            ///////////////////////////

        } else if (requestCode == FILE_REQUEST) {

        } else {
            Log.w("ImageControl", "got unrecognized intent result");
        }
    }

    // get the full-size photo
    private Bitmap getBitmapFromCameraData(Intent data) {
        Uri selectedImage = data.getData();
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContext().getContentResolver().query(selectedImage,filePathColumn, null, null, null);
        if (cursor == null) {
            Log.e("ImageControl", "Could not get cursor for camera data!");
            return null;
        }

        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return BitmapFactory.decodeFile(picturePath);
    }

    /**
     * http://developer.android.com/training/camera/photobasics.html
     */
    private void setFullImageFromFilePath(String imagePath) {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }
}
