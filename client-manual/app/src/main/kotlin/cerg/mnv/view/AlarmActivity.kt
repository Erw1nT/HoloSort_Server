package cerg.mnv.view


import android.content.Intent
import android.content.res.Configuration
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TextView
import cerg.mnv.controller.AuditoryDisplayController
import cerg.mnv.controller.VisualDisplayController
import cerg.mnv.model.MultiPatientData
import cerg.mnv.model.Patient
import cerg.mnv.model.VitalSign
import cerg.mnv.R
import cerg.mnv.model.Context
import mpmgame.*
import networking.ClientListener
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.sql.Timestamp
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.schedule
import android.os.CountDownTimer
import android.support.v4.content.ContextCompat


class AlarmActivity : AbstractServiceView() {
    var equationList = listOf<String>()
    var typeAlarms = listOf<String>("bed", "heart", "oxygen")
    var criticalState = listOf<String>("critical", "veryCritical")
    var alarms = arrayListOf<JSONObject>()
    var alarm = JSONObject()

    private var patientView: LinearLayout? = null
    private var patientImageView: ImageView? = null
    private var flash: ImageView? = null
    private var background: ImageView? = null

    private var mediaPlayer: MediaPlayer? = null

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
            if (messageLevel == MessageLevel.ERROR) Log.e("AlarmActivity", message)
            else Log.i("AlarmActivity", message)
        }

        override fun onServerDisconnect() {
            Log.i("AlarmActivity", "Disconnected from Server")
            this@AlarmActivity.stopAllSoundActivities()
            this@AlarmActivity.endScenario()
        }

        override fun onServerMessage(message: String) {
            this@AlarmActivity.processServerMessage(message)
            val startTime: String = SimpleDateFormat("HH:mm:ss.SSS").format(Date())
            val json = JSONObject(message)
            if (json.has("type")) {
                if (json.get("type") == "frontend" && message.isNotEmpty()) {
                    if (json.has("dataType")) {
                        this@AlarmActivity.startActivity(Intent(this@AlarmActivity, CalibrationActivity::class.java))
                    } else {
                        val messageText = json.get("content").toString()
                        var interruptionLength = messageText.toLong().times(1000)
                        showFlash(true)

                        // mediaPlayer?.start()
                        val timer = Timer()
                        Timer().schedule(300) {
                            /*if(mediaPlayer != null){
                                mediaPlayer?.stop()
                            }*/
                            // mediaPlayer?.seekTo(0)
                            showFlash(false)
                            setPatientViewVisible(true)

                            timer.scheduleAtFixedRate(
                                    object : TimerTask() {
                                        override fun run() {
                                            setAllInvisible()
                                            //Random Alarms
                                            /*
                                                var pNum = (1..6).shuffled().first()
                                                showAlarm(pNum, typeAlarms.random(), criticalState.random())
                                             */
                                            //Fixed Alarms
                                            if (alarms.isNotEmpty()) {
                                                alarm = alarms.first()
                                                println(alarm.toString())
                                                showAlarm(alarm.getInt("patientNumber"), alarm.getString("typeOfAlarm"), alarm.getString("criticalState"))
                                                alarms.remove(alarm)
                                            } else {
                                                Log.e("AlarmActivity", "Patient List is empty")
                                            }
                                        }
                                    }, 10, 5000
                            )
                        }
                        Timer().schedule(interruptionLength + 300) {
                            timer.cancel()
                            val endTime: String = SimpleDateFormat("HH:mm:ss.SSS").format(Date())
                            val obj = JSONObject()
                            obj.put("startTime", startTime)
                            obj.put("endTime", endTime)
                            this@AlarmActivity.sendBackEndMessage(obj)
                            setAllInvisible()
                            setPatientViewVisible(false)
                            showFlash(true)
                            //mediaPlayer?.start()
                            Timer().schedule(300) {
                                /*if(mediaPlayer != null){
                                    mediaPlayer?.stop()
                                }
                                mediaPlayer?.seekTo(0)*/
                                showFlash(false)
                            }
                        }
                    }
                }
            }
        }


        override fun onServerConnect() {
            Log.i("AlarmActivity", "Connected to Server")
        }
    }


    private fun endScenario() {
        super.dealWithScenarioEnd()
        this.initData()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.initPreferences()
        this.addClientListener(this.defaultClientListener)

        // Set layout
        setContentView(R.layout.activity_alarm)
        //TODO get device and set background
        this.background = this@AlarmActivity.findViewById(R.id.background) as ImageView

        this.patientView = this@AlarmActivity.findViewById(R.id.patientView) as LinearLayout
        this.flash = this@AlarmActivity.findViewById(R.id.flashView) as ImageView
        //mediaPlayer = MediaPlayer.create(this, R.raw.alerttone2)

        //mediaPlayer?.setOnPreparedListener{ }

    }

    fun setAllInvisible() {
        runOnUiThread {
            this@AlarmActivity.findViewById(R.id.patient1)!!.visibility = View.INVISIBLE
            this@AlarmActivity.findViewById(R.id.patient2)!!.visibility = View.INVISIBLE
            this@AlarmActivity.findViewById(R.id.patient3)!!.visibility = View.INVISIBLE
            this@AlarmActivity.findViewById(R.id.patient4)!!.visibility = View.INVISIBLE
            this@AlarmActivity.findViewById(R.id.patient5)!!.visibility = View.INVISIBLE
            this@AlarmActivity.findViewById(R.id.patient6)!!.visibility = View.INVISIBLE
        }
    }

    fun showAlarm(patientNumber: Int, typeOfAlarm: String, criticalState: String) {
        runOnUiThread {
            this.patientImageView = when (patientNumber) {
                1 -> this@AlarmActivity.findViewById(R.id.patient1) as ImageView
                2 -> this@AlarmActivity.findViewById(R.id.patient2) as ImageView
                3 -> this@AlarmActivity.findViewById(R.id.patient3) as ImageView
                4 -> this@AlarmActivity.findViewById(R.id.patient4) as ImageView
                5 -> this@AlarmActivity.findViewById(R.id.patient5) as ImageView
                6 -> this@AlarmActivity.findViewById(R.id.patient6) as ImageView
                else -> null
            }
        this.patientImageView!!.visibility = View.VISIBLE

                when (typeOfAlarm) {
                    "bed" -> this.patientImageView!!.setImageDrawable(ContextCompat.getDrawable(this@AlarmActivity, R.drawable.bed))
                    "heart" -> this.patientImageView!!.setImageDrawable(ContextCompat.getDrawable(this@AlarmActivity, R.drawable.heart))
                    "oxygen" -> this.patientImageView!!.setImageDrawable(ContextCompat.getDrawable(this@AlarmActivity, R.mipmap.oxygenimage))
                    else -> Log.e("ArithmeticActivity", "Wrong AlarmType")
                }

           when (criticalState) {
                "critical" -> this.patientImageView!!.setColorFilter(ContextCompat.getColor(this@AlarmActivity, R.color.critical), android.graphics.PorterDuff.Mode.MULTIPLY)
                "veryCritical" -> this.patientImageView!!.setColorFilter( ContextCompat.getColor(this@AlarmActivity, R.color.veryCritical), android.graphics.PorterDuff.Mode.MULTIPLY)
            }


        }
    }

    fun setPatientViewVisible(shouldBeVisible: Boolean) {
        runOnUiThread {
            if (shouldBeVisible) {
                this.patientView!!.visibility = View.VISIBLE
            } else {
                this.patientView!!.visibility = View.INVISIBLE
            }
        }
    }

    fun showFlash(shouldBeVisible: Boolean) {
        runOnUiThread {
            if (shouldBeVisible) {
                this.flash!!.visibility = View.VISIBLE
            } else {
                this.flash!!.visibility = View.INVISIBLE
            }
        }
    }

    private fun initData() {

    }

    override fun onStart() {
        super.onStart()
        // this.auditoryDisplayController?.start()
        this.addClientListener(this.defaultClientListener)
        this.bindAndStartNetworkinService()

        alarms.clear()
        val howOftenOpendIT = this@AlarmActivity.getPreference("numberOpendIT")?.toInt()
        println(howOftenOpendIT)
        when (howOftenOpendIT) {
            1 -> createFixedAlarms1()
            2 -> createFixedAlarms2()
            3 -> createFixedAlarms3()
            else -> createFixedAlarms2()
        }

    }

    override fun onStop() {
        super.onStop()

        //this.mediaPlayer?.stop()
        this.removeClientListener(this.defaultClientListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        //this.mediaPlayer?.release()
        this.unbindNetworkinService()
    }




    private fun createFixedAlarms1() {
        alarms.add(JSONObject("""{"patientNumber": 1, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "oxygen","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2,"typeOfAlarm": "bed","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "heart","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "bed","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2,"typeOfAlarm": "oxygen","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "heart","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "heart","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5,"typeOfAlarm": "bed","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "bed","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "oxygen","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2,"typeOfAlarm": "bed","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "oxygen","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4,"typeOfAlarm": "heart","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "oxygen","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "bed","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5,"typeOfAlarm": "bed","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4,"typeOfAlarm": "bed","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4,"typeOfAlarm": "heart","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "heart","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4,"typeOfAlarm": "bed","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2,"typeOfAlarm": "oxygen","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "bed","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2,"typeOfAlarm": "oxygen","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "bed","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "bed","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "bed","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "bed","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5,"typeOfAlarm": "heart","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5,"typeOfAlarm": "heart","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4,"typeOfAlarm": "bed","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "bed","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "bed","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4,"typeOfAlarm": "heart","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2,"typeOfAlarm": "heart","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "heart","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5,"typeOfAlarm": "heart","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "bed","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5,"typeOfAlarm": "heart","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2,"typeOfAlarm": "bed","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "bed","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "oxygen","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5,"typeOfAlarm": "oxygen","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2,"typeOfAlarm": "bed","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "oxygen","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "bed","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "heart","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "heart","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "bed","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "heart","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "heart","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "bed","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "oxygen","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4,"typeOfAlarm": "oxygen","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5,"typeOfAlarm": "bed","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "bed","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4,"typeOfAlarm": "bed","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "bed","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2,"typeOfAlarm": "heart","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "bed","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "bed","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "bed","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4,"typeOfAlarm": "bed","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "heart","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "bed","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4,"typeOfAlarm": "bed","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2,"typeOfAlarm": "bed","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "heart","criticalState": "critical"}"""))
    }

    private fun createFixedAlarms2() {
        alarms.add(JSONObject("""{"patientNumber": 1, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "oxygen", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1, "typeOfAlarm": "oxygen", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "bed","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "heart","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "bed","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "oxygen","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2,"typeOfAlarm": "bed","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "oxygen","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4,"typeOfAlarm": "heart","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "oxygen","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "bed","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5,"typeOfAlarm": "bed","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4,"typeOfAlarm": "bed","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1, "typeOfAlarm": "oxygen", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "bed", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "bed", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "bed", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "oxygen", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1, "typeOfAlarm": "bed", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "heart", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "oxygen", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1, "typeOfAlarm": "oxygen", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1, "typeOfAlarm": "oxygen", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "bed", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "bed", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "bed", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "oxygen", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1, "typeOfAlarm": "bed", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "heart", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "oxygen", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1, "typeOfAlarm": "oxygen", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1, "typeOfAlarm": "oxygen", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "bed", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "bed", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "bed", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "oxygen", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1, "typeOfAlarm": "bed", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "heart", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "bed", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "veryCritical"}"""))
    }

    private fun createFixedAlarms3() {
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "oxygen", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1, "typeOfAlarm": "oxygen", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "bed","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "oxygen","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2,"typeOfAlarm": "bed","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "oxygen","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4,"typeOfAlarm": "heart","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "oxygen","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1, "typeOfAlarm": "oxygen", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "oxygen", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1, "typeOfAlarm": "oxygen", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1, "typeOfAlarm": "oxygen", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "bed", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "bed", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "bed", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "oxygen", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1, "typeOfAlarm": "bed", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "heart", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "oxygen", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1, "typeOfAlarm": "oxygen", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1, "typeOfAlarm": "oxygen", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "bed", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "bed", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "bed", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "oxygen", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1, "typeOfAlarm": "bed", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "heart", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "oxygen", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1, "typeOfAlarm": "oxygen", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "heart","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "bed","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "heart","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "heart","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "bed","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1,"typeOfAlarm": "oxygen","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4,"typeOfAlarm": "oxygen","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4,"typeOfAlarm": "oxygen","criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5,"typeOfAlarm": "bed","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "heart","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6,"typeOfAlarm": "bed","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4,"typeOfAlarm": "bed","criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 2, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 1, "typeOfAlarm": "bed", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 5, "typeOfAlarm": "heart", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 6, "typeOfAlarm": "heart", "criticalState": "veryCritical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 4, "typeOfAlarm": "oxygen", "criticalState": "critical"}"""))
        alarms.add(JSONObject("""{"patientNumber": 3, "typeOfAlarm": "bed", "criticalState": "veryCritical"}"""))
    }

}