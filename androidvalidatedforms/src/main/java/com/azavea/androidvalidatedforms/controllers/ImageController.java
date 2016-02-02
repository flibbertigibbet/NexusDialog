package com.azavea.androidvalidatedforms.controllers;

import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.azavea.androidvalidatedforms.FormActivityBase;
import com.azavea.androidvalidatedforms.FormController;
import com.azavea.androidvalidatedforms.IntentResultListener;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Control for an image. Can either take a new picture with camera, or select existing image.
 *
 * Created by kathrynkillebrew on 2/1/16.
 */
public class ImageController<T extends Context & FormActivityBase> extends LabeledFieldController
    implements IntentResultListener {

    private static final SimpleDateFormat IMAGE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());

    private static final String LOG_LABEL = "ImageControl";

    private final int imageButtonId = FormController.generateViewId();

    private ImageButton imageButton;

    private static final int CAMERA_REQUEST = 11;
    private static final int FILE_REQUEST = 22;

    private WeakReference<T> callingActivity;
    private Uri currentPhotoPath;

    public ImageController(T ctx, String name, String labelText, boolean isRequired) {
        super(ctx, name, labelText, isRequired);
        callingActivity = new WeakReference<>(ctx);
    }

    @Override
    protected View createFieldView() {
        Context context = getContext();

        imageButton = new ImageButton(context);
        imageButton.setId(imageButtonId);
        imageButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        imageButton.setMaxHeight(300);
        imageButton.setImageDrawable(ContextCompat.getDrawable(context, android.R.drawable.ic_menu_camera));

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_LABEL, "image button clicked");
                promptForImage();
            }
        });

        return imageButton;
    }

    @Override
    public void refresh() {
        Log.d(LOG_LABEL, "TODO: go refresh now");
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
                    Log.e(LOG_LABEL, "Calling activity has gone!");
                    return;
                }

                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                        try {
                            // tell camera intent where to save the photo
                            File photoFile = createImageFile();
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                            caller.launchIntent(intent, CAMERA_REQUEST, ImageController.this);
                        } catch (IOException e) {
                            Log.e(LOG_LABEL, "Could not create file to save image into");
                            e.printStackTrace();
                        }
                    } else {
                        // TODO: handle
                        Log.e(LOG_LABEL, "Device has no camera!");
                    }
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
            Log.w(LOG_LABEL, "intent result not ok; doing nothing");
            return;
        }

        if (requestCode == CAMERA_REQUEST) {
            // TODO: store image path to model
            Log.d(LOG_LABEL, "full image saved to " + currentPhotoPath);

            // update image gallery to include the new pic
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(currentPhotoPath);
            getContext().sendBroadcast(mediaScanIntent);

            setDownscaledImageFromFilePath(imageButton, currentPhotoPath.getPath());

        } else if (requestCode == FILE_REQUEST) {
            Uri imageUri = resultData.getData();
            String[] projection = {MediaStore.MediaColumns.DATA};
            CursorLoader loader = new CursorLoader(getContext(), imageUri, projection, null, null, null);
            Cursor cursor = loader.loadInBackground();
            int col_idx = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            String imagePath = cursor.getString(col_idx);
            cursor.close();
            Log.d(LOG_LABEL, "Have image path: " + imagePath);

            setDownscaledImageFromFilePath(imageButton, imagePath);
        } else {
            Log.w(LOG_LABEL, "got unrecognized intent result");
        }
    }

    private File createImageFile() throws IOException {
        //File directory = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        String appName = getContext().getApplicationInfo().loadLabel(getContext().getPackageManager()).toString();
        Log.d(LOG_LABEL, "App name is: " + appName);

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // TODO: deal
            Log.e(LOG_LABEL, "No external media");
        }

        File picturePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File mediaStorageDir = new File(picturePath, appName);

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(LOG_LABEL, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = IMAGE_DATE_FORMAT.format(new Date());
        File imageFile = File.createTempFile("IMG_" + timeStamp + "_", ".jpg", mediaStorageDir);
        currentPhotoPath = Uri.fromFile(imageFile);

        return imageFile;
    }

    /**
     * Helper to downscale an image and put it into a view.
     * See: http://developer.android.com/training/camera/photobasics.html
     *
     * @param view ImageView to set (can also be an ImageButton, which is a subclass of ImageView)
     * @param imagePath Path to the image to set into the view
     */
    public static void setDownscaledImageFromFilePath(ImageView view, String imagePath) {
        // Get the dimensions of the view
        int targetW = view.getWidth();
        int targetH = view.getHeight();

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
        view.setImageBitmap(bitmap);
    }
}
