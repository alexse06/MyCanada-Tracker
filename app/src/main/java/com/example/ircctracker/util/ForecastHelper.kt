package com.example.ircctracker.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter

data class ForecastResult(
    val estimatedDate: String,
    val daysRemaining: Long,
    val progress: Float // 0.0 to 1.0
)

object ForecastHelper {

    fun calculateForecast(dateReceived: String?, processingMonths: Int?): ForecastResult? {
        if (dateReceived == null || processingMonths == null || processingMonths <= 0) return null
        
        // Simple logic for older Android versions fallback
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return ForecastResult("Estimated: +$processingMonths Months", 30L * processingMonths, 0.5f)
        }

        return try {
            val receivedDate = LocalDate.parse(dateReceived.take(10))
            val estimatedCompletion = receivedDate.plusMonths(processingMonths.toLong())
            val today = LocalDate.now()
            
            val daysRemaining = ChronoUnit.DAYS.between(today, estimatedCompletion)
            val totalDays = ChronoUnit.DAYS.between(receivedDate, estimatedCompletion)
            val daysElapsed = ChronoUnit.DAYS.between(receivedDate, today)
            
            val progress = if (totalDays > 0) {
                daysElapsed.toFloat() / totalDays.toFloat()
            } else {
                0f
            }
            
            val formattedDate = estimatedCompletion.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
            
            ForecastResult(
                estimatedDate = formattedDate,
                daysRemaining = daysRemaining,
                progress = progress.coerceIn(0f, 1f)
            )
        } catch (e: Exception) {
            null
        }
    }
}
