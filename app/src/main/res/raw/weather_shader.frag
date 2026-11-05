// WeatherTH - GPU Fragment Shader (AGSL / GLSL)
// Real-time procedural weather atmosphere and rain on glass simulation
// Runs on the mobile device GPU per-pixel at 60/120 FPS

uniform float2 uResolution;
uniform float uTime;
uniform float uWeatherType; // 0.0 = CLEAR/SUNNY, 1.0 = CLOUDY, 2.0 = RAIN, 3.0 = NIGHT

// Pseudo-random hash function
float hash(float2 p) {
    p = fract(p * float2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

// 2D Noise function for fluid atmospheric motion
float noise(float2 p) {
    float2 i = floor(p);
    float2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);

    float a = hash(i);
    float b = hash(i + float2(1.0, 0.0));
    float c = hash(i + float2(0.0, 1.0));
    float d = hash(i + float2(1.0, 1.0));

    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

// Fractal Brownian Motion (FBM) for realistic volumetric cloud & mist density
float fbm(float2 p) {
    float v = 0.0;
    float a = 0.5;
    float2 shift = float2(100.0, 100.0);
    // Rotate to reduce axial bias
    float2x2 rot = float2x2(cos(0.5), sin(0.5), -sin(0.5), cos(0.5));
    for (int i = 0; i < 4; ++i) {
        v += a * noise(p);
        p = rot * p * 2.0 + shift;
        a *= 0.5;
    }
    return v;
}

// Procedural Rain on Glass calculation with liquid droplet refraction
half4 renderRainOnGlass(float2 uv, float time) {
    float2 st = uv * float2(6.0, 14.0);
    st.y -= time * 2.5;

    float2 id = floor(st);
    float2 gv = fract(st) - 0.5;

    float h = hash(id);
    float dropRadius = 0.12 * (0.6 + 0.4 * sin(id.y * 3.0 + id.x));
    
    // Trail calculation
    float trail = smoothstep(0.04, 0.0, abs(gv.x)) * smoothstep(0.5, -0.5, gv.y) * 0.15;
    
    // Droplet shape with normal highlight
    float d = length(gv);
    float drop = smoothstep(dropRadius, dropRadius - 0.04, d);

    // Liquid refraction specular edge
    float highlight = smoothstep(dropRadius, dropRadius - 0.02, d + 0.02 * gv.y);

    float alpha = clamp(drop * 0.45 + trail + highlight * 0.25, 0.0, 0.6);
    half3 rainColor = half3(0.65, 0.88, 1.0) * (0.8 + 0.4 * highlight);
    return half4(rainColor, alpha);
}

// Procedural Volumetric Sun Rays & Golden Atmosphere
half4 renderSunGodRays(float2 uv, float time) {
    float2 sunCenter = float2(0.78, 0.18);
    float2 delta = uv - sunCenter;
    float dist = length(delta);
    float angle = atan(delta.y, delta.x);

    // Radiant angular beams
    float beams = sin(angle * 14.0 + time * 0.5) * 0.5 + 0.5;
    beams += sin(angle * 7.0 - time * 0.3) * 0.25;
    beams = pow(beams, 3.0);

    // Falloff from sun center
    float glow = exp(-dist * 2.2);
    float rayIntensity = glow * (0.2 + 0.45 * beams);

    half3 sunColor = half3(1.0, 0.82, 0.45);
    return half4(sunColor, clamp(rayIntensity * 0.4, 0.0, 0.5));
}

// Procedural Night Aurora & Twinkling Celestial Nebula
half4 renderNightAurora(float2 uv, float time) {
    float2 p = uv * 3.0;
    float wave1 = sin(p.x * 2.0 + time * 0.4 + fbm(p + time * 0.2) * 2.0);
    float wave2 = cos(p.x * 3.0 - time * 0.3 + fbm(p * 1.5) * 1.5);
    
    float auroraBand = smoothstep(0.4, 0.0, abs(uv.y - 0.35 + wave1 * 0.08 + wave2 * 0.05));
    float nebula = fbm(uv * 4.0 + time * 0.05) * auroraBand;

    half3 auroraGreen = half3(0.2, 0.9, 0.7);
    half3 auroraPurple = half3(0.5, 0.3, 0.9);
    half3 color = mix(auroraGreen, auroraPurple, uv.x + sin(time * 0.3) * 0.2);

    return half4(color, clamp(nebula * 0.35, 0.0, 0.4));
}

// Procedural Volumetric Mist for Cloudy Weather
half4 renderCloudMist(float2 uv, float time) {
    float2 p = uv * 2.5 + float2(time * 0.05, time * 0.01);
    float mist = fbm(p) * 0.35;
    mist += fbm(p * 2.0 - time * 0.03) * 0.15;
    half3 mistColor = half3(0.9, 0.95, 1.0);
    return half4(mistColor, clamp(mist * 0.25, 0.0, 0.3));
}

// Shader entry point
half4 main(float2 fragCoord) {
    float2 uv = fragCoord / uResolution;

    half4 finalColor = half4(0.0, 0.0, 0.0, 0.0);

    if (uWeatherType > 1.5 && uWeatherType < 2.5) {
        // Rain / Storm
        finalColor = renderRainOnGlass(uv, uTime);
    } else if (uWeatherType > 2.5) {
        // Night
        finalColor = renderNightAurora(uv, uTime);
    } else if (uWeatherType < 0.5) {
        // Clear Sunny
        finalColor = renderSunGodRays(uv, uTime);
    } else {
        // Cloudy
        finalColor = renderCloudMist(uv, uTime);
    }

    return finalColor;
}
