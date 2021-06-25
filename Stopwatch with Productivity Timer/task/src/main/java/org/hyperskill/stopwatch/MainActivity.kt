@file:Suppress("KDocMissingDocumentation")

package org.hyperskill.stopwatch

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

val colorChannel: IntRange = 50..255

class MainActivity : AppCompatActivity() {
    enum class State { IDLE, RUNNING_INFINITE, RUNNING_LIMITED }

    private var currentState = State.IDLE
    private var timeLimit: Int = 0
    private var timer: Timer? = null
    private lateinit var alertDialog: AlertDialog

    private fun timerTask(): TimerTask {
        return object : TimerTask() {
            val startTime = System.currentTimeMillis()
            override fun run() {
                val elapsedTime = (System.currentTimeMillis() - startTime) / 1000
                runOnUiThread {
                    if (currentState == State.RUNNING_LIMITED && elapsedTime > timeLimit) {
                        textView.setTextColor(Color.RED)
                        startNotification()
                    }
                    setTextView(elapsedTime)
                    progressBar.indeterminateTintList = ColorStateList.valueOf(
                        Color.rgb(
                            colorChannel.random(),
                            colorChannel.random(),
                            colorChannel.random()
                        )
                    )
                }
            }
        }
    }

    private fun startTimer() {
        currentState = if (timeLimit > 0) State.RUNNING_LIMITED else State.RUNNING_INFINITE
        progressBar.visibility = View.VISIBLE
        settingsButton.isEnabled = false
        timer = Timer()
        timer?.scheduleAtFixedRate(timerTask(), 0L, 1000L)
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
        setTextView(0)
        textView.setTextColor(Color.BLACK)
        progressBar.visibility = View.GONE
        settingsButton.isEnabled = true
        currentState = State.IDLE
    }

    internal fun setTextView(timePassed: Long) {
        val s = String.format(
            resources.getString(R.string.timeHHMM),
            (timePassed / 60).toString().padStart(2, '0'),
            (timePassed % 60).toString().padStart(2, '0')
        )
        textView.text = s
    }

    private fun buildDialog(): AlertDialog {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = this.layoutInflater.inflate(R.layout.dialog_set_time, null)
        val editText = dialogView.findViewById<EditText>(R.id.upperLimitEditText)
        editText.setText(if (timeLimit == 0) "" else timeLimit.toString())
        dialogBuilder
            .setView(dialogView)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                timeLimit =
                    editText.text.toString().toIntOrNull() ?: 0
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                editText.setText(if (timeLimit == 0) "" else timeLimit.toString())
                dialog.dismiss()
            }
            .setCancelable(true)
        return dialogBuilder.create()
    }

    internal fun startNotification() {
        val notification = NotificationCompat
            .Builder(this, resources.getString(R.string.notification_channel_id))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(resources.getString(R.string.notification_title))
            .setContentText(resources.getString(R.string.notification_text))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        val notificationChannel = NotificationChannel(
            resources.getString(R.string.notification_channel_id),
            resources.getString(R.string.notification_text),
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        notificationManager.createNotificationChannel(notificationChannel)
        notificationManager.notify(393939, notification)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        alertDialog = buildDialog()
        startButton.setOnClickListener {
            if (currentState == State.IDLE) {
                startTimer()
            }
        }
        resetButton.setOnClickListener {
            stopTimer()
        }
        settingsButton.setOnClickListener {
            buildDialog().show()
        }
    }
}