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

        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

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
        float[] scratch = new float[16];

        // Draw background color
        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        int [] viewPortParams = new int[4];
        GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, viewPortParams, 0);
        mScreenWidth = viewPortParams[2];
        mScreenHeight = viewPortParams[3];

        // Draw left square
        GLES20.glViewport(0, 0, mScreenWidth/2, mScreenHeight);
        mSquare.draw(mMVPMatrix);

        // Draw right square
        GLES20.glViewport(mScreenWidth/2, 0, mScreenWidth/2, mScreenHeight);
        mSquare.draw(mMVPMatrix);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        mScreenWidth = width;
        mScreenHeight = height;
        GLES20.glViewport(0, 0, width, height);

        float ratio = 1.0f; //(float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);

        // Calculate MVP matrix
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
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
