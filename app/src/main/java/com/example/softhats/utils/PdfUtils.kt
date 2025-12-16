package com.example.softhats.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.example.softhats.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfUtils {

    fun generarTicketPdf(
        context: Context,
        compraId: String,
        items: List<Pair<String, Double>>,
        total: Double
    ): File {

        val pageWidth = 300
        val pageHeight = 750

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 10f
        }

        var y = 20f
        val centerX = pageWidth / 2f

        // -------------------------
        // 🧢 LOGO
        // -------------------------
        val logoBitmap = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.ico
        )
        val logoScaled = Bitmap.createScaledBitmap(logoBitmap, 120, 120, true)
        canvas.drawBitmap(logoScaled, (pageWidth - 120) / 2f, y, null)
        y += 135f

        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Ticket de compra", centerX, y, paint)

        y += 10f
        canvas.drawText("------------------------------", centerX, y, paint)

        // -------------------------
        // 📅 FECHA Y FOLIO
        // -------------------------
        paint.textAlign = Paint.Align.LEFT
        y += 20f

        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Fecha: $fecha", 10f, y, paint)

        y += 14f
        canvas.drawText("Folio: $compraId", 10f, y, paint)

        y += 12f
        canvas.drawText("--------------------------------", 10f, y, paint)

        // -------------------------
        // 🛒 PRODUCTOS
        // -------------------------
        y += 14f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("PRODUCTOS", 10f, y, paint)

        paint.typeface = Typeface.DEFAULT
        y += 14f

        items.forEach { (nombre, precio) ->
            canvas.drawText(nombre, 10f, y, paint)
            y += 12f

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(
                "$${String.format("%.2f", precio)}",
                pageWidth - 10f,
                y,
                paint
            )

            paint.textAlign = Paint.Align.LEFT
            y += 10f
        }

        y += 10f
        canvas.drawText("--------------------------------", 10f, y, paint)

        // -------------------------
        // 💰 TOTAL
        // -------------------------
        y += 18f
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = 14f
        canvas.drawText("TOTAL", 10f, y, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(
            "$${String.format("%.2f", total)}",
            pageWidth - 10f,
            y,
            paint
        )

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT

        // -------------------------
        // 🔳 QR
        // -------------------------
        y += 30f

        val qrContenido = """
            Pedido SOFTHATS
            Folio: $compraId
            Total: $${String.format("%.2f", total)}
            Fecha: $fecha
        """.trimIndent()

        val qrBitmap = generarQr(qrContenido, 160)
        canvas.drawBitmap(qrBitmap, (pageWidth - 160) / 2f, y, null)

        y += 170f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Escanea para ver tu pedido", centerX, y, paint)

        y += 20f
        canvas.drawText("¡Gracias por tu compra!", centerX, y, paint)

        pdfDocument.finishPage(page)

        // -------------------------
        // 💾 GUARDAR PDF (AQUÍ ESTÁ LA CLAVE)
        // -------------------------
        val pdfFile = File(
            context.filesDir,
            "Ticket_$compraId.pdf"
        )

        val fos = FileOutputStream(pdfFile)
        pdfDocument.writeTo(fos)
        pdfDocument.close()
        fos.close()

        return pdfFile
    }

    // -------------------------
    // 🔳 QR
    // -------------------------
    private fun generarQr(texto: String, size: Int): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(texto, BarcodeFormat.QR_CODE, size, size)

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(
                    x,
                    y,
                    if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                )
            }
        }
        return bitmap
    }
}
