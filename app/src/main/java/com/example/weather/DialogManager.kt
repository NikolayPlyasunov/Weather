package com.example.weather

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText
import com.android.volley.Response.Listener

object DialogManager {
    fun searchByNameDialog(context: Context, listener: Listener) {
        val builder = AlertDialog.Builder(context)
        val edName = EditText(context)
        builder.setView(edName)
        val dialog = builder.create()
        dialog.setTitle("City name:")
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, _ ->
            listener.onClick(edName.text.toString())
            dialog.dismiss()
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel") { _, _ ->
            dialog.dismiss()
        }
        dialog.show()

    }

    interface Listener {
        fun onClick(name: String?)
    }
}