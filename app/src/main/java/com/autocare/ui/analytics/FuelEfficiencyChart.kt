package com.autocare.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.autocare.ui.dashboard.FuelEfficiencyPoint
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.fullWidth
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore

@Composable
fun FuelEfficiencyChart(
    points: List<FuelEfficiencyPoint>,
    modifier: Modifier = Modifier
) {
    if (points.size < 2) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Se necesitan al menos 2 cargas de combustible para mostrar tendencias.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }

    val modelProducer = remember { CartesianChartModelProducer.build() }
    val labelKey = remember { ExtraStore.Key<List<String>>() }
    val labels = points.map { it.dateLabel }
    
    LaunchedEffect(points) {
        modelProducer.tryRunTransaction {
            lineSeries {
                series(points.map { it.kmPerLiter })
            }
            updateExtras { it[labelKey] = labels }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(),
            startAxis = rememberStartAxis(
                label = rememberTextComponent(
                    color = MaterialTheme.colorScheme.onSurface,
                    textSize = 10.sp
                )
            ),
            bottomAxis = rememberBottomAxis(
                label = rememberTextComponent(
                    color = MaterialTheme.colorScheme.onSurface,
                    textSize = 10.sp
                ),
                valueFormatter = { value, _, _ ->
                    val index = value.toInt()
                    if (index % 2 == 0) labels.getOrNull(index) ?: "" else ""
                }
            ),
        ),
        modelProducer = modelProducer,
        scrollState = rememberVicoScrollState(scrollEnabled = true),
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun FuelEfficiencyChartPreview() {
    MaterialTheme {
        FuelEfficiencyChart(
            points = listOf(
                FuelEfficiencyPoint("01 Jan", 12.5, 1000L),
                FuelEfficiencyPoint("15 Jan", 13.2, 2000L),
                FuelEfficiencyPoint("01 Feb", 12.8, 3000L),
                FuelEfficiencyPoint("15 Feb", 14.1, 4000L)
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
