package com.azavea.androidvalidatedforms.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Task to resize (generally downscale) an image read from a file path and set it on an ImageView.
 *
 * See: http://developer.android.com/training/camera/photobasics.html
 * Created by kathrynkillebrew on 2/8/16.
 */
public class ResizeImageTask extends AsyncTask<String, Void, Bitmap> {

    private static final String LOG_LABEL = "ResizeImageTask";

    public interface ResizeImageCallback {
        void imageNotSet();
    }

    WeakReference<ResizeImageCallback> listener;
    WeakReference<ImageView> imageViewReference;
    int defaultWidth;
    int defaultHeight;
    int targetWidth;
    int targetHeight;

    /**
     * Create task to resize image.
     *
     * @param imageView ImageView to set (can also be an ImageButton, which is a subclass of ImageView)
     * @param defaultWidth width to use in case image view reports zero width
     * @param defaultHeight height to use in case image view reports zero height
     * @param listener task calls back to this listener if task failed (image could not be read)
     */
    public ResizeImageTask(ImageView imageView, int defaultWidth, int defaultHeight, ResizeImageCallback listener) {
        this.defaultWidth = defaultWidth;
        this.defaultHeight = defaultHeight;

        this.targetWidth = imageView.getWidth();
        this.targetHeight = imageView.getHeight();
        this.imageViewReference = new WeakReference<>(imageView);
        this.listener = new WeakReference<>(listener);
    }

    @Override
    protected Bitmap doInBackground(String... params) {

        String imagePath = params[0];

        // first check if image file actually exists
        File file = new File(imagePath);
        if (!file.exists()) {
            Log.e(LOG_LABEL, "Image file does not exist at " + imagePath);
            cancel(true);
            return null;
        }

        if (targetWidth < defaultWidth) {
            targetWidth = defaultWidth;
        }

        if (targetHeight < defaultHeight) {
            targetHeight = defaultHeight;
        }

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale the image
        int scaleFactor = Math.min(photoW/targetWidth, photoH/targetHeight);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);

        if (bitmap == null) {
            Log.e(LOG_LABEL, "Failed to decode bitmap at " + imagePath);
            cancel(true);
            return null;
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
                    return bitmap;
            }

            // rotate image
            Bitmap oriented = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return oriented;

        } catch (IOException e) {
            Log.e(LOG_LABEL, "Failed to get EXIF information to rotate image");
            e.printStackTrace();
            return bitmap;
        } catch (OutOfMemoryError e) {
            Log.e(LOG_LABEL, "Ran out of memory rotating image! Need to downscale further?");
            e.printStackTrace();
            return bitmap;
        }
    }

    /**
     * If task cancelled, notify listener and hide the image view.
     * Perhaps the file has gone missing, or is corrupt.
     */
    @Override
    protected void onCancelled() {
        ResizeImageCallback caller = listener.get();
        if (caller != null) {
            caller.imageNotSet();
        }

        // hide image view
        ImageView view = imageViewReference.get();
        if (view != null) {
            view.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap != null) {
            ImageView view = imageViewReference.get();
            if (view != null) {
                view.setImageBitmap(bitmap);
                view.setVisibility(View.VISIBLE);
            }
        }
    }
}
