package com.autocare.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.LayerDrawable
import android.graphics.pdf.PdfDocument
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.autocare.R
import com.autocare.data.entity.AlertSeverity
import com.autocare.data.entity.Expense
import com.autocare.data.entity.FuelLoad
import com.autocare.data.entity.Maintenance
import com.autocare.data.entity.Reminder
import com.autocare.data.entity.Vehicle
import com.autocare.data.entity.calculateSeverity
import com.autocare.ui.dashboard.CostBreakdownState
import com.autocare.ui.dashboard.FuelEfficiencyPoint
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class VehicleReportData(
    val vehicle: Vehicle,
    val fuelLoads: List<FuelLoad>,
    val maintenances: List<Maintenance>,
    val expenses: List<Expense>,
    val reminders: List<Reminder> = emptyList(),
    val fuelEfficiencyPoints: List<FuelEfficiencyPoint> = emptyList(),
    val costBreakdown: CostBreakdownState = CostBreakdownState(0.0, 0.0, 0.0, 0.0)
)

object ReportUtils {

    private fun calculateFuelEfficiencyPoints(fuelLoads: List<FuelLoad>): List<FuelEfficiencyPoint> {
        val sorted = fuelLoads.sortedBy { it.odometer }
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        val points = mutableListOf<FuelEfficiencyPoint>()
        for (i in 1 until sorted.size) {
            val prev = sorted[i - 1]
            val curr = sorted[i]
            val distance = curr.odometer - prev.odometer
            if (curr.liters > 0) {
                val efficiency = distance / curr.liters
                points.add(
                    FuelEfficiencyPoint(
                        dateLabel = dateFormat.format(Date(curr.date)),
                        kmPerLiter = efficiency,
                        timestamp = curr.date
                    )
                )
            }
        }
        return points
    }

    private fun calculateCostBreakdown(
        fuelLoads: List<FuelLoad>,
        maintenances: List<Maintenance>,
        expenses: List<Expense>
    ): CostBreakdownState {
        val fuelTotal = fuelLoads.sumOf { it.totalCost }
        val maintTotal = maintenances.sumOf { it.cost }
        val expTotal = expenses.sumOf { it.amount }
        return CostBreakdownState(
            fuelTotal = fuelTotal,
            maintenanceTotal = maintTotal,
            expenseTotal = expTotal,
            totalAmount = fuelTotal + maintTotal + expTotal
        )
    }

    fun generateVehicleReport(
        context: Context,
        vehicle: Vehicle,
        fuelLoads: List<FuelLoad>,
        maintenances: List<Maintenance>,
        expenses: List<Expense>,
        reminders: List<Reminder> = emptyList()
    ): File? {
        val efficiencyPoints = calculateFuelEfficiencyPoints(fuelLoads)
        val costBreakdown = calculateCostBreakdown(fuelLoads, maintenances, expenses)
        val data = VehicleReportData(
            vehicle = vehicle,
            fuelLoads = fuelLoads,
            maintenances = maintenances,
            expenses = expenses,
            reminders = reminders,
            fuelEfficiencyPoints = efficiencyPoints,
            costBreakdown = costBreakdown
        )
        return generateAllVehiclesReport(context, listOf(data), isSingleVehicle = true)
    }

    fun generateAllVehiclesReport(
        context: Context,
        reportDataList: List<VehicleReportData>,
        isSingleVehicle: Boolean = false
    ): File? {
        val pdfDocument = PdfDocument()

        val titlePaint = Paint().apply {
            textSize = 18f
            isFakeBoldText = true
            color = Color.BLACK
        }
        val headerPaint = Paint().apply {
            textSize = 14f
            isFakeBoldText = true
            color = Color.rgb(71, 212, 90)
        }
        val textPaint = Paint().apply {
            textSize = 11f
            color = Color.BLACK
        }
        val subTextPaint = Paint().apply {
            textSize = 10f
            color = Color.DKGRAY
        }
        val linePaint = Paint().apply {
            strokeWidth = 1f
            color = Color.LTGRAY
        }

        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        var y = 45f

        // Draw Header Logo (PNG / Vector directly on PDF Canvas to avoid distortion)
        val logoDrawable = try {
            ContextCompat.getDrawable(context, R.drawable.ic_app_icon_content)
                ?: ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground_image)
        } catch (_: Exception) {
            null
        }

        val logoSize = 36f
        val logoX = 50f
        val logoY = y - 3f

        if (logoDrawable != null) {
            if (logoDrawable is LayerDrawable) {
                for (i in 0 until logoDrawable.numberOfLayers) {
                    logoDrawable.getDrawable(i).setBounds(0, 0, logoSize.toInt(), logoSize.toInt())
                }
            }
            logoDrawable.setBounds(0, 0, logoSize.toInt(), logoSize.toInt())
            canvas.save()
            canvas.translate(logoX, logoY)
            logoDrawable.draw(canvas)
            canvas.restore()
        }

        val textStartX = if (logoDrawable != null) logoX + logoSize + 15f else 50f
        val reportTitle = if (isSingleVehicle && reportDataList.isNotEmpty()) {
            "AutoCare - Reporte de Vehículo"
        } else {
            "AutoCare - Reporte de Flotilla Completa"
        }

        // Draw Title and Subtitle sequentially to avoid overlapping
        canvas.drawText(reportTitle, textStartX, y + 10f, titlePaint)
        val now = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Generado el: $now", textStartX, y + 26f, subTextPaint)

        y += 45f
        canvas.drawLine(50f, y, 545f, y, linePaint)
        y += 20f

        // Overall Summary for Multi-vehicle report
        if (!isSingleVehicle && reportDataList.size > 1) {
            canvas.drawText("Resumen de flotilla (${reportDataList.size} Vehículos)", 50f, y, headerPaint)
            y += 20f

            val fleetFuel = reportDataList.sumOf { it.costBreakdown.fuelTotal }
            val fleetMaint = reportDataList.sumOf { it.costBreakdown.maintenanceTotal }
            val fleetExp = reportDataList.sumOf { it.costBreakdown.expenseTotal }
            val fleetTotal = fleetFuel + fleetMaint + fleetExp

            canvas.drawRect(50f, y, 545f, y + 50f, Paint().apply { color = Color.rgb(240, 244, 248); style = Paint.Style.FILL })
            y += 20f
            canvas.drawText("Gasto Total Acumulado Flotilla: $${String.format(Locale.getDefault(), "%,.2f", fleetTotal)}", 65f, y, Paint(textPaint).apply { isFakeBoldText = true })
            y += 18f
            canvas.drawText("Combustible: $${String.format(Locale.getDefault(), "%,.2f", fleetFuel)} | Mantenimiento: $${String.format(Locale.getDefault(), "%,.2f", fleetMaint)} | Gastos: $${String.format(Locale.getDefault(), "%,.2f", fleetExp)}", 65f, y, subTextPaint)
            y += 30f
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Iterate through vehicles
        reportDataList.forEachIndexed { index, data ->
            val vehicle = data.vehicle
            val fuelLoads = data.fuelLoads
            val maintenances = data.maintenances
            val expenses = data.expenses
            val reminders = data.reminders
            val points = data.fuelEfficiencyPoints.ifEmpty { calculateFuelEfficiencyPoints(fuelLoads) }
            val breakdown = if (data.costBreakdown.totalAmount > 0) data.costBreakdown else calculateCostBreakdown(fuelLoads, maintenances, expenses)

            // Page split check before starting vehicle section
            if (y > 650f) {
                pdfDocument.finishPage(page)
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
            }

            // Vehicle Header
            canvas.drawText("Vehículo ${index + 1}: ${vehicle.brand.uppercase()} ${vehicle.model.uppercase()} (${vehicle.year})", 50f, y, headerPaint)
            y += 20f

            // Specs Card
            canvas.drawRect(50f, y, 545f, y + 50f, Paint().apply { color = Color.rgb(245, 245, 245); style = Paint.Style.FILL })
            y += 18f
            canvas.drawText("Placa: ${vehicle.plate}   |   Kilometraje Actual: ${String.format(Locale.getDefault(), "%,d", vehicle.currentKm)} km", 65f, y, textPaint)

            val liters = fuelLoads.sumOf { it.liters }
            val avgConsumption = if (liters > 0) (fuelLoads.maxOfOrNull { it.odometer }?.minus(fuelLoads.minOfOrNull { it.odometer } ?: 0) ?: 0).toDouble() / liters else 0.0
            y += 18f
            canvas.drawText("Gasto Acumulado: $${String.format(Locale.getDefault(), "%,.2f", breakdown.totalAmount)}   |   Eficiencia: ${String.format(Locale.getDefault(), "%,.2f", avgConsumption)} km/L", 65f, y, subTextPaint)

            y += 30f

            // Check space for Charts
            if (y > 550f) {
                pdfDocument.finishPage(page)
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
            }

            // Draw Charts (Side by side)
            drawCostBreakdownChart(canvas, 50f, y, 230f, 120f, breakdown, textPaint, subTextPaint)
            drawFuelEfficiencyChart(canvas, 295f, y, 250f, 120f, points, textPaint, subTextPaint, linePaint)

            y += 140f

            // Reminders Section (if any active reminders exist)
            val activeReminders = reminders.filter { !it.isCompleted }
            if (activeReminders.isNotEmpty()) {
                if (y > 700f) {
                    pdfDocument.finishPage(page)
                    pageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    y = 50f
                }

                canvas.drawText("Recordatorios Pendientes (${activeReminders.size})", 50f, y, Paint(textPaint).apply { isFakeBoldText = true })
                y += 15f
                canvas.drawLine(50f, y, 545f, y, Paint(linePaint).apply { strokeWidth = 1.5f; color = Color.BLACK })
                y += 15f

                activeReminders.forEach { reminder ->
                    if (y > 780f) {
                        pdfDocument.finishPage(page)
                        pageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        y = 50f
                        canvas.drawLine(50f, y, 545f, y, Paint(linePaint).apply { strokeWidth = 1.5f; color = Color.BLACK })
                        y += 15f
                    }

                    val targetInfo = when {
                        reminder.targetKm != null -> "Meta: ${String.format(Locale.getDefault(), "%,d", reminder.targetKm)} km"
                        reminder.targetDate != null -> "Fecha: ${dateFormat.format(Date(reminder.targetDate))}"
                        else -> "Programado"
                    }

                    val severity = reminder.calculateSeverity(vehicle.currentKm)
                    val statusText = when (severity) {
                        AlertSeverity.CRITICAL -> "[Crítico]"
                        AlertSeverity.WARNING -> "[Pendiente]"
                        else -> "[Programado]"
                    }

                    val statusColor = when (severity) {
                        AlertSeverity.CRITICAL -> Color.rgb(200, 0, 0)
                        AlertSeverity.WARNING -> Color.rgb(200, 100, 0)
                        else -> Color.rgb(0, 100, 200)
                    }

                    canvas.drawText(reminder.title, 50f, y, Paint(textPaint).apply { isFakeBoldText = true })
                    canvas.drawText(statusText, 330f, y, Paint(textPaint).apply { isFakeBoldText = true; color = statusColor })
                    canvas.drawText(targetInfo, 430f, y, subTextPaint)
                    y += 14f

                    if (reminder.description.isNotBlank()) {
                        canvas.drawText(reminder.description, 50f, y, subTextPaint)
                        y += 12f
                    }
                    canvas.drawLine(50f, y, 545f, y, linePaint)
                    y += 15f
                }
                y += 10f
            }

            // Maintenance Log
            canvas.drawText("Historial de Mantenimiento (${maintenances.size})", 50f, y, Paint(textPaint).apply { isFakeBoldText = true })
            y += 15f
            canvas.drawLine(50f, y, 545f, y, Paint(linePaint).apply { strokeWidth = 1.5f; color = Color.BLACK })
            y += 15f

            if (maintenances.isEmpty()) {
                canvas.drawText("Sin mantenimientos registrados.", 50f, y, subTextPaint)
                y += 20f
            } else {
                maintenances.sortedByDescending { it.date }.forEach { maint ->
                    if (y > 780f) {
                        pdfDocument.finishPage(page)
                        pageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        y = 50f
                        canvas.drawLine(50f, y, 545f, y, Paint(linePaint).apply { strokeWidth = 1.5f; color = Color.BLACK })
                        y += 15f
                    }

                    val dateStr = dateFormat.format(Date(maint.date))
                    canvas.drawText(dateStr, 50f, y, textPaint)
                    canvas.drawText(maint.type, 140f, y, Paint(textPaint).apply { isFakeBoldText = true })
                    canvas.drawText("$${String.format(Locale.getDefault(), "%,.2f", maint.cost)}", 450f, y, textPaint)
                    y += 14f
                    val notes = if (maint.notes.isBlank()) "Sin notas" else maint.notes
                    canvas.drawText("${String.format(Locale.getDefault(), "%,d", maint.km)} km - $notes", 140f, y, subTextPaint)
                    y += 8f
                    canvas.drawLine(50f, y, 545f, y, linePaint)
                    y += 15f
                }
            }

            y += 25f
            canvas.drawLine(50f, y, 545f, y, Paint(linePaint).apply { strokeWidth = 1f; color = Color.GRAY })
            y += 20f
        }

        pdfDocument.finishPage(page)

        val prefix = if (isSingleVehicle) "Reporte" else "Reporte_Flotilla"
        val file = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            pdfDocument.close()
        }
        return file
    }

    private fun drawCostBreakdownChart(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        breakdown: CostBreakdownState,
        textPaint: Paint,
        subTextPaint: Paint
    ) {
        val borderPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = Color.LTGRAY
        }
        val titlePaint = Paint(textPaint).apply {
            textSize = 10f
            isFakeBoldText = true
        }

        // Card Container
        canvas.drawRoundRect(RectF(x, y, x + width, y + height), 6f, 6f, borderPaint)
        canvas.drawText("Desglose de Gastos", x + 10f, y + 18f, titlePaint)

        val total = breakdown.totalAmount
        if (total <= 0) {
            canvas.drawText("Sin gastos registrados", x + 10f, y + 55f, subTextPaint)
            return
        }

        val fuelPct = (breakdown.fuelTotal / total * 100).toFloat()
        val maintPct = (breakdown.maintenanceTotal / total * 100).toFloat()
        val expPct = (breakdown.expenseTotal / total * 100).toFloat()

        // Stacked Bar
        val barTop = y + 30f
        val barHeight = 16f
        val barWidth = width - 20f
        var currentX = x + 10f

        val fuelPaint = Paint().apply { color = Color.rgb(33, 150, 243); style = Paint.Style.FILL }
        val maintPaint = Paint().apply { color = Color.rgb(244, 67, 54); style = Paint.Style.FILL }
        val expPaint = Paint().apply { color = Color.rgb(76, 175, 80); style = Paint.Style.FILL }

        if (fuelPct > 0) {
            val w = barWidth * (fuelPct / 100f)
            canvas.drawRect(currentX, barTop, currentX + w, barTop + barHeight, fuelPaint)
            currentX += w
        }
        if (maintPct > 0) {
            val w = barWidth * (maintPct / 100f)
            canvas.drawRect(currentX, barTop, currentX + w, barTop + barHeight, maintPaint)
            currentX += w
        }
        if (expPct > 0) {
            val w = barWidth * (expPct / 100f)
            canvas.drawRect(currentX, barTop, currentX + w, barTop + barHeight, expPaint)
        }

        // Legend Items with Thousands Separators
        var legendY = y + 62f
        val legendItemPaint = Paint().apply { textSize = 8.5f; color = Color.DKGRAY }

        // Fuel Legend
        canvas.drawCircle(x + 15f, legendY - 3f, 4f, fuelPaint)
        canvas.drawText("Gas: $${String.format(Locale.getDefault(), "%,.2f", breakdown.fuelTotal)} (${String.format(Locale.getDefault(), "%.0f", fuelPct)}%)", x + 25f, legendY, legendItemPaint)
        legendY += 16f

        // Maint Legend
        canvas.drawCircle(x + 15f, legendY - 3f, 4f, maintPaint)
        canvas.drawText("Mantenimiento: $${String.format(Locale.getDefault(), "%,.2f", breakdown.maintenanceTotal)} (${String.format(Locale.getDefault(), "%.0f", maintPct)}%)", x + 25f, legendY, legendItemPaint)
        legendY += 16f

        // Expense Legend
        canvas.drawCircle(x + 15f, legendY - 3f, 4f, expPaint)
        canvas.drawText("Otros: $${String.format(Locale.getDefault(), "%,.2f", breakdown.expenseTotal)} (${String.format(Locale.getDefault(), "%.0f", expPct)}%)", x + 25f, legendY, legendItemPaint)
    }

    private fun drawFuelEfficiencyChart(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        points: List<FuelEfficiencyPoint>,
        textPaint: Paint,
        subTextPaint: Paint,
        linePaint: Paint
    ) {
        val borderPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = Color.LTGRAY
        }
        val titlePaint = Paint(textPaint).apply {
            textSize = 10f
            isFakeBoldText = true
        }

        // Card Container
        canvas.drawRoundRect(RectF(x, y, x + width, y + height), 6f, 6f, borderPaint)
        canvas.drawText("Tendencia Rendimiento (km/L)", x + 10f, y + 18f, titlePaint)

        if (points.size < 2) {
            canvas.drawText("Se requieren al menos 2 cargas", x + 10f, y + 55f, subTextPaint)
            canvas.drawText("para calcular la tendencia.", x + 10f, y + 70f, subTextPaint)
            return
        }

        val plotLeft = x + 30f
        val plotRight = x + width - 15f
        val plotTop = y + 30f
        val plotBottom = y + height - 25f

        val plotWidth = plotRight - plotLeft
        val plotHeight = plotBottom - plotTop

        val minVal = (points.minOf { it.kmPerLiter } * 0.9).coerceAtLeast(0.0)
        val maxVal = points.maxOf { it.kmPerLiter } * 1.1
        val valRange = if (maxVal > minVal) maxVal - minVal else 1.0

        // Axis
        val axisPaint = Paint().apply { color = Color.GRAY; strokeWidth = 1f }
        canvas.drawLine(plotLeft, plotTop, plotLeft, plotBottom, axisPaint)
        canvas.drawLine(plotLeft, plotBottom, plotRight, plotBottom, axisPaint)

        // Min / Max Y Labels
        val labelPaint = Paint().apply { textSize = 8f; color = Color.GRAY }
        canvas.drawText(String.format(Locale.getDefault(), "%,.1f", maxVal), x + 5f, plotTop + 8f, labelPaint)
        canvas.drawText(String.format(Locale.getDefault(), "%,.1f", minVal), x + 5f, plotBottom, labelPaint)

        // Plot Points and Line
        val chartPath = Path()
        val pointPaint = Paint().apply { color = Color.rgb(0, 150, 136); style = Paint.Style.FILL }
        val strokePaint = Paint().apply {
            color = Color.rgb(0, 150, 136)
            strokeWidth = 2f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val displayPoints = points.takeLast(8) // Display last 8 for PDF clarity
        displayPoints.forEachIndexed { idx, pt ->
            val px = plotLeft + (idx.toFloat() / (displayPoints.size - 1)) * plotWidth
            val py = plotBottom - ((pt.kmPerLiter - minVal) / valRange).toFloat() * plotHeight

            if (idx == 0) {
                chartPath.moveTo(px, py)
            } else {
                chartPath.lineTo(px, py)
            }

            canvas.drawCircle(px, py, 3f, pointPaint)

            // X Labels (First and Last)
            if (idx == 0 || idx == displayPoints.size - 1) {
                canvas.drawText(pt.dateLabel, px - 12f, plotBottom + 12f, labelPaint)
            }
        }

        canvas.drawPath(chartPath, strokePaint)
    }

    fun shareFile(context: Context, file: File) {
        val authority = "${context.packageName}.fileprovider"
        val contentUri = FileProvider.getUriForFile(context, authority, file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Compartir Reporte"))
    }
}
