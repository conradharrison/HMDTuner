package com.conradharrison.hmdtuner;

import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private static final String FIREBASE_URL = "https://hmdtuner.firebaseio.com/";
    public static final int QR_REQUEST_CODE = 42;

    private GLSurfaceView mGLView;
    private Firebase mFirebaseRef;

    public String mQRText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLView = new MyGLSurfaceView(this);
        setContentView(mGLView);

        if (savedInstanceState == null) {
            Intent intent = new Intent(this, QRActivity.class);
            startActivityForResult(intent, QR_REQUEST_CODE); // This intent will end automatically.
        } else {
            mQRText = savedInstanceState.getString("QRData");
        }

        //mFirebaseRef = new Firebase(FIREBASE_URL).child("data");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == QR_REQUEST_CODE) {
            if (data.hasExtra("QRData")) {
                mQRText = data.getExtras().getString("QRData");
                Toast.makeText(this, mQRText, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putString("QRData", mQRText);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            mGLView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}
