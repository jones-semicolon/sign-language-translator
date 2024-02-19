package com.android.signlanguagetranslator

import android.content.Context
import android.os.AsyncTask
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class ModelDownloader(private val context: Context) {
    private val modelFileName = "slt_model.task" // Name of the model file
    private val modelUrl = "https://raw.githubusercontent.com/jones-semicolon/sign-language-translator/main/${modelFileName}" // GitHub raw content URL of the model file

    fun downloadModelIfNeeded() {
        if (!modelExists() || isModelOutdated()) {
            // Download the model if it doesn't exist or if it's outdated
            DownloadModelTask().execute(modelUrl)
        }
    }

    private fun modelExists(): Boolean {
        val file = context.getFileStreamPath(modelFileName)
        return file.exists()
    }

    private fun isModelOutdated(): Boolean {
        // Implement logic to check if the downloaded model is outdated
        return false // Return true if outdated, false otherwise
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
                val fos = FileOutputStream(context.getFileStreamPath(modelFileName))
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
            if (result) {
                // Model downloaded successfully
            } else {
                // Failed to download the model
            }
        }
    }
}