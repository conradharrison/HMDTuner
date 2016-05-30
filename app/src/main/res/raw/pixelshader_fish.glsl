//precision mediump float;

uniform float u_Strength;
uniform float u_Zoom;
uniform vec4 u_Color;
uniform sampler2D u_Texture;

varying vec3 v_Position;		// Interpolated position for this fragment. Unused
varying vec2 v_TexCoordinate;   // Interpolated texture coordinate per fragment.

vec2 mapper(vec2 in_tex)
{
    vec2 out_tex;

    float input_width = 1.0;
    float input_height = 1.0;
    float half_height = input_height / 2.0;
    float half_width = input_width / 2.0;

    float r0 = sqrt(input_height*input_height + input_width*input_width) / u_Strength;

    float new_x  = in_tex.x - half_width;
    float new_y  = in_tex.y - half_height;

    float r = sqrt(new_x*new_x + new_y*new_y) / r0;

    float theta;

    if (r==0.0)
        theta = 1.0;
    else
        theta = atan(r) / r;

    out_tex.x = half_width + theta * new_x * u_Zoom;
    out_tex.y = half_height + theta * new_y * u_Zoom;

    return out_tex;
}

void main() {
    vec2 tex_xy;
    tex_xy = mapper(v_TexCoordinate);
    gl_FragColor = u_Color * texture2D(u_Texture, tex_xy);
}

