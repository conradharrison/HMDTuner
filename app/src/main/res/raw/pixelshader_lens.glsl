//precision mediump float;

const int HMD_PARAMS_SIZE = 8;

uniform float u_HMDParams[HMD_PARAMS_SIZE];
uniform vec4 u_Color;
uniform sampler2D u_Texture;

varying vec3 v_Position;		// Interpolated position for this fragment. Unused
varying vec2 v_TexCoordinate;   // Interpolated texture coordinate per fragment.

vec2 mapper(vec2 in_tex,
            float screen_to_lens_distance,
            float inter_lens_distance,
            float distortion_coefficients_k1,
            float distortion_coefficients_k2,
            float viewing_angles_left,
            float viewing_angles_right,
            float viewing_angles_bottom,
            float viewing_angles_top)
{
    vec2 out_tex;

    float input_width = 1.0;
    float input_height = 1.0;
    float half_height = input_height / 2.0;
    float half_width = input_width / 2.0;

    float r0 = sqrt(input_height*input_height + input_width*input_width) / (10.0*distortion_coefficients_k1);

    float new_x  = in_tex.x - half_width;
    float new_y  = in_tex.y - half_height;

    float r = sqrt(new_x*new_x + new_y*new_y) / r0;

    float theta;

    if (r==0.0)
        theta = 1.0;
    else
        theta = atan(r) / r;

    out_tex.x = half_width + theta * new_x * 1.0;
    out_tex.y = half_height + theta * new_y * 1.0;

    return out_tex;
}

void main() {

    vec2 tex_xy;

    float screen_to_lens_distance;
    float inter_lens_distance;
    float distortion_coefficients_k1;
    float distortion_coefficients_k2;
    float viewing_angles_left;
    float viewing_angles_right;
    float viewing_angles_bottom;
    float viewing_angles_top;

    // Decode HMDParams into individual fields
    screen_to_lens_distance = u_HMDParams[0];
    inter_lens_distance = u_HMDParams[1];
    distortion_coefficients_k1 = u_HMDParams[2];
    distortion_coefficients_k2 = u_HMDParams[3];
    viewing_angles_left = u_HMDParams[4];
    viewing_angles_right = u_HMDParams[5];
    viewing_angles_bottom = u_HMDParams[6];
    viewing_angles_top = u_HMDParams[7];

    // Custom texture mapping
    tex_xy = mapper(v_TexCoordinate,
                    screen_to_lens_distance,
                    inter_lens_distance,
                    distortion_coefficients_k1,
                    distortion_coefficients_k2,
                    viewing_angles_left,
                    viewing_angles_right,
                    viewing_angles_bottom,
                    viewing_angles_top);

    gl_FragColor = u_Color * texture2D(u_Texture, tex_xy);
}

