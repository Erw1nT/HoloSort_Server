package cerg.mnv.layout

import android.app.Activity
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import cerg.mnv.model.Patient
import cerg.mnv.model.PatientListener
import cerg.mnv.model.VitalSign
import cerg.mnv.R

class PatientLayout : LinearLayout, PatientListener {

    private var activity: Activity
    private var inflater: LayoutInflater
    private var patient: Patient

    private var patientHeaderView: View? = null

    // Constructor
    constructor(activity: Activity, patient: Patient) : super(activity.applicationContext) {
        this.activity = activity
        this.inflater = LayoutInflater.from(activity.applicationContext)
        this.patient = patient

        initView()
    }

    constructor(activity: Activity, attrs: AttributeSet, patient: Patient) : super(activity.applicationContext, attrs) {
        this.activity = activity
        this.inflater = LayoutInflater.from(activity.applicationContext)
        this.patient = patient

        initView()
    }

    constructor(activity: Activity, attrs: AttributeSet, defStyleAttr: Int, patient: Patient) : super(activity.applicationContext, attrs, defStyleAttr) {
        this.activity = activity
        this.inflater = LayoutInflater.from(activity.applicationContext)
        this.patient = patient

        initView()
    }

    // Listener to Patient model class
    override fun notifyVitalSignChanged(patient: Patient, vitalSign: VitalSign) {}

    override fun notifyLevelVitalSignChanged(patient: Patient, vitalSign: VitalSign) {}

    // Methods
    private fun initView() {
        // Set layout properties
        orientation = LinearLayout.VERTICAL
        //this.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));


        // Patient name
        patientHeaderView = inflater.inflate(R.layout.patient_header, this)
        val patientHeader = patientHeaderView!!.findViewById(R.id.patientHeader) as TextView
        patientHeader.setText(patient.name)
        patientHeader.setBackgroundColor(Color.TRANSPARENT)

        // Patient vital signs
        this.patient.getVitalSigns().forEach {
            val patientVitalSignLayout = PatientVitalSignLayout(activity, it)
            patientVitalSignLayout.setBackgroundColor(Color.TRANSPARENT)
            this.addView(patientVitalSignLayout)

        }
    }
}
