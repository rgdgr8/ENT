package de.esolutions.connectedradioclient.debug.data

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import de.esolutions.connectedradioclient.data.StationLogo
import de.esolutions.connectedradioclient.data.bearer.IBearer
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.MessageDigest

class DemoStationLogo {

    private var run = 1

    /**
     * Returns demo station logo.
     */
    fun getDemoObject(bearer: IBearer, fileExtension: String): StationLogo {
        val bitmap: Bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val color = this.getColor(bearer)
        canvas.drawColor(color)
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK
        paint.textSize = 20f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText(bearer.getBearerString(), 5f, 150f, paint)
        canvas.save()
        val image = when (fileExtension.lowercase()) {
            "png" -> getImageAsByteArray(bitmap, Bitmap.CompressFormat.PNG)
            "jpg", "jpeg" -> getImageAsByteArray(bitmap, Bitmap.CompressFormat.JPEG)
            "webp" -> getImageAsByteArray(bitmap, Bitmap.CompressFormat.WEBP)
            else -> throw IllegalArgumentException("Unsupported file extension: $fileExtension")
        }
        return StationLogo(image, bearer.getBearerString().md5(), fileExtension)
    }

    /**
     * Return an colorInt for the given bearer.
     *
     * Create a hashCode from the bearer (something like 512347892)
     * But the hue value from HSV have to be between 0 and 360
     * With % 360 the value of the hash is always less than 360
     * To change the color on every second request we add 180 to the hash (for testing)
     *
     */
    private fun getColor(bearer: IBearer): Int {
        var hash = bearer.getBearerString().hashCode()
        if (this.run % 2 == 0) {
            hash += 180
        }
        val hue = hash % 360
        val hsv = floatArrayOf(hue.toFloat(), 0.35F, 1.0F)
        val color = Color.HSVToColor(hsv)
        this.run++
        return color
    }

    private fun getImageAsByteArray(bitmap: Bitmap, format: Bitmap.CompressFormat): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(format, 100, stream)
        return stream.toByteArray()
    }

    private fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
    }
}