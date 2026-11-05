package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun TempTrendChart(
    temperatures: List<Double>,
    modifier: Modifier = Modifier
) {
    if (temperatures.size < 2) return

    val minT = temperatures.minOrNull() ?: 0.0
    val maxT = temperatures.maxOrNull() ?: 1.0
    val range = (maxT - minT).coerceAtLeast(1.0)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        val width = size.width
        val height = size.height
        val stepX = width / (temperatures.size - 1)

        val points = temperatures.mapIndexed { index, temp ->
            val normY = 1f - ((temp - minT) / range).toFloat()
            val y = 10f + normY * (height - 20f)
            val x = index * stepX
            Offset(x, y)
        }

        // Draw smooth path
        val strokePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val midX = (p0.x + p1.x) / 2
                val midY = (p0.y + p1.y) / 2
                quadraticTo(p0.x, p0.y, midX, midY)
            }
            lineTo(points.last().x, points.last().y)
        }

        // Fill under path
        val fillPath = Path().apply {
            addPath(strokePath)
            lineTo(points.last().x, height)
            lineTo(points.first().x, height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF38BDF8).copy(alpha = 0.35f),
                    Color(0xFF38BDF8).copy(alpha = 0.02f)
                )
            )
        )

        drawPath(
            path = strokePath,
            color = Color(0xFF38BDF8),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw dots on notable points (first, peak, last)
        points.forEachIndexed { idx, point ->
            if (idx == 0 || idx == points.size - 1 || idx % 4 == 0) {
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = point
                )
            }
        }
    }
}
