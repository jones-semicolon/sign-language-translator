package com.android.signlanguagetranslator

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ModelDownloader(private val context: Context) {
    private val modelFileName = "slt_model.task" // Name of the model file
    private val modelUrl = "https://raw.githubusercontent.com/jones-semicolon/sign-language-translator/main/$modelFileName" // GitHub raw content URL of the model file
    private var progressDialog: ProgressDialog? = null

    fun downloadModelIfNeeded() {
        val localFile = File(context.filesDir, modelFileName)
        if (!localFile.exists() || isModelOutdated(localFile)) {
            // Download the model if it doesn't exist or if it's outdated
            showProgressDialog()
            DownloadModelTask().execute(modelUrl)
        }
    }

    private fun showProgressDialog() {
        progressDialog = ProgressDialog(context).apply {
            setMessage("Downloading model file...")
            setCancelable(false)
            show()
        }
    }

    private fun dismissProgressDialog() {
        progressDialog?.dismiss()
    }

    private fun isModelOutdated(localFile: File): Boolean {
        // Get the last modified timestamp of the local file
        val localLastModified = localFile.lastModified()

        // You would need to implement logic here to fetch the last modified timestamp
        // or version of the file from the server. For example, if the server provides
        // the last modified timestamp in the HTTP headers, you can retrieve it here
        // and compare it with the local last modified timestamp.

        // For demonstration purposes, let's assume the server provides the last modified timestamp
        // in milliseconds since epoch in the HTTP headers. Replace this logic with your actual implementation.
        // Get the server last modified timestamp asynchronously
        getServerLastModifiedAsync { serverLastModified ->
            // Compare the server's last modified timestamp with the local timestamp
            val isOutdated = serverLastModified > localLastModified

            // Once the comparison is done, handle the result
            if (isOutdated) {
                // The model is outdated, initiate download or take appropriate action
                // For example: downloadModelIfNeeded()
                Log.d("SLT MODEL", "lastModified: ${localLastModified}, severLastModified: $serverLastModified")
            } else {
                // The model is up-to-date, no action needed
                Log.d("SLT MODEL", "lastModified: ${localLastModified}, severLastModified: $serverLastModified")
            }
        }

        return false
    }

    private fun getServerLastModifiedAsync(callback: (Long) -> Unit) {
        AsyncTask.execute {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(modelUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "HEAD"
                connection.connect()
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val lastModifiedHeader = connection.getHeaderField("Last-Modified")
                    Log.d("SLT MODEL", lastModifiedHeader)
                    if (lastModifiedHeader != null) {
                        val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
                        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
                        val lastModified = dateFormat.parse(lastModifiedHeader)?.time ?: 0L
                        callback(lastModified)
                        return@execute
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                connection?.disconnect()
            }
            callback(0L)
        }
    }

    private inner class DownloadModelTask : AsyncTask<String, Void, Boolean>() {
        override fun doInBackground(vararg params: String): Boolean {
            val modelUrl = params[0]
            try {
                val url = URL(modelUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

                // Download the model file
                val input: InputStream = connection.inputStream
                val fos = FileOutputStream(File(context.filesDir, modelFileName))
                val buffer = ByteArray(1024)
                var len: Int
                while (input.read(buffer).also { len = it } > 0) {
                    fos.write(buffer, 0, len)
                }
                fos.close()
                input.close()
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }

        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)
            dismissProgressDialog()
            if (result) {
                // Model downloaded successfully
                Toast.makeText(context, "Model downloaded successfully", Toast.LENGTH_SHORT).show()
            } else {
                // Failed to download the model
                Toast.makeText(context, "Failed to download the model", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
