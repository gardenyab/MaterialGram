package com.gardendev.materialgram

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gardendev.materialgram.ui.theme.MaterialGramTheme
import com.google.android.material.loadingindicator.LoadingIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.Dp

class StartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialGramTheme {
                // Вызываем наш центрированный экран
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Greeting(name: String, modifier: Modifier) {
    var progress by remember { mutableFloatStateOf(0.5f) }
    var isVisible by remember { mutableStateOf(true) }

    // Основной контейнер на весь экран
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // Центрируем содержимое Box
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, // Центрируем элементы внутри колонки по горизонтали
            verticalArrangement = Arrangement.spacedBy(24.dp) // Автоматические отступы между элементами
        ) {
            // 1. Круговой индикатор
            if (isVisible) {
                CircularWavyProgressIndicator(
                    progress = progress,
                    modifier = Modifier.size(100.dp)
                )
            }

            // 2. Линейный индикатор
            LinearWavyProgressIndicator(
                progress = progress,
                modifier = Modifier.width(300.dp) // Задаем ширину, чтобы не был на весь экран
            )

            // 3. Элементы управления
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Показать круговой:")
                Spacer(Modifier.width(8.dp))
                Switch(checked = isVisible, onCheckedChange = { isVisible = it })
            }

            Slider(
                value = progress,
                onValueChange = { progress = it },
                modifier = Modifier.width(300.dp)
            )

            Text("Прогресс: ${(progress * 100).toInt()}%")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE)
@Composable
fun GreetingPreview() {
    MaterialGramTheme {
        Greeting("Android", modifier = Modifier)
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
    speed: Float = 1.5f, // Скорость анимации
    direction: Float = -1f, // 1f - вправо, -1f - влев
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

        // Сглаживание амплитуды (0-5% и 95-100%)
        val smoothingFactor = when {
            progress < 0.05f -> progress / 0.05f
            progress > 0.95f -> (1f - progress) / 0.05f
            else -> 1f
        }
        val currentWaveHeightPx = baseWaveHeightPx * smoothingFactor
        val progressWidth = width * progress

        // 1. Рисуем ВОЛНИСТУЮ линию (Прогресс)
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

        // 2. Рисуем СЕРУЮ ЛИНИЮ (Фон)
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

        // 3. Рисуем ТОЧКУ в самом конце (на правой границе)
        // Она всегда там, пока прогресс не станет 100% (или можно оставить всегда)
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
    direction: Float = -1f // 1f - по часовой, -1f - против часовой
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

        // Коэффициент сглаживания (плавное затихание волн у краев)
        val smoothingFactor = when {
            progress < 0.05f -> progress / 0.05f
            progress > 0.95f -> (1f - progress) / 0.05f
            else -> 1f
        }
        val currentVariationPx = baseWaveVariationPx * smoothingFactor

        // 1. Волнистая активная дуга
        if (progress > 0f) {
            val path = Path()
            for (angle in 0..sweepAngle.toInt()) {
                val angleRad = Math.toRadians(angle.toDouble())

                // Применяем сглаженную вариацию и фазу
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

        // 2. Серая фоновая линия с зазорами
        if (progress < 1f) {
            val grayLineColor = color.copy(alpha = 0.2f)

            if (progress <= 0f) {
                // Идеально ровный круг, когда прогресса нет
                drawCircle(
                    color = grayLineColor,
                    radius = baseRadius,
                    style = Stroke(width = strokeWidthPx)
                )
            } else {
                // Дуга с зазорами с обеих сторон
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