package com.conradharrison.hmdtuner;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by conradh on 5/30/2016.
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "MyGLRenderer";
    private Context mContext;

    public static int mProgram;

    private int mScreenWidth;
    private int mScreenHeight;

    private Square mSquare;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    public MyGLRenderer(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        int vertexShader = ShaderHelper.compileShader(
                GLES20.GL_VERTEX_SHADER,
                getVertexShader());
        int fragmentShader = ShaderHelper.compileShader(
                GLES20.GL_FRAGMENT_SHADER,
                getFragmentShader());

        mProgram = ShaderHelper.createAndLinkProgram(
                vertexShader,
                fragmentShader,
                new String[] {"a_Position", "a_TexCoordinate"});

        // World setup
        mSquare   = new Square(mContext);
    }

    @Override
    public void onDrawFrame(GL10 unused) {

        // Draw background color
        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        float screenToLensDistance = MainActivity.mHMDParams[0];
        float interLensDistance = MainActivity.mHMDParams[1];
        float pixelsPerInch = MainActivity.mHMDParams[12];
        float pixelsPerMm = pixelsPerInch / 25.4f;
        float widthPixels = 1920.0f;
        float DEFAULT_INTER_LENS_DISTANCE = 60.f;
        float DEFAULT_SCREEN_TO_LENS_DISTANCE = 42.0f;
        float screenToLensFactor = (screenToLensDistance == 0.0f ? 1.0f : (DEFAULT_SCREEN_TO_LENS_DISTANCE / screenToLensDistance));
        float interEyeFactor = ((interLensDistance - DEFAULT_INTER_LENS_DISTANCE)/2) * pixelsPerMm * (4.0f*screenToLensFactor / widthPixels);


        // Draw left square
        Matrix.setLookAtM(mViewMatrix, 0, 0.0f-interEyeFactor, 0, -1.0f, 0.0f-interEyeFactor, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.orthoM(mProjectionMatrix, 0, -1.0f*screenToLensFactor, 1.0f*screenToLensFactor, -1.0f*screenToLensFactor, 1.0f*screenToLensFactor, 0.0f, 2.0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        GLES20.glViewport(0, 0, mScreenWidth/2, mScreenHeight);
        mSquare.draw(mMVPMatrix, MainActivity.mHMDParams, mScreenWidth, mScreenHeight);

        // Draw right square
        Matrix.setLookAtM(mViewMatrix, 0, 0.0f+interEyeFactor, 0, -1.0f, 0.0f+interEyeFactor, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.orthoM(mProjectionMatrix, 0, -1.0f*screenToLensFactor, 1.0f*screenToLensFactor, -1.0f*screenToLensFactor, 1.0f*screenToLensFactor, 0.0f, 2.0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        GLES20.glViewport(mScreenWidth/2, 0, mScreenWidth/2, mScreenHeight);
        mSquare.draw(mMVPMatrix, MainActivity.mHMDParams, mScreenWidth, mScreenHeight);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {

        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        mScreenWidth = width;
        mScreenHeight = height;
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    protected String getVertexShader()
    {
        return RawResourceReader.readTextFileFromRawResource(mContext, R.raw.vertexshader);
    }

    protected String getFragmentShader()
    {
        return RawResourceReader.readTextFileFromRawResource(mContext, R.raw.pixelshader_lens);
    }
}
