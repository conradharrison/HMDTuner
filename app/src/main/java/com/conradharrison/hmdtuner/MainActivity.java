package com.conradharrison.hmdtuner;

import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String FIREBASE_URL = "https://vrhmdtuner.firebaseio.com/";
    public static final int QR_REQUEST_CODE = 42;

    // Number of parameters needed to control the render
    static final int HMD_PARAMS_SIZE = 8;

    // We will keep this array in sync with Firebase
    public static float[] mHMDParams = new float[HMD_PARAMS_SIZE];

    private GLSurfaceView mGLView;

    private Firebase mFirebaseRef;
    private String mFirebaseUID;

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == QR_REQUEST_CODE) {
            if (data.hasExtra("QRData")) {
                mQRText = data.getExtras().getString("QRData");
                Toast.makeText(this, mQRText, Toast.LENGTH_SHORT).show();

                // Firebase: Create a connection, log in, and add data listener.
                Firebase.setAndroidContext(this);

                mFirebaseRef = new Firebase(FIREBASE_URL);

                mFirebaseRef.authWithCustomToken(mQRText, new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        mFirebaseUID = authData.getUid();
                        Log.i(TAG, "Auth passed: " + mFirebaseUID);
                    }
                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        Log.e(TAG, "Auth failed: " + firebaseError.getMessage());
                        Toast.makeText(MainActivity.this, "Authentication with database server failed. Please restart server and try again.", Toast.LENGTH_SHORT).show();
                        MainActivity.this.finish();
                    }
                });

                //Value event listener for realtime data update
                mFirebaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        mHMDParams[0] = snapshot.child("users").child(mFirebaseUID).child("screen_to_lens_distance").getValue(Double.class).floatValue();
                        mHMDParams[1] = snapshot.child("users").child(mFirebaseUID).child("inter_lens_distance").getValue(Double.class).floatValue();
                        mHMDParams[2] = snapshot.child("users").child(mFirebaseUID).child("distortion_coefficients").child("0").getValue(Double.class).floatValue();
                        mHMDParams[3] = snapshot.child("users").child(mFirebaseUID).child("distortion_coefficients").child("1").getValue(Double.class).floatValue();
                        mHMDParams[4] = snapshot.child("users").child(mFirebaseUID).child("left_eye_field_of_view_angles").child("0").getValue(Double.class).floatValue();
                        mHMDParams[5] = snapshot.child("users").child(mFirebaseUID).child("left_eye_field_of_view_angles").child("1").getValue(Double.class).floatValue();
                        mHMDParams[6] = snapshot.child("users").child(mFirebaseUID).child("left_eye_field_of_view_angles").child("2").getValue(Double.class).floatValue();
                        mHMDParams[7] = snapshot.child("users").child(mFirebaseUID).child("left_eye_field_of_view_angles").child("3").getValue(Double.class).floatValue();

                        Log.i(TAG, mHMDParams[0] + ","
                                + mHMDParams[1] + ","
                                + mHMDParams[2] + ","
                                + mHMDParams[3] + ","
                                + mHMDParams[4] + ","
                                + mHMDParams[5] + ","
                                + mHMDParams[6] + ","
                                + mHMDParams[7]);

                        mGLView.requestRender();
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.e(TAG, "Firebase onDataChange() failed: " + firebaseError.getMessage());
                        Toast.makeText(MainActivity.this, "Sync with database server failed. Please check database/server, and try again.", Toast.LENGTH_SHORT).show();
                        MainActivity.this.finish();
                    }
                });
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
