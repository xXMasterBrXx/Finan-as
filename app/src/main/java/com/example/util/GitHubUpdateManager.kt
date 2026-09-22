package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

sealed class UpdateCheckResult {
    data class UpdateAvailable(val latestVersionName: String, val downloadUrl: String, val releaseNotes: String) : UpdateCheckResult()
    data class NoUpdate(val currentTag: String = "") : UpdateCheckResult()
    data class Error(val message: String) : UpdateCheckResult()
}

class GitHubUpdateManager {
    companion object {
        const val DEFAULT_REPO = "xXMasterBrXx/Finan-as"
    }

    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    suspend fun checkForUpdates(
        ownerRepo: String = DEFAULT_REPO,
        installedVersion: String? = null
    ): UpdateCheckResult = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.github.com/repos/$ownerRepo/releases/latest"
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/vnd.github+json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext UpdateCheckResult.Error("Falha ao consultar repositório (${response.code})")
                }
                val body = response.body?.string() ?: return@withContext UpdateCheckResult.Error("Resposta do GitHub vazia")
                val json = JSONObject(body)
                val tagName = json.optString("tag_name", "")
                val bodyText = json.optString("body", "Sem notas de versão.")
                
                // Extract clean version name (e.g. "v0.1.2" -> "0.1.2")
                val cleanTagName = tagName.removePrefix("v").removePrefix("V").trim()
                val currentVersion = (installedVersion ?: BuildConfig.VERSION_NAME).removePrefix("v").removePrefix("V").trim()

                // Find APK in assets
                val assetsArray = json.optJSONArray("assets")
                var downloadUrl: String? = null
                if (assetsArray != null) {
                    for (i in 0 until assetsArray.length()) {
                        val assetObj = assetsArray.getJSONObject(i)
                        val name = assetObj.optString("name", "")
                        if (name.endsWith(".apk")) {
                            downloadUrl = assetObj.optString("browser_download_url")
                            break
                        }
                    }
                }

                if (downloadUrl == null) {
                    return@withContext UpdateCheckResult.Error("Nenhum arquivo APK foi encontrado no release mais recente")
                }

                // Check if the tag name is newer than the current version
                if (isNewerVersion(cleanTagName, currentVersion)) {
                    UpdateCheckResult.UpdateAvailable(
                        latestVersionName = tagName,
                        downloadUrl = downloadUrl,
                        releaseNotes = bodyText
                    )
                } else {
                    UpdateCheckResult.NoUpdate(currentTag = tagName)
                }
            }
        } catch (e: Exception) {
            UpdateCheckResult.Error(e.localizedMessage ?: "Erro de conexão")
        }
    }

    fun isNewerVersion(latest: String, current: String): Boolean {
        // If current is the old unversioned placeholder "1.0" or "1.0.0",
        // and latest is an actual GitHub release (such as "0.1.2"), treat latest as an update.
        if ((current == "1.0" || current == "1.0.0" || current.startsWith("1.0.0")) && latest != current) {
            return true
        }

        try {
            val latestParts = latest.split(".").map { it.filter { ch -> ch.isDigit() }.toIntOrNull() ?: 0 }
            val currentParts = current.split(".").map { it.filter { ch -> ch.isDigit() }.toIntOrNull() ?: 0 }
            val maxLength = maxOf(latestParts.size, currentParts.size)
            for (i in 0 until maxLength) {
                val l = latestParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (l > c) return true
                if (l < c) return false
            }
        } catch (e: Exception) {
            // Fallback to direct string comparison if version formatting is custom
            return latest != current
        }
        return false
    }

    suspend fun downloadAndInstallApk(
        context: Context,
        downloadUrl: String,
        onProgress: (Int) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(downloadUrl).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Falha no download (${response.code})"))
                }
                val body = response.body ?: return@withContext Result.failure(Exception("Arquivo de download vazio"))
                val contentLength = body.contentLength()
                val cacheDir = context.externalCacheDir ?: context.cacheDir
                val apkFile = File(cacheDir, "update.apk")
                if (apkFile.exists()) {
                    apkFile.delete()
                }

                body.byteStream().use { inputStream ->
                    FileOutputStream(apkFile).use { outputStream ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var totalBytesRead = 0L
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead
                            if (contentLength > 0) {
                                val progress = ((totalBytesRead * 100) / contentLength).toInt()
                                onProgress(progress)
                            }
                        }
                    }
                }
                Result.success(apkFile)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun triggerInstall(context: Context, apkFile: File): Result<Unit> {
        return try {
            if (!apkFile.exists() || apkFile.length() == 0L) {
                return Result.failure(Exception("Arquivo APK não encontrado ou corrompido."))
            }
            apkFile.setReadable(true, false)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
                    return Result.failure(Exception("Autorize a instalação de fontes desconhecidas para o FinanFlow nas configurações do Android e tente novamente."))
                }
            }

            val authority = "${context.packageName}.fileprovider"
            val apkUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(context, authority, apkFile)
            } else {
                Uri.fromFile(apkFile)
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            val resolveInfoList = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resolveInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(packageName, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(intent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
