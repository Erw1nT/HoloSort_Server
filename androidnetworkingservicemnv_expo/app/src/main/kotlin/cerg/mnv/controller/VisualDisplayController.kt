package cerg.mnv.controller

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import cerg.mnv.layout.PatientLayout
import cerg.mnv.model.MultiPatientData
import cerg.mnv.model.MultiPatientDataListener
import cerg.mnv.model.Patient
import cerg.mnv.model.VitalSign
import cerg.mnv.R
import org.w3c.dom.Text
import java.sql.Timestamp
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.timer

class VisualDisplayController private constructor(private var activity: Activity) {
    private var context: Context? = null
    private var inflater: LayoutInflater? = null

    private var patientColumnsLinearLayout: LinearLayout? = null
    private var vitalSignNamesLinearLayout: LinearLayout? = null

    private var equation: TextView? = null

    init {
        // this.mpData.addListener(this)
        this.initWithActivity(this.activity)
    }

    fun initWithActivity(newActivity: Activity) {
        this.activity = newActivity
        //this.context = this.activity.applicationContext
        //this.inflater = LayoutInflater.from(activity.applicationContext)
        this.initView()
        this.createDisplay()
    }

    // Listener
/*
    override fun patientChanged(patient: Patient) {}

    override fun patientLevelChanged(patient: Patient) {}

    override fun freezeOccured() {
        this.activity.runOnUiThread {
            if (this.mpData.isVisualEnabled) {
                this.activity.findViewById(R.id.layout).visibility = View.INVISIBLE
            }
        }
    }

    override fun unfreezeOccured() {
        this.activity.runOnUiThread {
            if (this.mpData.isVisualEnabled) {
                this.activity.findViewById(R.id.layout).visibility = View.VISIBLE
            }
        }
    }

*/

    fun scenarioStarted() {
        this.cleanDisplay()
        this.activity.runOnUiThread {
            this.activity.findViewById(R.id.flashView).visibility = View.VISIBLE

            //  Thread.sleep(2_000)
            //this.activity.findViewById(R.id.flashView).visibility = View.INVISIBLE
            this.activity.findViewById(R.id.arithTextView).visibility = View.VISIBLE
            equation = this.activity.findViewById(R.id.arithTextView) as TextView
            equation!!.text = "7+4"
            equation!!.setTextColor(Color.RED)
            // Thread.sleep(3_000)
            equation!!.text = "5+6"

        }

    }
/*
        if (this.mpData.isVisualEnabled) {
            this.cleanDisplay()
            this.createDisplay()
            this.activity.findViewById(R.id.layout).visibility = View.VISIBLE
        } else {
            this.activity.findViewById(R.id.layout).visibility = View.INVISIBLE
        }

 */


    fun scenarioStopped() {
        this.activity.findViewById(R.id.layout).visibility = View.INVISIBLE
        this.cleanDisplay()
        this.createDisplay()
    }

    // Methods
    private fun initView() {
        //this.patientColumnsLinearLayout = this.activity.findViewById(R.id.patientColumsLinearLayout) as LinearLayout
        //this.vitalSignNamesLinearLayout = this.activity.findViewById(R.id.vitalSignNamesLinearLayout) as LinearLayout
        this.activity.findViewById(R.id.layout).visibility = View.VISIBLE

    }

    private fun cleanDisplay() {
        this.activity.runOnUiThread {
            // this.patientColumnsLinearLayout?.removeAllViews()
            //this.vitalSignNamesLinearLayout?.removeAllViews()
            this.activity.findViewById(R.id.flashView).visibility = View.INVISIBLE
            this.activity.findViewById(R.id.arithTextView).visibility = View.INVISIBLE
            this.activity.findViewById(R.id.layout).visibility = View.INVISIBLE
        }
    }

    private fun createDisplay() {
        this.activity.runOnUiThread {

            this.activity.findViewById(R.id.layout).visibility = View.VISIBLE
            this.activity.findViewById(R.id.arithTextView).visibility = View.VISIBLE
            this.activity.findViewById(R.id.flashView).visibility = View.VISIBLE
            equation = this.activity.findViewById(R.id.arithTextView) as TextView

            timeEquation()
            val timer = Timer()
            timer.scheduleAtFixedRate(
                    object  : TimerTask() {
                        override fun run() {
                            equation!!.text = "2+2"
                        }
                    }, 1000, 1
            )
            timer.schedule (object  : TimerTask(){
                override fun run() = timer.cancel()
            },1000)

        }
    }

    fun timeEquation(){

    }
    fun refreshDisplay() {
        this.cleanDisplay()
        this.createDisplay()
    }

    private fun determineBackgroundColor(patient: Patient): Int {
        return if (MultiPatientData.getPatients().indexOf(patient) % 2 == 0) {
            this.activity.resources.getColor(R.color.grey, null)
        } else {
            this.activity.resources.getColor(R.color.darkgrey, null)
        }
    }

    private fun determineVitalSignTextColor(vitalSign: VitalSign): Int {
        when (vitalSign.name) {
            "HR" -> return activity.resources.getColor(R.color.hr, null)
            "SPO2" -> return activity.resources.getColor(R.color.spo2, null)
            "BP" -> return activity.resources.getColor(R.color.bp, null)
            "ETCO2" -> return activity.resources.getColor(R.color.etco2, null)
            "TEMP" -> return activity.resources.getColor(R.color.temp, null)
            "RESP" -> return activity.resources.getColor(R.color.resp, null)
        }
        return Color.WHITE
    }

    companion object {

        private var instance: VisualDisplayController? = null

        fun createInstance(activity: Activity): VisualDisplayController {
            if (VisualDisplayController.instance == null) {
                VisualDisplayController.instance = VisualDisplayController(activity)
            } else VisualDisplayController.instance?.initWithActivity(activity)
            return VisualDisplayController.instance!!
        }
    }
}
