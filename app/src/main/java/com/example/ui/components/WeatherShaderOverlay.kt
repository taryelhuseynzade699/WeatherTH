package com.example.ui.components

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
import com.example.data.model.WeatherCategory

const val AGSL_WEATHER_SHADER_SRC = """
uniform float2 uResolution;
uniform float uTime;
uniform float uWeatherType;

float hash(float2 p) {
    p = fract(p * float2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

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

float fbm(float2 p) {
    float v = 0.0;
    float a = 0.5;
    float2 shift = float2(100.0, 100.0);
    float2x2 rot = float2x2(0.877, 0.479, -0.479, 0.877);
    for (int i = 0; i < 4; ++i) {
        v += a * noise(p);
        p = rot * p * 2.0 + shift;
        a *= 0.5;
    }
    return v;
}

half4 renderRainOnGlass(float2 uv, float time) {
    float2 st = uv * float2(7.0, 16.0);
    st.y -= time * 2.8;

    float2 id = floor(st);
    float2 gv = fract(st) - 0.5;

    float h = hash(id);
    float dropRadius = 0.14 * (0.6 + 0.4 * sin(id.y * 3.0 + id.x));
    float trail = smoothstep(0.035, 0.0, abs(gv.x)) * smoothstep(0.5, -0.5, gv.y) * 0.18;
    float d = length(gv);
    float drop = smoothstep(dropRadius, dropRadius - 0.035, d);
    float highlight = smoothstep(dropRadius, dropRadius - 0.02, d + 0.025 * gv.y);

    float alpha = clamp(drop * 0.55 + trail + highlight * 0.35, 0.0, 0.65);
    half3 rainColor = half3(0.70, 0.90, 1.0) * (0.85 + 0.5 * highlight);
    return half4(rainColor, alpha);
}

half4 renderSunGodRays(float2 uv, float time) {
    float2 sunCenter = float2(0.80, 0.16);
    float2 delta = uv - sunCenter;
    float dist = length(delta);
    float angle = atan(delta.y, delta.x);

    float beams = sin(angle * 12.0 + time * 0.4) * 0.5 + 0.5;
    beams += sin(angle * 6.0 - time * 0.25) * 0.25;
    beams = pow(beams, 2.5);

    float glow = exp(-dist * 2.0);
    float rayIntensity = glow * (0.15 + 0.40 * beams);
    half3 sunColor = half3(1.0, 0.84, 0.45);
    return half4(sunColor, clamp(rayIntensity * 0.45, 0.0, 0.55));
}

half4 renderNightAurora(float2 uv, float time) {
    float2 p = uv * 3.0;
    float wave1 = sin(p.x * 2.0 + time * 0.35 + fbm(p + time * 0.15) * 2.0);
    float wave2 = cos(p.x * 3.0 - time * 0.25 + fbm(p * 1.5) * 1.5);
    float auroraBand = smoothstep(0.38, 0.0, abs(uv.y - 0.32 + wave1 * 0.08 + wave2 * 0.05));
    float nebula = fbm(uv * 4.0 + time * 0.04) * auroraBand;

    half3 auroraGreen = half3(0.2, 0.95, 0.75);
    half3 auroraPurple = half3(0.55, 0.35, 0.95);
    half3 color = mix(auroraGreen, auroraPurple, uv.x + sin(time * 0.25) * 0.25);
    return half4(color, clamp(nebula * 0.40, 0.0, 0.45));
}

half4 renderCloudMist(float2 uv, float time) {
    float2 p = uv * 2.2 + float2(time * 0.04, time * 0.01);
    float mist = fbm(p) * 0.30;
    mist += fbm(p * 2.0 - time * 0.02) * 0.15;
    half3 mistColor = half3(0.92, 0.96, 1.0);
    return half4(mistColor, clamp(mist * 0.30, 0.0, 0.35));
}

half4 main(float2 fragCoord) {
    float2 uv = fragCoord / uResolution;
    if (uWeatherType > 1.5 && uWeatherType < 2.5) {
        return renderRainOnGlass(uv, uTime);
    } else if (uWeatherType > 2.5) {
        return renderNightAurora(uv, uTime);
    } else if (uWeatherType < 0.5) {
        return renderSunGodRays(uv, uTime);
    } else {
        return renderCloudMist(uv, uTime);
    }
}
"""

@Composable
fun WeatherShaderOverlay(
    category: WeatherCategory,
    isDay: Boolean,
    modifier: Modifier = Modifier
) {
    // Weather type: 0 = SUNNY, 1 = CLOUDY, 2 = RAIN, 3 = NIGHT
    val weatherType = when {
        !isDay -> 3.0f
        category == WeatherCategory.RAIN || category == WeatherCategory.DRIZZLE || category == WeatherCategory.THUNDERSTORM -> 2.0f
        category == WeatherCategory.CLEAR -> 0.0f
        else -> 1.0f
    }

    val infiniteTransition = rememberInfiniteTransition(label = "shader_timer")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 62.83f, // 10 * 2*pi
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        AgslRuntimeShaderView(
            weatherType = weatherType,
            time = time,
            modifier = modifier
        )
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun AgslRuntimeShaderView(
    weatherType: Float,
    time: Float,
    modifier: Modifier = Modifier
) {
    val runtimeShader = remember {
        try {
            RuntimeShader(AGSL_WEATHER_SHADER_SRC)
        } catch (e: Exception) {
            null
        }
    } ?: return

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (width > 0 && height > 0) {
            runtimeShader.setFloatUniform("uResolution", width, height)
            runtimeShader.setFloatUniform("uTime", time)
            runtimeShader.setFloatUniform("uWeatherType", weatherType)

            drawRect(
                brush = ShaderBrush(runtimeShader),
                size = size
            )
        }
    }
}
