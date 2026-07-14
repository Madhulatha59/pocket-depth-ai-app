package com.example.pocketdepthai.ui

import android.content.Context
import android.content.ContentValues
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun generateReportPdf(
    context: Context,
    patientName: String,
    doctorName: String,
    verdict: String,
    stageAndGrade: String,
    meanPpd: Float,
    meanCal: Float,
    bopPercentage: Float,
    deepPocketsCount: Int
): File {
    val pdfDoc = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDoc.startPage(pageInfo)
    val canvas = page.canvas

    val paint = Paint()
    paint.isAntiAlias = true

    // Background color
    canvas.drawColor(Color.WHITE)

    // Draw header/logo background banner
    paint.color = Color.parseColor("#F5F7FF")
    canvas.drawRect(20f, 20f, 575f, 120f, paint)

    // Header Title
    paint.color = Color.parseColor("#5D48D1")
    paint.textSize = 20f
    paint.isFakeBoldText = true
    canvas.drawText("POCKET DEPTH AI - CLINICAL REPORT", 40f, 65f, paint)

    paint.color = Color.GRAY
    paint.textSize = 11f
    paint.isFakeBoldText = false
    canvas.drawText("Automated Periodontal Diagnostics & Metrics Summary", 40f, 95f, paint)

    // Metadata section
    paint.color = Color.BLACK
    paint.textSize = 14f
    paint.isFakeBoldText = true
    canvas.drawText("Patient Information", 40f, 160f, paint)

    paint.isFakeBoldText = false
    paint.textSize = 12f
    canvas.drawText("Patient Name: $patientName", 40f, 190f, paint)
    canvas.drawText("Attending Clinician: Dr. $doctorName", 40f, 210f, paint)
    canvas.drawText("Report Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}", 40f, 230f, paint)

    // Diagnostic Results section
    paint.color = Color.BLACK
    paint.textSize = 14f
    paint.isFakeBoldText = true
    canvas.drawText("Diagnostic Results & AI Verdict", 40f, 280f, paint)

    paint.isFakeBoldText = false
    paint.textSize = 12f
    canvas.drawText("Clinical Verdict:", 40f, 310f, paint)
    
    // Choose color for severity
    val verdictColor = when {
        verdict.contains("Severe") -> "#D84315"
        verdict.contains("Moderate") -> "#FF9800"
        else -> "#4CAF50"
    }
    paint.color = Color.parseColor(verdictColor)
    paint.isFakeBoldText = true
    canvas.drawText("$verdict ($stageAndGrade)", 170f, 310f, paint)

    paint.color = Color.BLACK
    paint.isFakeBoldText = false
    canvas.drawText("Mean PPD (Pocket Depth):", 40f, 340f, paint)
    canvas.drawText("${meanPpd} mm", 270f, 340f, paint)

    canvas.drawText("Mean CAL (Attachment Loss):", 40f, 360f, paint)
    canvas.drawText("${meanCal} mm", 270f, 360f, paint)

    canvas.drawText("BOP Positive (Bleeding on Probing):", 40f, 380f, paint)
    canvas.drawText("${bopPercentage.toInt()}% of sites", 270f, 380f, paint)

    canvas.drawText("Deep Pockets Identified (>= 4mm):", 40f, 400f, paint)
    canvas.drawText("$deepPocketsCount sites", 270f, 400f, paint)

    // Divider line
    paint.color = Color.LTGRAY
    canvas.drawLine(40f, 430f, 550f, 430f, paint)

    // Recommendations section
    paint.color = Color.BLACK
    paint.textSize = 14f
    paint.isFakeBoldText = true
    canvas.drawText("Recommended Treatment Plan", 40f, 465f, paint)

    paint.isFakeBoldText = false
    paint.textSize = 11f
    var yOffset = 495f
    val recommendations = if (verdict.contains("Healthy")) {
        listOf(
            "- Maintain excellent daily oral hygiene habits (brush twice daily, floss daily).",
            "- Routine 6-month clinical evaluation and prophylaxis (professional cleaning).",
            "- Periodontal maintenance monitoring at regular intervals."
        )
    } else {
        listOf(
            "- Full-mouth mechanical debridement (Scaling and Root Planing - SRP).",
            "- Comprehensive assessment of systemic risk factors (Diabetes mellitus, smoking, etc.).",
            "- Strict 3-month periodontal maintenance and supportive periodontal therapy (SPT).",
            "- Re-evaluation of pocket depths and tissue response in 4-6 weeks."
        )
    }

    for (rec in recommendations) {
        canvas.drawText(rec, 40f, yOffset, paint)
        yOffset += 22f
    }

    // Signature
    paint.color = Color.GRAY
    paint.textSize = 11f
    paint.isFakeBoldText = true
    canvas.drawText("Electronically Signed by: Dr. $doctorName", 40f, 760f, paint)

    pdfDoc.finishPage(page)

    val tempFile = File(context.cacheDir, "Clinical_Periodontal_Report.pdf")
    FileOutputStream(tempFile).use { fos ->
        pdfDoc.writeTo(fos)
    }
    pdfDoc.close()

    return tempFile
}

fun savePdfToDownloads(context: Context, cachedFile: File): File? {
    val fileName = "Periodontal_Report_${System.currentTimeMillis()}.pdf"
    val resolver = context.contentResolver
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { os ->
                cachedFile.inputStream().use { inputStream ->
                    inputStream.copyTo(os)
                }
            }
            return File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
        }
    } else {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) downloadsDir.mkdirs()
        val destFile = File(downloadsDir, fileName)
        cachedFile.copyTo(destFile, overwrite = true)
        return destFile
    }
    return null
}

fun sharePdfFile(context: Context, file: File, specificPackage: String? = null) {
    val fileUri: Uri = FileProvider.getUriForFile(
        context,
        "com.example.pocketdepthai.fileprovider",
        file
    )
    
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, fileUri)
        putExtra(Intent.EXTRA_SUBJECT, "Clinical Periodontal Report")
        putExtra(Intent.EXTRA_TEXT, "Hello, please find attached the Clinical Periodontal Report.")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (specificPackage != null) {
            setPackage(specificPackage)
        }
    }
    
    val chooser = Intent.createChooser(shareIntent, "Share Clinical Report")
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
}
