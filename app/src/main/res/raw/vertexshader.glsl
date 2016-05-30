uniform mat4 u_MVPMatrix;

attribute vec4 a_Position;		// Per-vertex position information we will pass in.
attribute vec2 a_TexCoordinate; // Per-vertex texture coordinate information we will pass in.

varying vec3 v_Position;		// This will be passed into the fragment shader.
varying vec2 v_TexCoordinate;   // This will be passed into the fragment shader.

void main() {
    // The matrix must be included as a modifier of gl_Position.
    // Note that the uMVPMatrix factor *must be first* in order
    // for the matrix multiplication product to be correct.
    gl_Position = u_MVPMatrix * a_Position;

    // Pass on the attributes
    v_Position = vec3(gl_Position); // Unused?
  	v_TexCoordinate = a_TexCoordinate;
}