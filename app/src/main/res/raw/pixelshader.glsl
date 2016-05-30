precision mediump float;

uniform float strength;
uniform float zoom;
uniform vec4 u_Color;
uniform sampler2D u_Texture;

varying vec3 v_Position;		// Interpolated position for this fragment. Unused
varying vec2 v_TexCoordinate;   // Interpolated texture coordinate per fragment.

void main() {
    gl_FragColor = u_Color * texture2D(u_Texture, v_TexCoordinate);
}