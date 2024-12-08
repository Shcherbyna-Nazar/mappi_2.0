package com.example.mappi.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object ImageUtils {
    suspend fun compressImage(uri: Uri, context: Context): File {
        return withContext(Dispatchers.IO) {
            try {
                // Convert URI to File
                val originalFile = if (uri.scheme == "content") {
                    copyUriToFile(uri, context)
                } else {
                    File(uri.path ?: throw IllegalArgumentException("Invalid file path"))
                }

                // Compress the file
                Compressor.compress(context, originalFile) {
                    resolution(1280, 720) // Set a resolution limit
                    quality(75) // Set quality to 75%
                    format(Bitmap.CompressFormat.JPEG) // Use JPEG format
                }
            } catch (e: Exception) {
                Log.e("CompressImage", "Error compressing image", e)
                throw e
            }
        }
    }

    private fun copyUriToFile(uri: Uri, context: Context): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Unable to open URI: $uri")
        val tempFile = File.createTempFile("temp_", ".jpg", context.cacheDir)
        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    /**
     * Helper function to extract the storage path from the download URL.
     */
    fun extractStoragePath(downloadUrl: String): String? {
        return try {

            val urlParts = downloadUrl.split("?")[0]
            val encodedPath = urlParts.substringAfter("/o/")
            Uri.decode(encodedPath)
        } catch (e: Exception) {
            Log.e("FirebaseDataSource", "Error extracting storage path: ${e.localizedMessage}")
            null
        }
    }
}
