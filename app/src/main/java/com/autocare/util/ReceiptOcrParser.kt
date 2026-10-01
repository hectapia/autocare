package com.autocare.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class ReceiptOcrData(
    val totalCost: Double? = null,
    val liters: Double? = null,
    val dateMillis: Long? = null,
    val vendorName: String? = null,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

object ReceiptOcrParser {

    private val recognizer by lazy { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    suspend fun processReceiptImage(context: Context, imageUri: Uri): ReceiptOcrData {
        return try {
            val inputImage = try {
                val bitmap = loadScaledBitmap(context, imageUri)
                if (bitmap != null) {
                    InputImage.fromBitmap(bitmap, 0)
                } else {
                    InputImage.fromFilePath(context, imageUri)
                }
            } catch (t: Throwable) {
                return ReceiptOcrData(
                    isSuccess = false,
                    errorMessage = "No se pudo cargar la imagen del ticket. Intenta con otra fotografía."
                )
            }

            val resultText: Text = recognizer.process(inputImage).awaitTask()
            
            val lines: List<String> = resultText.textBlocks.flatMap { block ->
                block.lines.map { line -> line.text.trim() }
            }

            var vendorName: String? = null
            var totalCost: Double? = null
            var liters: Double? = null
            var dateMillis: Long? = null

            // 1. Vendor Name Heuristic (top 3 lines)
            if (lines.isNotEmpty()) {
                vendorName = lines.take(3).firstOrNull { line ->
                    line.length >= 3 && !line.matches(Regex("^\\d+$")) && !line.lowercase().contains("ticket")
                }
            }

            // 2. Parse Amounts & Liters line by line
            val numberRegex = Pattern.compile("(\\d+[.,]\\d{2})")
            val amountKeywords = listOf("total", "importe", "monto", "neto", "costo", "pagado", "$")
            val litersKeywords = listOf("litros", "litro", "volumen", "lts", "lt", "l")

            var maxFoundAmount = 0.0

            for (line in lines) {
                val lowerLine = line.lowercase()

                // Check for Liters
                if (liters == null && litersKeywords.any { lowerLine.contains(it) }) {
                    val matcher = numberRegex.matcher(line)
                    if (matcher.find()) {
                        val numStr = matcher.group(1)?.replace(",", ".")
                        val valDouble = numStr?.toDoubleOrNull()
                        if (valDouble != null && valDouble in 0.1..300.0) {
                            liters = valDouble
                        }
                    }
                }

                // Check for Total Amount
                if (amountKeywords.any { lowerLine.contains(it) }) {
                    val matcher = numberRegex.matcher(line)
                    if (matcher.find()) {
                        val numStr = matcher.group(1)?.replace(",", ".")
                        val valDouble = numStr?.toDoubleOrNull()
                        if (valDouble != null && valDouble > maxFoundAmount) {
                            maxFoundAmount = valDouble
                        }
                    }
                }

                // Parse Date
                if (dateMillis == null) {
                    val parsedDate = parseDateFromLine(line)
                    if (parsedDate != null) {
                        dateMillis = parsedDate
                    }
                }
            }

            if (maxFoundAmount > 0.0) {
                totalCost = maxFoundAmount
            } else {
                // Fallback: search all line numbers for highest dollar amount
                val allAmounts = lines.flatMap { line ->
                    val matcher = numberRegex.matcher(line)
                    val list = mutableListOf<Double>()
                    while (matcher.find()) {
                        val numStr = matcher.group(1)?.replace(",", ".")
                        numStr?.toDoubleOrNull()?.let { list.add(it) }
                    }
                    list
                }
                val topAmount = allAmounts.filter { it in 1.0..100000.0 }.maxOrNull()
                if (topAmount != null) {
                    totalCost = topAmount
                }
            }

            val hasData = totalCost != null || liters != null || dateMillis != null || vendorName != null
            ReceiptOcrData(
                totalCost = totalCost,
                liters = liters,
                dateMillis = dateMillis,
                vendorName = vendorName,
                isSuccess = hasData,
                errorMessage = if (hasData) null else "No se encontraron datos legibles en el ticket."
            )
        } catch (t: Throwable) {
            t.printStackTrace()
            ReceiptOcrData(
                isSuccess = false,
                errorMessage = "No se pudo leer el ticket. Intenta con más zoom o mejor iluminación."
            )
        }
    }

    private fun loadScaledBitmap(context: Context, imageUri: Uri): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(imageUri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            var sampleSize = 1
            val maxDimension = 1280
            while (options.outWidth / sampleSize > maxDimension || options.outHeight / sampleSize > maxDimension) {
                sampleSize *= 2
            }

            val scaleOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }

            context.contentResolver.openInputStream(imageUri)?.use {
                BitmapFactory.decodeStream(it, null, scaleOptions)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseDateFromLine(line: String): Long? {
        val datePatterns = listOf(
            Regex("(\\d{1,2})[/.-](\\d{1,2})[/.-](\\d{4})"),           // 15/01/2026 or 5/1/2026
            Regex("(\\d{4})[/.-](\\d{1,2})[/.-](\\d{1,2})"),           // 2026-01-15
            Regex("(\\d{1,2})[/.-]([a-zA-Z]{3,4})[/.-](\\d{2,4})"),    // 15/Ene/2026 or 15-Jan-26
            Regex("(\\d{1,2})\\s+([a-zA-Z]{3,4})\\s+(\\d{2,4})"),       // 15 Ene 2026 or 15 Jan 26
            Regex("([a-zA-Z]{3,4})\\s+(\\d{1,2})[,\\s]+\\s*(\\d{2,4})"), // Jan 15, 2026 or Ene 15 26
            Regex("(\\d{1,2})[/.-](\\d{1,2})[/.-](\\d{2})")            // 15/01/26
        )

        val locales = listOf(Locale("es"), Locale.US, Locale.getDefault())

        for (regex in datePatterns) {
            val match = regex.find(line)
            if (match != null) {
                val str = match.value
                val dateFormats = listOf(
                    "dd/MM/yyyy", "dd-MM-yyyy", "dd.MM.yyyy",
                    "yyyy-MM-dd", "yyyy/MM/dd", "yyyy.MM.dd",
                    "dd/MMM/yyyy", "dd-MMM-yyyy", "dd MMM yyyy",
                    "dd/MMM/yy", "dd-MMM-yy", "dd MMM yy",
                    "MMM dd, yyyy", "MMM dd yyyy", "MMM dd, yy", "MMM dd yy",
                    "dd/MM/yy", "dd-MM-yy", "dd.MM.yy"
                )

                for (pattern in dateFormats) {
                    for (locale in locales) {
                        try {
                            val sdf = SimpleDateFormat(pattern, locale).apply {
                                isLenient = false
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            val date = sdf.parse(str)
                            if (date != null) {
                                return date.time
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        }
        return null
    }
}

private suspend fun <T> Task<T>.awaitTask(): T =
    suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result -> continuation.resume(result) }
        addOnFailureListener { exception -> continuation.resumeWithException(exception) }
    }
