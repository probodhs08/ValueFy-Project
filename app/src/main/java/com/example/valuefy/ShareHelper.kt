package com.example.valuefy

import android.content.Context
import android.content.Intent
import android.widget.Toast

class ShareHelper(private val context: Context) {
    fun shareText(text: String) {
        if (text.isBlank()) {
            Toast.makeText(context, "Nothing to share!", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }

        val chooser = Intent.createChooser(intent, "Share via")
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(chooser)
        } else {
            Toast.makeText(context, "No app available to share!", Toast.LENGTH_SHORT).show()
        }
    }
}
