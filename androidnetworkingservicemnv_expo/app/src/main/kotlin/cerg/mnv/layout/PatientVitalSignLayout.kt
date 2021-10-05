package cerg.mnv.layout

import android.app.Activity
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import cerg.mnv.model.VitalSign
import cerg.mnv.model.VitalSignListener
import cerg.mnv.R

class PatientVitalSignLayout : FrameLayout, VitalSignListener {

    private var activity: Activity? = null
    private var inflater: LayoutInflater? = null
    private var vitalSign: VitalSign

    private var vitalSignBoundVH: TextView? = null
    private var vitalSignBoundH: TextView? = null
    private var vitalSignBoundN: TextView? = null
    private var vitalSignBoundL: TextView? = null
    private var vitalSignBoundVL: TextView? = null

    private var vitalSignValue: TextView? = null

    private var patientVitalSignView: View? = null

    // Constructor
    constructor(activity: Activity, vitalSign: VitalSign) : super(activity.applicationContext) {
        this.activity = activity
        this.vitalSign = vitalSign
        vitalSign.addListener(this)
        this.inflater = LayoutInflater.from(activity.applicationContext)
        initView()
    }

    constructor(activity: Activity, attrs: AttributeSet, vitalSign: VitalSign) : super(activity.applicationContext, attrs) {
        this.activity = activity
        this.vitalSign = vitalSign
        vitalSign.addListener(this)
        this.inflater = LayoutInflater.from(activity.applicationContext)
        initView()
    }

    constructor(activity: Activity, attrs: AttributeSet, defStyleAttr: Int, vitalSign: VitalSign) : super(activity.applicationContext, attrs, defStyleAttr) {
        this.activity = activity
        this.vitalSign = vitalSign
        vitalSign.addListener(this)
        this.inflater = LayoutInflater.from(activity.applicationContext)
        initView()
    }

    // Listener
    override fun vitalSignChanged(vitalSign: VitalSign) {
        val value = vitalSign.value
        activity!!.runOnUiThread {
            vitalSignValue = patientVitalSignView!!.findViewById(R.id.vitalSignValue) as TextView
            vitalSignValue!!.text = Integer.toString(value)
        }
    }

    override fun vitalSignLevelChanged(vitalSign: VitalSign) {
        activity!!.runOnUiThread { updateLevelView() }
    }

    // Methods
    private fun initView() {
        // Vital sign value
        patientVitalSignView = inflater!!.inflate(R.layout.patient_vitalsign, this, true)
        vitalSignValue = patientVitalSignView!!.findViewById(R.id.vitalSignValue) as TextView

        //Vital sign names
        // TextView gap = (TextView) patientVitalSignView.findViewById(R.id.gap);
        // TextView vitalSignName = (TextView) patientVitalSignView.findViewById(R.id.vitalSignName);

        // Vital sign Boundary views
        vitalSignBoundVH = patientVitalSignView!!.findViewById(R.id.vitalSignVH) as TextView
        vitalSignBoundH = patientVitalSignView!!.findViewById(R.id.vitalSignH) as TextView
        vitalSignBoundL = patientVitalSignView!!.findViewById(R.id.vitalSignL) as TextView
        vitalSignBoundVL = patientVitalSignView!!.findViewById(R.id.vitalSignVL) as TextView
        vitalSignBoundN = patientVitalSignView!!.findViewById(R.id.vitalSignN) as TextView

        //set current value
        vitalSignValue!!.text = Integer.toString(vitalSign.value)
        vitalSignValue!!.setTextColor(determineVitalSignColor(vitalSign))

        //set Text and Textcolors respective to patient thresholds and to respective vital sign
        vitalSignBoundVH!!.text = Integer.toString(vitalSign.highAlarm)
        vitalSignBoundVH!!.setTextColor(determineVitalSignColor(vitalSign))
        vitalSignBoundH!!.text = Integer.toString(vitalSign.highWarning)
        vitalSignBoundH!!.setTextColor(determineVitalSignColor(vitalSign))
        vitalSignBoundL!!.text = Integer.toString(vitalSign.lowWarning)
        vitalSignBoundL!!.setTextColor(determineVitalSignColor(vitalSign))
        vitalSignBoundVL!!.text = Integer.toString(vitalSign.lowAlarm)
        vitalSignBoundVL!!.setTextColor(determineVitalSignColor(vitalSign))

        //set background colors of level view
        vitalSignBoundVL!!.setBackgroundColor(Color.TRANSPARENT)
        vitalSignBoundL!!.setBackgroundColor(Color.TRANSPARENT)
        vitalSignBoundN!!.setBackgroundColor(Color.TRANSPARENT)
        vitalSignBoundH!!.setBackgroundColor(Color.TRANSPARENT)
        vitalSignBoundVH!!.setBackgroundColor(Color.TRANSPARENT)

        //set background color of vital sign names
        //gap.setBackgroundColor(Color.TRANSPARENT);
        //vitalSignName.setBackgroundColor(Color.TRANSPARENT);

        //update LevelView
        updateLevelView()
    }

    private fun updateLevelView() {
        when (vitalSign.level) {
            VitalSign.Level.NORMAL -> {
                vitalSignBoundVH!!.setBackgroundColor(Color.TRANSPARENT)
                vitalSignBoundVH!!.setTextColor(determineVitalSignColor(vitalSign))
                vitalSignBoundH!!.setBackgroundColor(Color.TRANSPARENT)
                vitalSignBoundH!!.setTextColor(determineVitalSignColor(vitalSign))
                vitalSignBoundN!!.setBackgroundColor(Color.TRANSPARENT)
                vitalSignBoundL!!.setBackgroundColor(Color.TRANSPARENT)
                vitalSignBoundL!!.setTextColor(determineVitalSignColor(vitalSign))
                vitalSignBoundVL!!.setBackgroundColor(Color.TRANSPARENT)
                vitalSignBoundVL!!.setTextColor(determineVitalSignColor(vitalSign))

                vitalSignValue!!.setBackgroundColor(Color.TRANSPARENT)
                vitalSignValue!!.setTextColor(determineVitalSignColor(vitalSign))
            }
            VitalSign.Level.VERY_HIGH -> {
                vitalSignBoundVH!!.setBackgroundColor(determineVitalSignColor(vitalSign))
                vitalSignBoundVH!!.setTextColor(Color.BLACK)
                vitalSignBoundH!!.setBackgroundColor(Color.TRANSPARENT)
                vitalSignBoundH!!.setTextColor(determineVitalSignColor(vitalSign))
                vitalSignBoundN!!.setBackgroundColor(Color.TRANSPARENT)
                vitalSignBoundL!!.setBackgroundColor(Color.TRANSPARENT)
                vitalSignBoundL!!.setTextColor(determineVitalSignColor(vitalSign))
                vitalSignBoundVL!!.setBackgroundColor(Color.TRANSPARENT)
                vitalSignBoundVL!!.setTextColor(determineVitalSignColor(vitalSign))

                vitalSignValue!!.setBackgroundColor(determineVitalSignColor(vitalSign))
                vitalSignValue!!.setTextColor(Color.BLACK)
            }
            VitalSign.Level.HIGH -> {
                vitalSignBoundVH!!.setBackgroundColor(Color.TRANSPARENT)
                vitalSignBoundVH!!.setTextColor(determineVitalSignColor(vitalSign))
                vitalSignBoundH!!.setBackgroundColor(determineVitalSignColor(vitalSign))
                vitalSignBoundH!!.setTextColor(Color.BLACK)
                vitalSignBoundN!!.setBackgroundColor(Color.TRANSPARENT)
                vitalSignBoundL!!.setBackgroundColor(Color.TRANSPARENT)
                vitalSignBoundL!!.setTextColor(determineVitalSignColor(vitalSign))
                vitalSignBoundVL!!.setBackgroundColor(Color.TRANSPARENT)
                vitalSignBoundVL!!.setTextColor(determineVitalSignColor(vitalSign))

                vitalSignValue!!.setBackgroundColor(determineVitalSignColor(vitalSign))
                vitalSignValue!!.setTextColor(Color.BLACK)
            }
            VitalSign.Level.LOW -> {
                vitalSignBoundVH!!.setBackgroundColor(Color.TRANSPARENT)
                vitalSignBoundVH!!.setTextColor(determineVitalSignColor(vitalSign))
                vitalSignBoundH!!.setBackgroundColor(Color.TRANSPARENT)
                vitalSignBoundH!!.setTextColor(determineVitalSignColor(vitalSign))
                vitalSignBoundN!!.setBackgroundColor(Color.TRANSPARENT)
                vitalSignBoundL!!.setBackgroundColor(determineVitalSignColor(vitalSign))
                vitalSignBoundL!!.setTextColor(Color.BLACK)
                vitalSignBoundVL!!.setBackgroundColor(Color.TRANSPARENT)
                vitalSignBoundVL!!.setTextColor(determineVitalSignColor(vitalSign))

                vitalSignValue!!.setBackgroundColor(determineVitalSignColor(vitalSign))
                vitalSignValue!!.setTextColor(Color.BLACK)
            }
            VitalSign.Level.VERY_LOW -> {
                vitalSignBoundVH!!.setBackgroundColor(Color.TRANSPARENT)
                vitalSignBoundVH!!.setTextColor(determineVitalSignColor(vitalSign))
                vitalSignBoundH!!.setBackgroundColor(Color.TRANSPARENT)
                vitalSignBoundH!!.setTextColor(determineVitalSignColor(vitalSign))
                vitalSignBoundN!!.setBackgroundColor(Color.TRANSPARENT)
                vitalSignBoundL!!.setBackgroundColor(Color.TRANSPARENT)
                vitalSignBoundL!!.setTextColor(determineVitalSignColor(vitalSign))
                vitalSignBoundVL!!.setBackgroundColor(determineVitalSignColor(vitalSign))
                vitalSignBoundVL!!.setTextColor(Color.BLACK)

                vitalSignValue!!.setBackgroundColor(determineVitalSignColor(vitalSign))
                vitalSignValue!!.setTextColor(Color.BLACK)
            }
            VitalSign.Level.NOT_VALID -> {
                vitalSignValue!!.setBackgroundColor(Color.RED)
                vitalSignValue!!.setTextColor(determineVitalSignColor(vitalSign))
            }
            VitalSign.Level.UNINITIALIZED -> {
                vitalSignValue!!.setBackgroundColor(Color.TRANSPARENT)
                vitalSignValue!!.setTextColor(Color.WHITE)
                vitalSignValue!!.text = "--"
                vitalSignBoundVH!!.setBackgroundColor(Color.TRANSPARENT)
                vitalSignBoundVH!!.setTextColor(Color.WHITE)
                vitalSignBoundVH!!.text = ""
                vitalSignBoundH!!.setBackgroundColor(Color.TRANSPARENT)
                vitalSignBoundH!!.setTextColor(Color.WHITE)
                vitalSignBoundH!!.text = ""
                vitalSignBoundN!!.setBackgroundColor(Color.TRANSPARENT)
                vitalSignBoundN!!.setTextColor(Color.WHITE)
                vitalSignBoundN!!.text = ""
                vitalSignBoundL!!.setBackgroundColor(Color.TRANSPARENT)
                vitalSignBoundL!!.setTextColor(Color.WHITE)
                vitalSignBoundL!!.text = ""
                vitalSignBoundVL!!.setBackgroundColor(Color.TRANSPARENT)
                vitalSignBoundVL!!.setTextColor(Color.WHITE)
                vitalSignBoundVL!!.text = ""
            }
        }
    }

    private fun determineVitalSignColor(vitalSign: VitalSign): Int {
        when (vitalSign.name) {
            "HR" -> return resources.getColor(R.color.hr, null)
            "SPO2" -> return resources.getColor(R.color.spo2, null)
            "BP" -> return activity!!.resources.getColor(R.color.bp, null)
            "ETCO2" -> return activity!!.resources.getColor(R.color.etco2, null)
            "TEMP" -> return activity!!.resources.getColor(R.color.temp, null)
            "RESP" -> return activity!!.resources.getColor(R.color.resp, null)
        }
        return Color.WHITE
    }
}
