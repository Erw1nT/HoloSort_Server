package cerg.mnv.view


import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import cerg.mnv.model.MultiPatientData
import cerg.mnv.model.Patient
import cerg.mnv.model.VitalSign
import cerg.mnv.R
import networking.ClientListener
import org.json.JSONException
import org.json.JSONObject
import java.sql.Timestamp
import java.util.*
import kotlin.concurrent.schedule

class ArithmeticActivity : AbstractServiceView() {
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
    }


    var equationList = listOf<String>()
    var training = false
    private var equation: TextView? = null
    private var flash: ImageView? = null
    private var background: ImageView? = null

    private var answerButton1: Button? = null
    private var answerButton2: Button? = null
    private var answerButton3: Button? = null
    private var answerButton4: Button? = null

    private var equationIndex: Int = 0
    private var currentAnswer: Int = 0

    private var wasEquationAnswered: Boolean? = null
    private var errorCount: Int = 0

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
            if (messageLevel == MessageLevel.ERROR) Log.e("ArithmeticActivity", message)
            else Log.i("ArithmeticActivity", message)
        }

        override fun onServerDisconnect() {
            Log.i("ArithmeticActivity", "Disconnected from Server")
            this@ArithmeticActivity.endScenario()
        }

        override fun onServerMessage(message: String) {
            this@ArithmeticActivity.processServerMessage(message)

            val json = JSONObject(message)
            if (json.get("type") == "frontend" && message.isNotEmpty()) {

                if (json.has("dataType")) {
                    println(json.get("dataType"))

                    if (json.get("dataType") == "endTraining") {
                        this@ArithmeticActivity.setPreference("Training", "false")
                        equationList = equationsEasy2
                    }
                    else {
                        this@ArithmeticActivity.setPreference("Training", "false")
                        this@ArithmeticActivity.startActivity(Intent(this@ArithmeticActivity, CalibrationActivity::class.java))
                    }
                }
                else {
                    val messageText = json.get("content").toString()
                    val interruptionLength = messageText.toLong().times(1000)
                    showFlash(true)

                    val timer = Timer()
                    Timer().schedule(300) {
                        showFlash(false)
                        setTextVisible(true)

                        timer.scheduleAtFixedRate(
                                object : TimerTask() {
                                    override fun run() {

                                        showEquation(equationList[equationIndex % equationList.count()])
                                        equationIndex++

                                    }
                                }, 10, 5000
                        )
                    }
                    Timer().schedule(interruptionLength) {
                        timer.cancel()
                        val time = Timestamp(System.currentTimeMillis())
                        this@ArithmeticActivity.sendBackEndMessage(time.toString())
                        setTextVisible(false)
                        showFlash(true)
                        Timer().schedule(300) {
                            showFlash(false)
                        }
                    }
                }
            }
        }

        override fun onServerConnect() {
            Log.i("ArithmeticActivity", "Connected to Server")
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
        setContentView(R.layout.activity_arith)

        //TODO get device and set background
        this.background = this@ArithmeticActivity.findViewById(R.id.background) as ImageView
        this.equation = this@ArithmeticActivity.findViewById(R.id.arithTextView) as TextView
        this.flash = this@ArithmeticActivity.findViewById(R.id.flashView) as ImageView

        this.answerButton1 = this@ArithmeticActivity.findViewById(R.id.answerButton1) as Button
        this.answerButton2 = this@ArithmeticActivity.findViewById(R.id.answerButton2) as Button
        this.answerButton3 = this@ArithmeticActivity.findViewById(R.id.answerButton3) as Button
        this.answerButton4 = this@ArithmeticActivity.findViewById(R.id.answerButton4) as Button

        this.answerButton1?.setOnClickListener { buttonHandler(this.answerButton1!!) }
        this.answerButton2?.setOnClickListener { buttonHandler(this.answerButton2!!) }
        this.answerButton3?.setOnClickListener { buttonHandler(this.answerButton3!!) }
        this.answerButton4?.setOnClickListener { buttonHandler(this.answerButton4!!) }

    }

    private fun buttonHandler(button: Button)
    {
        //prevent multiple presses
        if (wasEquationAnswered == true) return

        // if the button text does not match the currentAnswer, an error was made
        val buttonText = button.text.toString().toInt()
        if (buttonText != currentAnswer)
        {
            errorCount++
            wasEquationAnswered = true
        }
    }

    fun showEquation(equationString: String) {
        runOnUiThread {

            // if the previous equation has not been answered, an error was made
            if (wasEquationAnswered == false) {
                errorCount++
            }

            // the equation is parsed and the buttons texts are set
            val result = parseEquation(equationString)
            setAnswerButtonTexts(result)

            currentAnswer = result
            wasEquationAnswered = false

            if (this.equation != null) {
                this.equation!!.text = equationString
            }

        }
    }

    /**
     * Parses the equation string and return the result of the equation.
     */
    fun parseEquation(equationString: String) : Int {

        val isAddition = equationString.contains("+")
        val operator = if (isAddition) "+" else "-"

        val first = equationString.split(operator)[0].toInt()
        val second = equationString.split(operator)[1].toInt()

        val result = if (isAddition) first + second else first - second
        return result
    }

    /**
     * Sets the text on the answer buttons to the correct answer and 3 additional, randomly generated numbers.
     */
    private fun setAnswerButtonTexts(correctResult: Int) {

        val result = mutableListOf(correctResult)
        generateUniqueRandomNumbers(result, 4)
        result.shuffle()

        this.answerButton1?.text = result[0].toString()
        this.answerButton2?.text = result[1].toString()
        this.answerButton3?.text = result[2].toString()
        this.answerButton4?.text = result[3].toString()
    }

    /**
     * Takes a list of integers and generates as many unique, random integers as specified by amount and adds them to the list.
     * The random integers are in the range of [0, 30].
     */
    private fun generateUniqueRandomNumbers(result: MutableList<Int>, amount: Int) {

        if (amount <= result.count()) return

        while (true)
        {
            val randomNr = (0..30).random()
            if (result.contains(randomNr)) continue

            result.add(randomNr)
            if (result.count() == amount) break
        }

    }

    fun setTextVisible(shouldBeVisible: Boolean) {
        runOnUiThread {
            if (shouldBeVisible) {
                this.equation!!.visibility = View.VISIBLE
            } else {
                this.equation!!.visibility = View.INVISIBLE
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

        training = this@ArithmeticActivity.getPreference("Training")!!.toBoolean()

        if (training) {
            equationList = this.equationsEasy
        } else {
            equationList = this.equationsEasy2
        }

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


    //1-100
    val equationsEasy = listOf<String>("13-4",
        "14+10",
        "7+3",
        "16-9",
        "8-6",
        "13+2",
        "15+5",
        "8-4",
        "10-6",
        "15-6",
        "12+5",
        "5-1",
        "9-5",
        "14+9",
        "11-4",
        "12-3",
        "14-6",
        "13+7",
        "9-6",
        "6+5",
        "15+5",
        "14-4",
        "9-7",
        "7-2",
        "10+3",
        "13-1",
        "6+4",
        "15+7",
        "7-2",
        "5+7",
        "15+7",
        "6-3",
        "9+7",
        "7+7",
        "12+2",
        "14+9",
        "9-2",
        "9-5",
        "12-9",
        "14+10",
        "13+10",
        "10-1",
        "7-4",
        "11-9",
        "14+5",
        "14-9",
        "5+7",
        "13-4",
        "10+4",
        "7-5",
        "15-3",
        "13+4",
        "14-1",
        "13+8",
        "10-6",
        "8-5",
        "10+6",
        "10+8",
        "8+6",
        "15+9",
        "14+8",
        "10+10",
        "12-7",
        "7-3",
        "11+2",
        "10-6",
        "5-2",
        "6-3",
        "12-2",
        "7+9",
        "6-5",
        "11-4",
        "11-10",
        "11-8",
        "13-5",
        "10-5",
        "8+8",
        "11-4",
        "15+3",
        "11+4",
        "9+2",
        "8+9",
        "6-3",
        "14-7",
        "5+2",
        "7-4",
        "8-6",
        "10+2",
        "10-2",
        "13-10",
        "13+2",
        "12+4",
        "11+9",
        "6-6",
        "12-6",
        "11+1",
        "6-5",
        "10-3",
        "9-6",
        "10+1",
        "11+7",
        "15-7",
        "5+4",
        "14-9",
        "9+2",
        "7-2",
        "14-7",
        "12-3",
        "7-5",
        "9+8",
        "15+5",
        "6+5",
        "14+10",
        "10-8",
        "15-2",
        "11-9",
        "9-6",
        "5+4",
        "12-7",
        "14+9",
        "11-5",
        "6+4",
        "11+2",
        "12+6",
        "13+1",
        "15+7",
        "15+3",
        "9+7",
        "7+8",
        "11-6",
        "9-7",
        "13-5",
        "12-7",
        "9+4",
        "7+7",
        "15+5",
        "14-7",
        "13+3",
        "8+5",
        "7-4",
        "13+5",
        "6-5",
        "10-6",
        "10+8",
        "11-9",
        "11-1",
        "10-1",
        "6-3",
        "13+9",
        "13+8",
        "14-2",
        "10+2",
        "10-8",
        "9+1",
        "5+4",
        "11+9",
        "10-1",
        "13-7",
        "12-1",
        "7-1",
        "4-2",
        "6-3",
        "12-5",
        "7-5",
        "13-4",
        "11-5",
        "12-9",
        "14+8",
        "10-9",
        "15+10",
        "12+5",
        "6+9",
        "12-7",
        "5+6",
        "11+10",
        "8+10",
        "15-9",
        "13+10",
        "11-4",
        "5+5",
        "12+3",
        "9-3",
        "12-2",
        "10-7",
        "13-5",
        "8+1",
        "12-10",
        "13-9",
        "6-4",
        "5-4",
        "12+2",
        "10+3",
        "15+7",
        "13+8",
        "8-1",
        "14-2",
        "5+8",
        "9-3",
        "8+10",
        "6-3",
        "12+1",
        "14+12",
        "9-5",
        "9-1",
        "10-3",
        "7-2",
        "15-14",
        "11-2",
        "10+2",
        "12-4",
        "6-5",
        "14-9",
        "8-3",
        "8-5",
        "8+4",
        "9-7",
        "1+1",
        "11-5",
        "10+6",
        "12-1",
        "14-1",
        "13-12",
        "7-2",
        "15+8",
        "3-1",
        "15+7",
        "10+9",
        "13-4",
        "15+12",
        "12-6",
        "6+9",
        "14+13",
        "6-1",
        "8-4",
        "3+10",
        "6-3",
        "10+10",
        "8-5",
        "3+12",
        "7+13",
        "5+1",
        "7+12",
        "15-5",
        "9-6",
        "12-9",
        "15-10",
        "4+12",
        "11-7",
        "8+15",
        "2+1",
        "15+3",
        "5+7",
        "14-13",
        "12-6",
        "11+7",
        "15-2",
        "6+9",
        "9-7",
        "13+2",
        "5+10",
        "1+3",
        "10-5",
        "5+15",
        "11-9",
        "7+6",
        "13-2",
        "13-2",
        "6+7",
        "11-1",
        "6-1",
        "9+6",
        "13+4",
        "2+3",
        "9-2",
        "12-8",
        "15+10",
        "7-4",
        "13-5",
        "13-12",
        "11-6",
        "11+10",
        "9+3",
        "12-6",
        "2+10",
        "10-4",
        "1+4",
        "3+15",
        "15+14",
        "7+15",
        "12-6",
        "3+13",
        "14-12",
        "8+3",
        "14-1",
        "6+1",
        "15+12",
        "9+11",
        "8+10",
        "9+15",
        "15-2",
        "3+5",
        "4+13",
        "7+7",
        "8+15",
        "13-5",
        "13-8",
        "12+6",
        "12-5",
        "10-9",
        "5+7",
        "10+7",
        "11-9",
        "8-2",
        "14+15")
    //100-200
    val equationsEasy2 = listOf<String>("13-4",
            "14+10",
            "7+3",
            "16-9",
            "8-6",
            "13+2",
            "15+5",
            "8-4",
            "10-6",
            "15-6",
            "12+5",
            "5-1",
            "9-5",
            "14+9",
            "11-4",
            "12-3",
            "14-6",
            "13+7",
            "9-6",
            "6+5",
            "15+5",
            "14-4",
            "9-7",
            "7-2",
            "10+3",
            "13-1",
            "6+4",
            "15+7",
            "7-2",
            "5+7",
            "15+7",
            "6-3",
            "9+7",
            "7+7",
            "12+2",
            "14+9",
            "9-2",
            "9-5",
            "12-9",
            "14+10",
            "13+10",
            "10-1",
            "7-4",
            "11-9",
            "14+5",
            "14-9",
            "5+7",
            "13-4",
            "10+4",
            "7-5",
            "15-3",
            "13+4",
            "14-1",
            "13+8",
            "10-6",
            "8-5",
            "10+6",
            "10+8",
            "8+6",
            "15+9",
            "14+8",
            "10+10",
            "12-7",
            "7-3",
            "11+2",
            "10-6",
            "5-2",
            "6-3",
            "12-2",
            "7+9",
            "6-5",
            "11-4",
            "11-10",
            "11-8",
            "13-5",
            "10-5",
            "8+8",
            "11-4",
            "15+3",
            "11+4",
            "9+2",
            "8+9",
            "6-3",
            "14-7",
            "5+2",
            "7-4",
            "8-6",
            "10+2",
            "10-2",
            "13-10",
            "13+2",
            "12+4",
            "11+9",
            "6-6",
            "12-6",
            "11+1",
            "6-5",
            "10-3",
            "9-6",
            "10+1",
            "11+7",
            "15-7",
            "5+4",
            "14-9",
            "9+2",
            "7-2",
            "14-7",
            "12-3",
            "7-5",
            "9+8",
            "15+5",
            "6+5",
            "14+10",
            "10-8",
            "15-2",
            "11-9",
            "9-6",
            "5+4",
            "12-7",
            "14+9",
            "11-5",
            "6+4",
            "11+2",
            "12+6",
            "13+1",
            "15+7",
            "15+3",
            "9+7",
            "7+8",
            "11-6",
            "9-7",
            "13-5",
            "12-7",
            "9+4",
            "7+7",
            "15+5",
            "14-7",
            "13+3",
            "8+5",
            "7-4",
            "13+5",
            "6-5",
            "10-6",
            "10+8",
            "11-9",
            "11-1",
            "10-1",
            "6-3",
            "13+9",
            "13+8",
            "14-2",
            "10+2",
            "10-8",
            "9+1",
            "5+4",
            "11+9",
            "10-1",
            "13-7",
            "12-1",
            "7-1",
            "4-2",
            "6-3",
            "12-5",
            "7-5",
            "13-4",
            "11-5",
            "12-9",
            "14+8",
            "10-9",
            "15+10",
            "12+5",
            "6+9",
            "12-7",
            "5+6",
            "11+10",
            "8+10",
            "15-9",
            "13+10",
            "11-4",
            "5+5",
            "12+3",
            "9-3",
            "12-2",
            "10-7",
            "13-5",
            "8+1",
            "12-10",
            "13-9",
            "6-4",
            "5-4",
            "12+2",
            "10+3",
            "15+7",
            "13+8",
            "8-1",
            "14-2",
            "5+8",
            "9-3",
            "8+10",
            "6-3",
            "12+1",
            "14+12",
            "9-5",
            "9-1",
            "10-3",
            "7-2",
            "15-14",
            "11-2",
            "10+2",
            "12-4",
            "6-5",
            "14-9",
            "8-3",
            "8-5",
            "8+4",
            "9-7",
            "1+1",
            "11-5",
            "10+6",
            "12-1",
            "14-1",
            "13-12",
            "7-2",
            "15+8",
            "3-1",
            "15+7",
            "10+9",
            "13-4",
            "15+12",
            "12-6",
            "6+9",
            "14+13",
            "6-1",
            "8-4",
            "3+10",
            "6-3",
            "10+10",
            "8-5",
            "3+12",
            "7+13",
            "5+1",
            "7+12",
            "15-5",
            "9-6",
            "12-9",
            "15-10",
            "4+12",
            "11-7",
            "8+15",
            "2+1",
            "15+3",
            "5+7",
            "14-13",
            "12-6",
            "11+7",
            "15-2",
            "6+9",
            "9-7",
            "13+2",
            "5+10",
            "1+3",
            "10-5",
            "5+15",
            "11-9",
            "7+6",
            "13-2",
            "13-2",
            "6+7",
            "11-1",
            "6-1",
            "9+6",
            "13+4",
            "2+3",
            "9-2",
            "12-8",
            "15+10",
            "7-4",
            "13-5",
            "13-12",
            "11-6",
            "11+10",
            "9+3",
            "12-6",
            "2+10",
            "10-4",
            "1+4",
            "3+15",
            "15+14",
            "7+15",
            "12-6",
            "3+13",
            "14-12",
            "8+3",
            "14-1",
            "6+1",
            "15+12",
            "9+11",
            "8+10",
            "9+15",
            "15-2",
            "3+5",
            "4+13",
            "7+7",
            "8+15",
            "13-5",
            "13-8",
            "12+6",
            "12-5",
            "10-9",
            "5+7",
            "10+7",
            "11-9",
            "8-2",
            "14+15"

        )
    //200-300
    val equationsEasy3 = listOf<String>("11+7",
            "15-7",
            "5+4",
            "14-9",
            "9+2",
            "7-2",
            "14-7",
            "12-3",
            "7-5",
            "9+8",
            "15+5",
            "6+5",
            "14+10",
            "10-8",
            "15-2",
            "11-9",
            "9-6",
            "5+4",
            "12-7",
            "14+9",
            "11-5",
            "6+4",
            "11+2",
            "12+6",
            "13+1",
            "15+7",
            "15+3",
            "9+7",
            "7+8",
            "11-6",
            "9-7",
            "13-5",
            "12-7",
            "9+4",
            "7+7",
            "15+5",
            "14-7",
            "13+3",
            "8+5",
            "7-4",
            "13+5",
            "6-5",
            "10-6",
            "10+8",
            "11-9",
            "11-1",
            "10-1",
            "6-3",
            "13+9",
            "13+8",
            "14-2",
            "10+2",
            "10-8",
            "9+1",
            "5+4",
            "11+9",
            "10-1",
            "13-7",
            "12-1",
            "7-1",
            "4-2",
            "6-3",
            "12-5",
            "7-5",
            "13-4",
            "11-5",
            "12-9",
            "14+8",
            "10-9",
            "15+10",
            "12+5",
            "6+9",
            "12-7",
            "5+6",
            "11+10",
            "8+10",
            "15-9",
            "13+10",
            "11-4",
            "5+5",
            "12+3",
            "9-3",
            "12-2",
            "10-7",
            "13-5",
            "8+1",
            "12-10",
            "13-9",
            "6-4",
            "5-4",
            "12+2",
            "10+3",
            "15+7",
            "13+8",
            "8-1",
            "14-2",
            "5+8",
            "9-3",
            "8+10",
            "6-3")

}

