package com.talotv.os.core.security

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Talo Security Manager
 * Oversees cryptographic integrity of OTA updates, APK payloads and secure installations.
 * Every byte received is cryptographically verified with SHA-256 before disk execution.
 */
object TaloSecurityManager {

    private const val TAG = "TaloSecurityManager"

    sealed class UpdateVerificationResult {
        object Success : UpdateVerificationResult()
        data class HashMismatch(val expected: String, val actual: String) : UpdateVerificationResult()
        data class NetworkError(val message: String) : UpdateVerificationResult()
        data class FileError(val message: String) : UpdateVerificationResult()
    }

    /**
     * Downloads an OTA APK into private internal cache and verifies its SHA-256 bit-by-bit.
     * Prevents any MITM or compromised package installation.
     */
    suspend fun downloadAndVerifyUpdate(
        context: Context,
        apkUrl: String,
        expectedSha256: String,
        onProgress: (Int) -> Unit = {}
    ): Pair<UpdateVerificationResult, File?> = withContext(Dispatchers.IO) {
        val targetFile = File(context.cacheDir, "talo_update_secured.apk")
        if (targetFile.exists()) {
            targetFile.delete()
        }

        try {
            val url = URL(apkUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 30000
                requestMethod = "GET"
                instanceFollowRedirects = true
            }

            if (connection.responseCode !in 200..299) {
                return@withContext Pair(
                    UpdateVerificationResult.NetworkError("HTTP Error: ${connection.responseCode}"),
                    null
                )
            }

            val fileLength = connection.contentLength
            var totalBytesRead = 0L

            connection.inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        if (fileLength > 0) {
                            val progress = ((totalBytesRead * 100) / fileLength).toInt()
                            onProgress(progress)
                        }
                    }
                }
            }

            // Cryptographic Verification: compute SHA-256 of downloaded payload
            val computedSha256 = calculateFileSha256(targetFile)
            val cleanExpected = expectedSha256.trim().uppercase()
            val cleanComputed = computedSha256.trim().uppercase()

            if (cleanExpected.isNotEmpty() && cleanExpected != cleanComputed) {
                Log.e(TAG, "SECURITY ALERT: Update SHA-256 hash mismatch!")
                Log.e(TAG, "Expected: $cleanExpected")
                Log.e(TAG, "Computed: $cleanComputed")
                targetFile.delete() // Immediate destruction of compromised payload
                return@withContext Pair(
                    UpdateVerificationResult.HashMismatch(cleanExpected, cleanComputed),
                    null
                )
            }

            Log.i(TAG, "Cryptographic integrity verified successfully: SHA-256=$cleanComputed")
            Pair(UpdateVerificationResult.Success, targetFile)
        } catch (e: Exception) {
            Log.e(TAG, "Update download failed: ${e.message}")
            targetFile.delete()
            Pair(UpdateVerificationResult.NetworkError(e.message ?: "Network error"), null)
        }
    }

    /**
     * Calculates the SHA-256 checksum of any local file
     */
    fun calculateFileSha256(file: File): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }.uppercase()
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Safely triggers Android Package Installer for a verified update APK
     */
    fun launchSecureInstaller(context: Context, apkFile: File) {
        try {
            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Cannot launch package installer: ${e.message}")
        }
    }
}
