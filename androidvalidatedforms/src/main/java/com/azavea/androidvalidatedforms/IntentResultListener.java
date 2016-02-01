package com.azavea.androidvalidatedforms;

import android.content.Intent;

/**
 * Created by kathrynkillebrew on 2/1/16.
 */
public interface IntentResultListener {
    void gotIntentResult(int requestCode, int resultCode, Intent resultData);
}
