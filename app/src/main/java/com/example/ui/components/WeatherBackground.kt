package com.example.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.WeatherCategory
import kotlin.random.Random

@Composable
fun WeatherBackground(
    category: WeatherCategory,
    isDay: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // 3D Camera breathing / subtle parallax float animation
    val infiniteTransition = rememberInfiniteTransition(label = "3d_camera")
    val cameraScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "camera_scale"
    )

    // Determine 3D asset resource
    val backgroundRes = when {
        !isDay -> R.drawable.bg_weather_night
        category == WeatherCategory.RAIN || category == WeatherCategory.DRIZZLE || category == WeatherCategory.THUNDERSTORM -> R.drawable.bg_weather_rain
        category == WeatherCategory.CLEAR -> R.drawable.bg_weather_sunny
        else -> R.drawable.bg_weather_cloudy
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Base 3D Photorealistic Landscape/Sky with Camera Parallax
        Crossfade(
            targetState = backgroundRes,
            animationSpec = tween(1000),
            modifier = Modifier.fillMaxSize(),
            label = "3d_bg_crossfade"
        ) { resId ->
            Image(
                painter = painterResource(id = resId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(cameraScale)
            )
        }

        // Real-Time GPU Fragment Shader Effect Layer (.frag / AGSL)
        WeatherShaderOverlay(
            category = category,
            isDay = isDay,
            modifier = Modifier.fillMaxSize()
        )

        // Live 3D Particle & Atmosphere Layer
        when {
            category == WeatherCategory.RAIN || category == WeatherCategory.DRIZZLE || category == WeatherCategory.THUNDERSTORM -> {
                Animated3DRainOverlay(isThunder = category == WeatherCategory.THUNDERSTORM)
            }
            !isDay -> {
                Animated3DNightStarsOverlay()
            }
            category == WeatherCategory.CLEAR -> {
                Animated3DSunlightParticlesOverlay()
            }
            else -> {
                Animated3DCloudDriftOverlay()
            }
        }

        // Cinematic 3D Depth Vignette & Darkening Scrim (ensures contrast & readibility for UI cards)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.20f),
                            Color(0xFF0B132B).copy(alpha = 0.70f),
                            Color(0xFF0B132B).copy(alpha = 0.92f)
                        )
                    )
                )
        )

        // Weather Content Overlaid in 3D Space
        content()
    }
}

/**
 * 3D Falling Rain effect with perspective, depth, and lightning flash for thunderstorms.
 */
@Composable
private fun Animated3DRainOverlay(isThunder: Boolean) {
    val rainTransition = rememberInfiniteTransition(label = "rain_transition")
    val rainProgress by rainTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rain_progress"
    )

    val thunderAlpha by rainTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isThunder) 0.35f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "thunder_alpha"
    )

    // Pre-calculate pseudo-random rain streaks with varied 3D depth/speeds
    val rainDrops = remember {
        val rand = Random(42)
        List(65) {
            RainDropData(
                startXPercent = rand.nextFloat(),
                startYPercent = rand.nextFloat(),
                length = rand.nextFloat() * 35f + 25f,
                speedMultiplier = rand.nextFloat() * 0.7f + 0.8f,
                depthAlpha = rand.nextFloat() * 0.45f + 0.25f,
                strokeWidth = rand.nextFloat() * 1.5f + 1f
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isThunder && thunderAlpha > 0.1f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF818CF8).copy(alpha = thunderAlpha))
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            rainDrops.forEach { drop ->
                val progress = (rainProgress * drop.speedMultiplier + drop.startYPercent) % 1.0f
                val currentY = progress * (canvasH + 100f) - 50f
                val currentX = drop.startXPercent * canvasW + (progress * 40f) // slight wind slant

                drawLine(
                    color = Color(0xFFA5F3FC).copy(alpha = drop.depthAlpha),
                    start = Offset(currentX, currentY),
                    end = Offset(currentX + 8f, currentY + drop.length),
                    strokeWidth = drop.strokeWidth.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

private data class RainDropData(
    val startXPercent: Float,
    val startYPercent: Float,
    val length: Float,
    val speedMultiplier: Float,
    val depthAlpha: Float,
    val strokeWidth: Float
)

/**
 * 3D Night Stars with shimmering depths.
 */
@Composable
private fun Animated3DNightStarsOverlay() {
    val starTransition = rememberInfiniteTransition(label = "star_twinkle")
    val twinklePulse by starTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle_pulse"
    )

    val stars = remember {
        val rand = Random(108)
        List(40) {
            StarData(
                xPercent = rand.nextFloat(),
                yPercent = rand.nextFloat() * 0.55f, // upper half of screen
                radius = rand.nextFloat() * 2.2f + 1.0f,
                baseAlpha = rand.nextFloat() * 0.5f + 0.3f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasW = size.width
        val canvasH = size.height

        stars.forEachIndexed { i, star ->
            val alpha = (star.baseAlpha * if (i % 2 == 0) twinklePulse else (1.3f - twinklePulse)).coerceIn(0.1f, 0.95f)
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = star.radius.dp.toPx(),
                center = Offset(star.xPercent * canvasW, star.yPercent * canvasH)
            )
        }
    }
}

private data class StarData(
    val xPercent: Float,
    val yPercent: Float,
    val radius: Float,
    val baseAlpha: Float
)

/**
 * 3D Golden Sunlight Volumetric Particles.
 */
@Composable
private fun Animated3DSunlightParticlesOverlay() {
    val sunTransition = rememberInfiniteTransition(label = "sun_particles")
    val sunPulse by sunTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sun_drift"
    )

    val orbs = remember {
        val rand = Random(77)
        List(25) {
            OrbData(
                startXPercent = rand.nextFloat(),
                startYPercent = rand.nextFloat() * 0.65f,
                size = rand.nextFloat() * 12f + 4f,
                alpha = rand.nextFloat() * 0.25f + 0.1f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasW = size.width
        val canvasH = size.height

        orbs.forEach { orb ->
            val progress = (sunPulse + orb.startYPercent) % 1f
            val y = progress * canvasH * 0.7f
            val x = orb.startXPercent * canvasW + kotlin.math.sin(progress * 6.28) * 20f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFDE68A).copy(alpha = orb.alpha),
                        Color(0xFFF59E0B).copy(alpha = orb.alpha * 0.4f),
                        Color.Transparent
                    ),
                    center = Offset(x.toFloat(), y.toFloat()),
                    radius = orb.size.dp.toPx()
                ),
                radius = orb.size.dp.toPx(),
                center = Offset(x.toFloat(), y.toFloat())
            )
        }
    }
}

private data class OrbData(
    val startXPercent: Float,
    val startYPercent: Float,
    val size: Float,
    val alpha: Float
)

/**
 * 3D Ambient Cloud Drift Overlay.
 */
@Composable
private fun Animated3DCloudDriftOverlay() {
    val cloudTransition = rememberInfiniteTransition(label = "cloud_drift")
    val driftOffset by cloudTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drift_offset"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasW = size.width
        val canvasH = size.height

        val xShift = driftOffset * canvasW
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = Offset(xShift % (canvasW * 1.5f), canvasH * 0.25f),
                radius = canvasW * 0.7f
            ),
            radius = canvasW * 0.7f,
            center = Offset(xShift % (canvasW * 1.5f), canvasH * 0.25f)
        )
    }
}
