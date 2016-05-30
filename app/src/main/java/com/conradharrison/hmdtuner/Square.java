package com.conradharrison.hmdtuner;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.content.Context;
import android.opengl.GLES20;

/**
 * A two-dimensional square for use as a drawn object in OpenGL ES 2.0.
 */
public class Square {

    private final FloatBuffer vertexBuffer;
    private final FloatBuffer texelBuffer;
    private final ShortBuffer drawListBuffer;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private int mZoomHandle;
    private int mStrengthHandle;

    private FloatBuffer mCubeTextureCoordinates;
    private int mTextureUniformHandle;
    private int mTextureCoordinateHandle;
    private int mBrickDataHandle;

    private float mZoom;
    private float mStrength;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float squareCoords[] = {
            -1.00f,  1.00f, 0.0f,   // top left
            -1.00f, -1.00f, 0.0f,   // bottom left
            1.00f, -1.00f, 0.0f,   // bottom right
            1.00f,  1.00f, 0.0f }; // top right

    static final int COORDS_PER_TEXEL = 2;
    static float squareTextureCoords[] = {
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
            0.0f, 0.0f
            };

    private final short drawOrder[] = {
            0, 1, 2, 0, 2, 3 }; // order to draw vertices

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private final int texelStride = COORDS_PER_TEXEL * 4; // 4 bytes per vertex

    float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Square(Context context) {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        // 4 bytes per float
        ByteBuffer tb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                squareTextureCoords.length * 4);
        tb.order(ByteOrder.nativeOrder());
        texelBuffer = tb.asFloatBuffer();
        texelBuffer.put(squareTextureCoords);
        texelBuffer.position(0);

        // Read in an image and set as texture, store handle for later.
        mBrickDataHandle = TextureHelper.loadTexture(context, R.raw.grid);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBrickDataHandle);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     */
    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(MyGLRenderer.mProgram);

        mMVPMatrixHandle = GLES20.glGetUniformLocation(MyGLRenderer.mProgram, "u_MVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        mPositionHandle = GLES20.glGetAttribLocation(MyGLRenderer.mProgram, "a_Position");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        mTextureCoordinateHandle = GLES20.glGetAttribLocation(MyGLRenderer.mProgram, "a_TexCoordinate");
        GLES20.glVertexAttribPointer(
                mTextureCoordinateHandle, COORDS_PER_TEXEL,
                GLES20.GL_FLOAT, false,
                texelStride, texelBuffer);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        mStrengthHandle = GLES20.glGetUniformLocation(MyGLRenderer.mProgram, "u_Strength");
        GLES20.glUniform1f(mStrengthHandle, 1.0f);

        mZoomHandle = GLES20.glGetUniformLocation(MyGLRenderer.mProgram, "u_Zoom");
        GLES20.glUniform1f(mZoomHandle, 1.00f);

        mColorHandle = GLES20.glGetUniformLocation(MyGLRenderer.mProgram, "u_Color");
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        mTextureUniformHandle = GLES20.glGetUniformLocation(MyGLRenderer.mProgram, "u_Texture");
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        // Draw the square
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }


}