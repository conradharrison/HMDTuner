//precision mediump float;

const int HMD_PARAMS_SIZE = 12;

uniform float u_HMDParams[HMD_PARAMS_SIZE];
uniform sampler2D u_Texture;

uniform float u_ScreenWidth;
uniform float u_ScreenHeight;
uniform float u_interEyePixels;

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

    float new_x  = in_tex.x - half_width;
    float new_y  = in_tex.y - half_height;

    float view_port_width = u_ScreenWidth/2.0;

    float offsetted_x;
    if (gl_FragCoord.x >= u_ScreenWidth/2.0) {
        offsetted_x = gl_FragCoord.x - u_ScreenWidth/2.0 - u_interEyePixels;
    } else {
        offsetted_x = gl_FragCoord.x + u_interEyePixels;
    }

    float new_position_x = (offsetted_x - view_port_width/2.0)/view_port_width;
    float new_position_y = (gl_FragCoord.y - u_ScreenHeight/2.0)/u_ScreenHeight;

    float r2 = (new_position_x*new_position_x + new_position_y*new_position_y);
    float t1 = r2 * distortion_coefficients_k1;
    float t2 = r2 * r2 * distortion_coefficients_k2;

    out_tex.x = half_width + ((1.0 + t1 + t2) * new_x);
    out_tex.y = half_height + ((1.0 + t1 + t2) * new_y);

    return out_tex;
}

void main() {

    vec2 tex_xy_r;
    vec2 tex_xy_g;
    vec2 tex_xy_b;

    float screen_to_lens_distance;
    float inter_lens_distance;
    float distortion_coefficients_r_k1;
    float distortion_coefficients_r_k2;
    float distortion_coefficients_g_k1;
    float distortion_coefficients_g_k2;
    float distortion_coefficients_b_k1;
    float distortion_coefficients_b_k2;
    float viewing_angles_left;
    float viewing_angles_right;
    float viewing_angles_bottom;
    float viewing_angles_top;

    // Decode HMDParams into individual fields
    screen_to_lens_distance = u_HMDParams[0];
    inter_lens_distance = u_HMDParams[1];
    distortion_coefficients_r_k1 = u_HMDParams[2];
    distortion_coefficients_r_k2 = u_HMDParams[3];
    distortion_coefficients_g_k1 = u_HMDParams[4];
    distortion_coefficients_g_k2 = u_HMDParams[5];
    distortion_coefficients_b_k1 = u_HMDParams[6];
    distortion_coefficients_b_k2 = u_HMDParams[7];
    viewing_angles_left = u_HMDParams[8];
    viewing_angles_right = u_HMDParams[9];
    viewing_angles_bottom = u_HMDParams[10];
    viewing_angles_top = u_HMDParams[11];

    // Custom texture mapping
    tex_xy_r = mapper(v_TexCoordinate,
                    screen_to_lens_distance,
                    inter_lens_distance,
                    distortion_coefficients_r_k1,
                    distortion_coefficients_r_k2,
                    viewing_angles_left,
                    viewing_angles_right,
                    viewing_angles_bottom,
                    viewing_angles_top);

    tex_xy_g = mapper(v_TexCoordinate,
                         screen_to_lens_distance,
                         inter_lens_distance,
                         distortion_coefficients_g_k1,
                         distortion_coefficients_g_k2,
                         viewing_angles_left,
                         viewing_angles_right,
                         viewing_angles_bottom,
                         viewing_angles_top);

    tex_xy_b = mapper(v_TexCoordinate,
                         screen_to_lens_distance,
                         inter_lens_distance,
                         distortion_coefficients_b_k1,
                         distortion_coefficients_b_k2,
                         viewing_angles_left,
                         viewing_angles_right,
                         viewing_angles_bottom,
                         viewing_angles_top);

    gl_FragColor = vec4(texture2D(u_Texture, tex_xy_r).x, texture2D(u_Texture, tex_xy_g).y, texture2D(u_Texture, tex_xy_b).z, 1.0);

}

