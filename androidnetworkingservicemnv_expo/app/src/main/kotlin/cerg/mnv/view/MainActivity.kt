package cerg.mnv.view


import android.content.Intent
import android.content.res.Configuration
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import cerg.mnv.controller.AuditoryDisplayController
import cerg.mnv.controller.VisualDisplayController
import cerg.mnv.model.MultiPatientData
import cerg.mnv.model.Patient
import cerg.mnv.model.VitalSign
import cerg.mnv.R
import cerg.mnv.model.Context
import mpmgame.*
import networking.ClientListener
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AbstractServiceView() {
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        this.visualDisplayController?.refreshDisplay()
    }

    // AuditoryDisplayController
    private var auditoryDisplayController: AuditoryDisplayController? = null
    private  var visualDisplayController: VisualDisplayController? = null

    private val defaultClientListener = object : ClientListener {
        override fun onClientMessage(message: String) {
            val jsonObject = JSONObject(message)
            val messageLevel by lazy {
                try {
                    if (jsonObject.getString("type") == "error") MessageLevel.ERROR else MessageLevel.INFO
                } catch (ex : JSONException) {
                    MessageLevel.INFO
                }
            }
            if (messageLevel == MessageLevel.ERROR) Log.e("MainActivity", message)
            else Log.i("MainActivity", message)
        }
        override fun onServerDisconnect() {
            Log.i("MainActivity", "Disconnected from Server")
            this@MainActivity.stopAllSoundActivities()
            this@MainActivity.endScenario()
        }
        override fun onServerMessage(message: String) {
            when (val mpmMessage = this@MainActivity.processServerMessage(message)) {
                is MpmGameMessageError -> {
                    Log.e("MainActivity", "Mpm Game Message is erroneous: $mpmMessage")
                }
                is MultiPatient -> { this@MainActivity.visualDisplayController?.refreshDisplay() }
                is SimulationStateChange ->
                    if (mpmMessage.state == SimulationState.ENDED) this@MainActivity.endScenario()
                }
        }
        override fun onServerConnect() {
            Log.i("MainActivity", "Connected to Server")
            this@MainActivity.auditoryDisplayController?.start()
        }
    }


    private fun endScenario() {
        super.dealWithScenarioEnd()
        this.initData()
        this.auditoryDisplayController?.stop()
        this.visualDisplayController?.refreshDisplay()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.initPreferences()
        this.addClientListener(this.defaultClientListener)

        // Set layout
        setContentView(R.layout.activity_main)

        // Create dummy data (only if we haven't received patient data yet)
        if (!Context.patientsInitialized) this.initData()

        // Create system controller
        auditoryDisplayController = AuditoryDisplayController.createInstance(this, MultiPatientData)
        //visualDisplayController = VisualDisplayController.createInstance(this, MultiPatientData)

        cerg.mnv.utils.setSoundLevels(
                getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager, 100)
    }

    private fun initData() {
        val mp = MultiPatientData
        mp.clearPatients()

        for (i in 0 until 6) {
            val p = Patient(i, "P $i")
            for ((vId, value) in arrayOf("BP", "HR", "SPO2").withIndex()) {
                val v = VitalSign(vId, value)
                v.value = -1
                p.addVitalSign(v)
            }
            mp.addPatient(p)
        }
    }

    override fun onStart() {
        super.onStart()

        this.auditoryDisplayController?.start()

        this.addClientListener(this.defaultClientListener)
        this.bindAndStartNetworkinService()

        this.checkForSoundDownloads()
    }

    override fun onStop() {
        super.onStop()

        this.stopCheckingForSoundDownloads()
        this.auditoryDisplayController?.stop()

        this.removeClientListener(this.defaultClientListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        this.unbindNetworkinService()
    }


}
