package com.autocare.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.autocare.data.entity.Vehicle
import com.autocare.data.entity.FuelLoad
import com.autocare.data.entity.Maintenance
import com.autocare.data.entity.Expense
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ReportUtils {

    fun generateVehicleReport(
        context: Context,
        vehicle: Vehicle,
        fuelLoads: List<FuelLoad>,
        maintenances: List<Maintenance>,
        expenses: List<Expense>
    ): File? {
        val pdfDocument = PdfDocument()
        val titlePaint = Paint().apply {
            textSize = 24f
            isFakeBoldText = true
            color = Color.BLACK
        }
        val headerPaint = Paint().apply {
            textSize = 18f
            isFakeBoldText = true
            color = Color.rgb(0, 102, 204) // Professional Blue
        }
        val textPaint = Paint().apply {
            textSize = 12f
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
        var y = 60f

        // Header Section
        canvas.drawText("AUTOCARE", 50f, y, titlePaint)
        canvas.drawText("REPORTE DE HISTORIAL DE SERVICIO", 50f, y + 25f, subTextPaint)
        
        val now = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Generado el: $now", 400f, y, subTextPaint)
        
        y += 60f
        canvas.drawLine(50f, y, 545f, y, linePaint)
        y += 30f

        // Vehicle Info Section
        canvas.drawText("INFORMACIÓN DEL VEHÍCULO", 50f, y, headerPaint)
        y += 30f
        
        canvas.drawRect(50f, y - 5f, 545f, y + 70f, Paint().apply { color = Color.rgb(245, 245, 245); style = Paint.Style.FILL })
        
        y += 20f
        canvas.drawText("Marca/Modelo: ${vehicle.brand} ${vehicle.model} (${vehicle.year})", 65f, y, textPaint)
        y += 20f
        canvas.drawText("Placa: ${vehicle.plate}", 65f, y, textPaint)
        y += 20f
        canvas.drawText("Kilometraje Actual: ${String.format("%,d", vehicle.currentKm)} km", 65f, y, textPaint)
        
        y += 50f
        
        // Operational Summary Section
        canvas.drawText("RESUMEN DE OPERACIÓN", 50f, y, headerPaint)
        y += 30f

        val totalFuel = fuelLoads.sumOf { it.totalCost }
        val totalMaint = maintenances.sumOf { it.cost }
        val totalExp = expenses.sumOf { it.amount }
        val total = totalFuel + totalMaint + totalExp
        
        val liters = fuelLoads.sumOf { it.liters }
        val avgConsumption = if (liters > 0) (fuelLoads.maxOfOrNull { it.odometer }?.minus(fuelLoads.minOfOrNull { it.odometer } ?: 0) ?: 0).toDouble() / liters else 0.0

        canvas.drawText("Gasto en Combustible:", 65f, y, textPaint)
        canvas.drawText("$${String.format("%.2f", totalFuel)}", 400f, y, textPaint)
        y += 20f
        canvas.drawText("Gasto en Mantenimiento:", 65f, y, textPaint)
        canvas.drawText("$${String.format("%.2f", totalMaint)}", 400f, y, textPaint)
        y += 20f
        canvas.drawText("Otros Gastos:", 65f, y, textPaint)
        canvas.drawText("$${String.format("%.2f", totalExp)}", 400f, y, textPaint)
        y += 20f
        canvas.drawLine(65f, y, 545f, y, linePaint)
        y += 25f
        canvas.drawText("TOTAL ACUMULADO:", 65f, y, Paint(textPaint).apply { isFakeBoldText = true })
        canvas.drawText("$${String.format("%.2f", total)}", 400f, y, Paint(textPaint).apply { isFakeBoldText = true })
        
        y += 30f
        canvas.drawText("Eficiencia Promedio: ${String.format("%.2f", avgConsumption)} km/L", 65f, y, subTextPaint)
        
        y += 50f

        // Maintenance Log Section
        canvas.drawText("HISTORIAL DE MANTENIMIENTO", 50f, y, headerPaint)
        y += 25f
        canvas.drawLine(50f, y, 545f, y, Paint(linePaint).apply { strokeWidth = 2f; color = Color.BLACK })
        y += 20f
        
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        
        maintenances.sortedByDescending { it.date }.forEach { maint ->
            if (y > 780f) {
                pdfDocument.finishPage(page)
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
                canvas.drawLine(50f, y, 545f, y, Paint(linePaint).apply { strokeWidth = 2f; color = Color.BLACK })
                y += 20f
            }
            
            val dateStr = dateFormat.format(Date(maint.date))
            canvas.drawText(dateStr, 50f, y, textPaint)
            canvas.drawText(maint.type, 150f, y, Paint(textPaint).apply { isFakeBoldText = true })
            canvas.drawText("$${String.format("%.2f", maint.cost)}", 450f, y, textPaint)
            y += 15f
            canvas.drawText("${String.format("%,d", maint.km)} km - ${maint.notes}", 150f, y, subTextPaint)
            y += 10f
            canvas.drawLine(50f, y, 545f, y, linePaint)
            y += 20f
        }

        pdfDocument.finishPage(page)

        val file = File(context.cacheDir, "Historial_${vehicle.plate}_${System.currentTimeMillis()}.pdf")
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
