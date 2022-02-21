package cerg.mnv.view


import android.content.Intent
import android.content.res.Configuration
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import cerg.mnv.R
import networking.ClientListener
import org.json.JSONException
import org.json.JSONObject
import java.sql.Timestamp
import java.util.*
import kotlin.concurrent.schedule
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.util.ArrayList


class CalibrationActivity : AbstractServiceView() {
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
    }


    private var rightLow: TextView? = null
    private var leftLow: TextView? = null
    private var rightUp: TextView? = null
    private var leftUp: TextView? = null
    private var centerRightLow: TextView? = null
    private var centerLeftLow: TextView? = null
    private var centerRightUp: TextView? = null
    private var centerLeftUp: TextView? = null
    private var whiteRectangle: ImageView? = null
    private var background: ImageView? = null
    private var centerLayout: RelativeLayout? = null

    private var listOfNumbersLeftTop = listOf<Int>(2, 4, 5, 9, 1, 3, 4, 7, 8, 5, 6, 7, 3, 2, 3, 4, 5, 1, 5, 4, 7, 8, 9, 2, 4, 5, 9, 1, 3, 4, 7, 8, 5, 6, 7, 3, 2, 3)
    private var listOfNumbersRightTop = listOf<Int>(4, 5, 1, 5, 4, 7, 8, 9, 2, 4, 5, 9, 1, 3, 4, 7, 8, 5, 6, 7, 3, 2, 3, 4, 5, 1, 5, 4, 7, 8, 9, 3, 2, 4, 8, 2, 7, 5)
    private var listOfNumbersLeftBottom = listOf<Int>(6, 2, 4, 1, 7, 2, 5, 6, 9, 2, 1, 6, 3, 4, 7, 2, 3, 4, 7, 8, 5, 6, 7, 3, 2, 3, 4, 5, 1, 5, 4, 7, 1, 4, 6, 8, 9)
    private var listOfNumbersRightBottom = listOf<Int>(3, 6, 2, 1, 5, 7, 3, 5, 2, 4, 5, 2, 7, 8, 2, 4, 9, 1, 3, 1, 8, 2, 5, 3, 7, 3, 7, 2, 5, 3, 6, 9, 4, 1, 4)


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
            if (messageLevel == MessageLevel.ERROR) Log.e("CalibrationActivity", message)
            else Log.i("CalibrationActivity", message)
        }

        override fun onServerDisconnect() {
            Log.i("CalibrationActivity", "Disconnected from Server")
            // this@ArithmeticActivity.stopAllSoundActivities()
            this@CalibrationActivity.endScenario()
        }

        override fun onServerMessage(message: String) {

            this@CalibrationActivity.processServerMessage(message)
            val jsonMessage = JSONObject(message)
            if (jsonMessage.get("type") != "expDataHMD" && message.isNotEmpty()) {
                var endCalibration = jsonMessage.getBoolean("content")
                if (endCalibration) {
                    /*val training = jsonMessage.getBoolean("training")
                    this@CalibrationActivity.setPreference("Training", training.toString())
                     */
                    val interruptionTask = this@CalibrationActivity.getPreference("interruptionTask")
                    if (interruptionTask == "alarm") {
                        this@CalibrationActivity.startActivity(Intent(this@CalibrationActivity, AlarmActivity::class.java))
                    } else if (interruptionTask == "arithmetic") {
                        this@CalibrationActivity.startActivity(Intent(this@CalibrationActivity, ArithmeticActivity::class.java))
                    }
                } else {
                    changeNumbers()
                }
            }
        }

        override fun onServerConnect() {
            Log.i("CalibrationActivity", "Connected to Server")
        }
    }

    private fun changeNumbers() {
        runOnUiThread {
            if (this.listOfNumbersLeftBottom.isNotEmpty() && this.listOfNumbersLeftTop.isNotEmpty()) {
                this.centerLeftUp!!.text = this.listOfNumbersLeftTop.first().toString()
                this.listOfNumbersLeftTop = this.listOfNumbersLeftTop.drop(1)
                this.centerLeftLow!!.text = this.listOfNumbersLeftBottom.first().toString()
                this.listOfNumbersLeftBottom = this.listOfNumbersLeftBottom.drop(1)
            }
            if (this.listOfNumbersRightBottom.isNotEmpty() && this.listOfNumbersRightTop.isNotEmpty()) {
                this.centerRightUp!!.text = this.listOfNumbersRightTop.first().toString()
                this.listOfNumbersRightTop = this.listOfNumbersRightTop.drop(1)
                this.centerRightLow!!.text = this.listOfNumbersRightBottom.first().toString()
                this.listOfNumbersRightBottom = this.listOfNumbersRightBottom.drop(1)
            }
        }
    }

    private fun changeCanvas() {
        runOnUiThread {
            val layoutParams = this.centerLayout!!.layoutParams as RelativeLayout.LayoutParams
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
            this.centerLayout!!.layoutParams = layoutParams
        }
    }

    private fun endScenario() {
        super.dealWithScenarioEnd()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.initPreferences()
        this.addClientListener(this.defaultClientListener)
        // Set layout
        setContentView(R.layout.activity_calibration)
        //TODO get device and set background
        this.background = this@CalibrationActivity.findViewById(R.id.background) as ImageView

        this.centerRightLow = this@CalibrationActivity.findViewById(R.id.centerRightLow) as TextView
        this.centerLeftLow = this@CalibrationActivity.findViewById(R.id.centerLeftLow) as TextView
        this.centerRightUp = this@CalibrationActivity.findViewById(R.id.centerRightUp) as TextView
        this.centerLeftUp = this@CalibrationActivity.findViewById(R.id.centerLeftUp) as TextView
        this.whiteRectangle = this@CalibrationActivity.findViewById(R.id.whiteRectangle) as ImageView
        this.centerLayout = this@CalibrationActivity.findViewById(R.id.centerLayout) as RelativeLayout
        changeNumbers()
    }


    override fun onStart() {
        super.onStart()
        this.addClientListener(this.defaultClientListener)
        this.bindAndStartNetworkinService()
    }


    override fun onStop() {
        super.onStop()
        this.removeClientListener(this.defaultClientListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        this.unbindNetworkinService()
    }

}
