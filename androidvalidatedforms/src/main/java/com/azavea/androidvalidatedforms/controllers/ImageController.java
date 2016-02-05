package com.azavea.androidvalidatedforms.controllers;

import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.azavea.androidvalidatedforms.FormActivityBase;
import com.azavea.androidvalidatedforms.IntentResultListener;
import com.azavea.androidvalidatedforms.R;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Control for an image. Can either take a new picture with camera, or select existing image.
 * Model for image control is expected to be a string to hold the path to the image on the device.
 *
 * Created by kathrynkillebrew on 2/1/16.
 */
public class ImageController<T extends Context & FormActivityBase> extends LabeledFieldController
    implements IntentResultListener {

    private static final SimpleDateFormat IMAGE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());

    private static final String LOG_LABEL = "ImageControl";

    private ImageView imageView;

    private static final int CAMERA_REQUEST = 11;
    private static final int FILE_REQUEST = 22;

    private final String TAKE_PHOTO_PROMPT;
    private final String SELECT_PHOTO_FILE_PROMPT;
    private final String CANCEL_ACTION;

    private WeakReference<T> callingActivity;
    private Uri currentPhotoPath;

    public ImageController(T ctx, String name, String labelText, boolean isRequired) {
        super(ctx, name, labelText, isRequired);
        callingActivity = new WeakReference<>(ctx);

        TAKE_PHOTO_PROMPT = ctx.getString(R.string.image_take_with_camera);
        SELECT_PHOTO_FILE_PROMPT = ctx.getString(R.string.image_pick_file);
        CANCEL_ACTION = ctx.getString(R.string.cancel);
    }

    @Override
    protected View createView() {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.form_labeled_image_element, null);
        errorView = (TextView) view.findViewById(R.id.field_error);

        TextView label = (TextView)view.findViewById(R.id.field_label);
        if (labelText == null) {
            label.setVisibility(View.GONE);
        } else {
            label.setText(labelText);
        }

        ImageButton imageButton = (ImageButton) view.findViewById(R.id.image_button);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_LABEL, "image button clicked");
                promptForImage();
            }
        });

        imageView = (ImageView) view.findViewById(R.id.image_view);
        if (getModelValue() != null) {
            refresh();
        }

        return view;
    }

    @Override
    protected View createFieldView() {
        // using a custom layout, so nothing to do here
        return null;
    }

    /**
     * Use this method to set image path on model instead of calling
     * {@link com.azavea.androidvalidatedforms.FormModel#setValue(String, Object)} directly.
     *
     * Override to use backing model that is not itself a String.
     *
     * @param newImagePath Path to set on model
     */
    protected void setModelValue(String newImagePath) {
        getModel().setValue(getName(), newImagePath);
    }

    /**
     * Use this method to retrieve image path on model instead of calling
     * {@link com.azavea.androidvalidatedforms.FormModel#getValue(String)} directly.
     *
     * Override to use backing model that is not itself a String.
     *
     * @return String value of path to image
     */
    protected Object getModelValue() {
        return getModel().getValue(getName());
    }

    @Override
    public void refresh() {
        Object value = getModelValue();
        String valueStr = value != null ? value.toString() : "";

        if (!valueStr.isEmpty()) {
            setImageToPath(valueStr);
        } else {
            // have no image set yet
            imageView.setVisibility(View.GONE);
        }
    }

    /**
     * Helper to set image, check if it succeeded, and clear path on model if it failed
     *
     * @param path Path to the image file to use
     */
    private void setImageToPath(String path) {
        if (!setDownscaledImageFromFilePath(imageView, path, 200, 200)) {
            // failed to set image; clear path string that was set
            setModelValue(null);
            setNeedsValidation();
        }
    }

    /**
     * Display dialog to set image either by taking a new picture with the camera, or picking
     * an existing image from the gallery.
     */
    private void promptForImage() {
        Context context = getContext();
        final CharSequence[] items = {TAKE_PHOTO_PROMPT, SELECT_PHOTO_FILE_PROMPT, CANCEL_ACTION};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.image_picker_button_label));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                T caller = callingActivity.get();
                if (caller == null) {
                    Log.e(LOG_LABEL, "Calling activity has gone!");
                    return;
                }

                currentPhotoPath = null;

                if (items[item].equals(TAKE_PHOTO_PROMPT)) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                        try {
                            // tell camera intent where to save the photo
                            File photoFile = createImageFile();

                            // if external media is not mounted and that is where images go,
                            // the camera will display an appropriate message when opened
                            // without EXTRA_OUTPUT set

                            if (photoFile != null) {
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                            }
                            caller.launchIntent(intent, CAMERA_REQUEST, ImageController.this);
                        } catch (IOException e) {
                            Log.e(LOG_LABEL, "Could not create file to save image into");
                            e.printStackTrace();
                        }
                    } else {
                        // should be handled with app requirements
                        Log.e(LOG_LABEL, "Device has no camera! Require camera in your AndroidManifest.xml");
                    }
                } else if (items[item].equals(SELECT_PHOTO_FILE_PROMPT)) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    caller.launchIntent(Intent.createChooser(intent, "Select File"), FILE_REQUEST, ImageController.this);
                } else if (items[item].equals(CANCEL_ACTION)) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    /**
     * Called by activity when camera or file picker intent result returns.
     *
     * @param requestCode The request code sent
     * @param resultCode Should be "OK"
     * @param resultData The intent results; content depends on the intent launched
     */
    @Override
    public void gotIntentResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode != Activity.RESULT_OK) {
            Log.w(LOG_LABEL, "intent result not ok; doing nothing");
            return;
        }

        if (requestCode == CAMERA_REQUEST) {

            if (currentPhotoPath != null) {
                // camera saved image to external media
                Log.d(LOG_LABEL, "full image saved to " + currentPhotoPath.getPath());

                // store image path to model
                setModelValue(currentPhotoPath.getPath());
                setNeedsValidation();

                // update image gallery to include the new pic
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(currentPhotoPath);
                getContext().sendBroadcast(mediaScanIntent);
            }
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

            // store image path to model
            setModelValue(imagePath);
            setNeedsValidation();
        } else {
            Log.w(LOG_LABEL, "got unrecognized intent result");
        }
    }

    /**
     * Create a file to use for storing a photo taken by the camera.
     *
     * @return File created in media directory, in a subdirectory that is the app name
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        String appName = getContext().getApplicationInfo().loadLabel(getContext().getPackageManager()).toString();

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File picturePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

            File mediaStorageDir = new File(picturePath, appName);

            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.e(LOG_LABEL, "failed to create directory");
                    return null;
                }
            }

            // Create a media file name
            String timeStamp = IMAGE_DATE_FORMAT.format(new Date());
            File imageFile = File.createTempFile("IMG_" + timeStamp + "_", ".jpg", mediaStorageDir);
            currentPhotoPath = Uri.fromFile(imageFile);

            return imageFile;
        } else {
            Log.e(LOG_LABEL, "No external media mounted or emulated");
            currentPhotoPath = null;
            return null;
        }
    }

    /**
     * Helper to downscale an image and put it into a view.
     * See: http://developer.android.com/training/camera/photobasics.html
     *
     * @param view ImageView to set (can also be an ImageButton, which is a subclass of ImageView)
     * @param imagePath Path to the image to set into the view
     * @return true on success
     */
    public static boolean setDownscaledImageFromFilePath(ImageView view, String imagePath, int defaultWidth, int defaultHeight) {
        // first check if image file actually exists
        File file = new File(imagePath);
        if (!file.exists()) {
            Log.e(LOG_LABEL, "Image file does not exist at " + imagePath);
            view.setVisibility(View.GONE);
            return false;
        }

        // Get the dimensions of the view
        int targetW = view.getWidth();
        int targetH = view.getHeight();

        if (targetW < defaultWidth) {
            targetW = defaultWidth;
        }

        if (targetH < defaultHeight) {
            targetH = defaultHeight;
        }

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);

        if (bitmap == null) {
            Log.e(LOG_LABEL, "Failed to decode bitmap at " + imagePath);
            view.setVisibility(View.GONE);
            return false;
        }

        // camera may not necessarily store image right side up
        // get orientation information set on bitmap, if any
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(-90);
                    break;
                default:
                    // no rotation needed
                    view.setImageBitmap(bitmap);
                    return true;
            }

            // rotate image
            Bitmap oriented = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            view.setImageBitmap(oriented);

        } catch (IOException e) {
            Log.e(LOG_LABEL, "Failed to get EXIF information to rotate image");
            e.printStackTrace();
            view.setImageBitmap(bitmap);
        } catch (OutOfMemoryError e) {
            Log.e(LOG_LABEL, "Ran out of memory rotating image! Need to downscale further?");
            e.printStackTrace();
            view.setImageBitmap(bitmap);
        }

        view.setVisibility(View.VISIBLE);
        return true;
    }
}
