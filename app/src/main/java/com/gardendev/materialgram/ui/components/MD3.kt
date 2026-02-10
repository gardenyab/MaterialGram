package com.gardendev.materialgram.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun IndeterminateCircularWavyProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    waveCount: Int = 15,
    waveHeight: Dp = 4.dp,
    strokeWidth: Dp = 6.dp,
    speed: Float = 1f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wavySpinner")

    // 1. Вращение всей дуги (голова индикатора)
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween((2000 / speed).toInt(), easing = LinearEasing)
        ), label = "rotation"
    )

    // 2. Длина дуги (растяжение и сжатие от 30 до 270 градусов)
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 30f,
        targetValue = 270f,
        animationSpec = infiniteRepeatable(
            animation = tween((1500 / speed).toInt(), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "sweep"
    )

    // 3. Бег волны внутри дуги
    val phaseShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween((1000 / speed).toInt(), easing = LinearEasing)
        ), label = "phase"
    )

    Canvas(modifier = modifier.size(48.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val strokeWidthPx = strokeWidth.toPx()
        val waveHeightPx = waveHeight.toPx()
        val baseRadius = (size.minDimension / 2) - waveHeightPx

        val path = Path()

        // Рисуем только часть круга (от 0 до текущего sweepAngle)
        for (angle in 0..sweepAngle.toInt() step 2) {
            val totalAngle = angle.toDouble() + rotation // Сдвигаем на общую ротацию
            val angleRad = Math.toRadians(totalAngle - 90.0) // -90 чтобы начать сверху

            // Волна зависит от угла и фазы
            val wave = Math.sin(Math.toRadians(angle.toDouble()) * (waveCount / 5.0) - phaseShift.toDouble())
            val r = baseRadius + (wave.toFloat() * waveHeightPx)

            val x = center.x + Math.cos(angleRad).toFloat() * r
            val y = center.y + Math.sin(angleRad).toFloat() * r

            if (angle == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun LinearWavyProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    waveLength: Dp = 45.dp,
    waveHeight: Dp = 3.dp,
    gap: Dp = 12.dp,
    speed: Float = 1.5f,
    direction: Float = -1f,
    dotRadius: Dp = 3.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "linearWave")
    val phaseShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat() * direction,
        animationSpec = infiniteRepeatable(
            animation = tween((1500 / speed).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "phase"
    )

    Canvas(modifier = modifier
        .fillMaxWidth()
        .height(waveHeight * 3f)
    ) {
        val width = size.width
        val height = size.height
        val strokeWidthPx = 6.dp.toPx()
        val gapPx = gap.toPx()
        val waveLenPx = waveLength.toPx()
        val baseWaveHeightPx = waveHeight.toPx()
        val dotRadiusPx = dotRadius.toPx()

        val smoothingFactor = when {
            progress < 0.05f -> progress / 0.05f
            progress > 0.95f -> (1f - progress) / 0.05f
            else -> 1f
        }
        val currentWaveHeightPx = baseWaveHeightPx * smoothingFactor
        val progressWidth = width * progress

        if (progress > 0f) {
            val path = Path()
            for (x in 0..progressWidth.toInt()) {
                val relativeX = x / waveLenPx
                val y = (height / 2) + (Math.sin((relativeX * 2 * Math.PI) - phaseShift.toDouble()).toFloat() * currentWaveHeightPx)

                if (x == 0) path.moveTo(x.toFloat(), y)
                else path.lineTo(x.toFloat(), y)
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }

        val grayLineColor = color.copy(alpha = 0.2f)
        if (progress < 1f) {
            val startX = if (progress <= 0f) 0f else progressWidth + gapPx

            if (startX < width) {
                drawLine(
                    color = grayLineColor,
                    start = Offset(startX, height / 2),
                    end = Offset(width, height / 2),
                    strokeWidth = strokeWidthPx,
                    cap = StrokeCap.Round
                )
            }
        }

        drawCircle(
            color = if (progress >= 1f) color else color,
            radius = dotRadiusPx,
            center = Offset(width, height / 2)
        )
    }
}

@Composable
fun CircularWavyProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    waveCount: Int = 12,
    waveVariation: Dp = 2.dp,
    gapDegrees: Float = 15f,
    speed: Float = 1.5f,
    direction: Float = -1f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "circularWave")
    val phaseShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat() * direction,
        animationSpec = infiniteRepeatable(
            animation = tween((2000 / speed).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "phase"
    )

    Canvas(modifier = modifier.size(100.dp).padding(8.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val strokeWidthPx = 6.dp.toPx()
        val baseWaveVariationPx = waveVariation.toPx()
        val baseRadius = (size.minDimension / 2) - baseWaveVariationPx - (strokeWidthPx / 2)

        val sweepAngle = progress * 360f

        val smoothingFactor = when {
            progress < 0.05f -> progress / 0.05f
            progress > 0.95f -> (1f - progress) / 0.05f
            else -> 1f
        }
        val currentVariationPx = baseWaveVariationPx * smoothingFactor

        if (progress > 0f) {
            val path = Path()
            for (angle in 0..sweepAngle.toInt()) {
                val angleRad = Math.toRadians(angle.toDouble())

                val wave = Math.sin(angleRad * waveCount - phaseShift.toDouble())
                val r = baseRadius + (wave.toFloat() * currentVariationPx)

                val x = center.x + Math.cos(angleRad - Math.PI / 2).toFloat() * r
                val y = center.y + Math.sin(angleRad - Math.PI / 2).toFloat() * r

                if (angle == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }

        if (progress < 1f) {
            val grayLineColor = color.copy(alpha = 0.2f)

            if (progress <= 0f) {
                drawCircle(
                    color = grayLineColor,
                    radius = baseRadius,
                    style = Stroke(width = strokeWidthPx)
                )
            } else {
                val startAngle = sweepAngle - 90f + gapDegrees
                val backgroundSweep = 360f - sweepAngle - (gapDegrees * 2)

                if (backgroundSweep > 0) {
                    drawArc(
                        color = grayLineColor,
                        startAngle = startAngle,
                        sweepAngle = backgroundSweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                        size = Size(baseRadius * 2, baseRadius * 2),
                        topLeft = Offset(center.x - baseRadius, center.y - baseRadius)
                    )
                }
            }
        }
    }
}