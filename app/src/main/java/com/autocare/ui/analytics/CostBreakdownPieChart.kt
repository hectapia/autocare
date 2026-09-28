package com.autocare.ui.analytics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autocare.ui.dashboard.CostBreakdownState

import java.util.Locale

@Composable
fun CostBreakdownPieChart(
    breakdown: CostBreakdownState,
    modifier: Modifier = Modifier
) {
    val total = breakdown.totalAmount
    if (total <= 0.0) {
        Box(
            modifier = modifier.fillMaxWidth().height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hay datos de gastos para mostrar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val fuelAngle = (breakdown.fuelTotal / total * 360f).toFloat()
    val maintAngle = (breakdown.maintenanceTotal / total * 360f).toFloat()
    val expAngle = (breakdown.expenseTotal / total * 360f).toFloat()

    val colors = listOf(
        MaterialTheme.colorScheme.primary,        // Fuel: Main action/brand color
        MaterialTheme.colorScheme.error,          // Maintenance: Needs attention/higher cost usually
        MaterialTheme.colorScheme.tertiary        // Expenses: Distinct accent color
    )

    val isInspectionMode = LocalInspectionMode.current
    val animationProgress = remember { Animatable(if (isInspectionMode) 1f else 0f) }
    LaunchedEffect(breakdown) {
        if (!isInspectionMode) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000)
            )
        }
    }

    Column(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Canvas(modifier = Modifier.size(140.dp)) {
                var startAngle = -90f
                val strokeWidth = 20.dp.toPx()

                // Fuel Arc
                if (fuelAngle > 0) {
                    drawArc(
                        color = colors[0],
                        startAngle = startAngle,
                        sweepAngle = fuelAngle * animationProgress.value,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                    startAngle += fuelAngle
                }

                // Maintenance Arc
                if (maintAngle > 0) {
                    drawArc(
                        color = colors[1],
                        startAngle = startAngle,
                        sweepAngle = maintAngle * animationProgress.value,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                    startAngle += maintAngle
                }

                // Expenses Arc
                if (expAngle > 0) {
                    drawArc(
                        color = colors[2],
                        startAngle = startAngle,
                        sweepAngle = expAngle * animationProgress.value,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LegendItem(
                    label = "Combustible",
                    amount = breakdown.fuelTotal,
                    percentage = (breakdown.fuelTotal / total * 100),
                    color = colors[0]
                )
                LegendItem(
                    label = "Mantenimiento",
                    amount = breakdown.maintenanceTotal,
                    percentage = (breakdown.maintenanceTotal / total * 100),
                    color = colors[1]
                )
                LegendItem(
                    label = "Gastos Varios",
                    amount = breakdown.expenseTotal,
                    percentage = (breakdown.expenseTotal / total * 100),
                    color = colors[2]
                )
            }
        }
    }
}

@Composable
fun LegendItem(
    label: String,
    amount: Double,
    percentage: Double,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Canvas(modifier = Modifier.size(12.dp)) {
            drawCircle(color = color)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$${String.format(Locale.US, "%,.2f", amount)} (${String.format(Locale.US, "%.1f", percentage)}%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun CostBreakdownPieChartPreview() {
    MaterialTheme {
        CostBreakdownPieChart(
            breakdown = CostBreakdownState(
                fuelTotal = 150.0,
                maintenanceTotal = 80.0,
                expenseTotal = 20.0,
                totalAmount = 250.0
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
