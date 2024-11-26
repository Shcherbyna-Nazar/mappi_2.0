package com.example.mappi.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

object ImageUtils {
    suspend fun loadBitmapFromUrl(url: String): Bitmap = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection()
            connection.connect()
            val inputStream = connection.getInputStream()
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            throw RuntimeException("Error loading image: $url", e)
        }
    }
}
