package cerg.mnv.view

import android.os.Bundle
import android.widget.TextView
import android.text.style.ForegroundColorSpan
import android.text.Spannable
import android.graphics.Color
import networking.ClientListener
import org.json.JSONException
import org.json.JSONObject
import android.content.Intent
import android.os.Handler
import cerg.mnv.R
import mpmgame.MpmGameMessageError
import publisher.JsonMessage
import publisher.MESSAGE_STATUS
import publisher.MESSAGE_TYPE
import utils.createJsonForGivenType
import java.sql.Timestamp
import java.util.*
import kotlin.concurrent.schedule

enum class MessageLevel(var color: Int) {
    INFO(Color.BLUE), MESSAGE(Color.GREEN), ERROR(Color.RED), CONNECTION_STATE(Color.WHITE)
}

class MonitorActivity : AbstractServiceView() {
    private var textView: TextView? = null

    private val defaultClientListener = object : ClientListener {
        override fun onClientMessage(message: String) {
            val jsonObject = JSONObject(message)
            val messageLevel by lazy {
                try {
                    if (jsonObject.getString("type") == "error") MessageLevel.ERROR else MessageLevel.INFO
                } catch (ex: JSONException) {
                    MessageLevel.INFO
                }
            }
            appendToTextView("Log: $message", messageLevel)
        }

        override fun onServerDisconnect() {
            appendToTextView("Log: ${utils.createInfoJson("Disconnected from Server")}", MessageLevel.CONNECTION_STATE)
            this@MonitorActivity.stopAllSoundActivities()
        }

        override fun onServerMessage(message: String) {
            this@MonitorActivity.processServerMessage(message)
            val json = JSONObject(message)

          /*  if (json.get("type")=="frontend" && message.isNotEmpty()){
                val messageText = json.get("content").toString()
                var interruptionLength = messageText.toLong().times(1000)
                //this@MonitorActivity.sendBackEndMessage("Start Interruption task")

                appendToTextView("Message: " + messageText, MessageLevel.MESSAGE)

                Timer().schedule(interruptionLength) {
                    val time = Timestamp(System.currentTimeMillis())
                    val timeString = time.toString()
                    this@MonitorActivity.sendBackEndMessage(timeString + " 1")
                }
            }

           */
        }

        override fun onServerConnect() {
            appendToTextView("Log: ${utils.createInfoJson("Connected to Server")}", MessageLevel.CONNECTION_STATE)
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.initPreferences()
        this.addClientListener(this.defaultClientListener)

        setContentView(R.layout.monitor)
        this.textView = this.findViewById(R.id.output) as TextView
    }

    private fun appendToTextView(msg: String, level: MessageLevel = MessageLevel.INFO) {
        runOnUiThread {
            if (this.textView != null) {
                val start = this.textView!!.text.length
                this.textView!!.append(msg + "\n")
                val end = this.textView!!.text.length
                val spannableText = this.textView!!.text as Spannable
                spannableText.setSpan(ForegroundColorSpan(level.color), start, end, 0)
                if (this.textView!!.layout != null)
                    this.textView!!.scrollTo(0, this.textView!!.layout.height - this.textView!!.height)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        this.addClientListener(this.defaultClientListener)
        this.bindAndStartNetworkinService()
        this@MonitorActivity.sendBackEndMessage("hello backend, I started")
        this.checkForSoundDownloads()
    }

    override fun onStop() {
        super.onStop()

        this.stopCheckingForSoundDownloads()

        this.removeClientListener(this.defaultClientListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        this.unbindNetworkinService()
    }


}
