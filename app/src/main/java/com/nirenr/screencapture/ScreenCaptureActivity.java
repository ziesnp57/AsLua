package com.nirenr.screencapture;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.widget.TextView;

public class ScreenCaptureActivity extends Activity {

    public static final int REQUEST_MEDIA_PROJECTION = 18;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView view = new TextView(this);
        view.setText("请授予权限");
        setContentView(view);
        requesturePermission();
    }

    public void requesturePermission() {

        try {
            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                    getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            startActivityForResult(
                    mediaProjectionManager.createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION);
        } catch (Exception e) {
            e.printStackTrace();
            ScreenShot.setResultData(null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == RESULT_OK && data != null) {
                ScreenShot.setResultData(data);
                //Toast.makeText(this,"获得权限成功",Toast.LENGTH_SHORT).show();
            }
        } else {
            ScreenShot.setResultData(null);
        }
        finish();
    }

    @Override
    public void finish() {
        finishAndRemoveTask();
    }
}
