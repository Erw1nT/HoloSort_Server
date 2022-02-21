package cerg.mnv.controller

import android.content.Context
import android.media.MediaPlayer
import android.os.AsyncTask
import cerg.mnv.model.MultiPatientData
import cerg.mnv.model.MultiPatientDataListener
import cerg.mnv.model.Patient
import cerg.mnv.R
import logging.GlobalLogger
import java.util.concurrent.ConcurrentLinkedQueue

// TODO: refactor to play downloaded files as well...
class AuditoryDisplayController(private var context: Context, private var mpData: MultiPatientData) : MultiPatientDataListener {
    private var playSequence: PlaySequence? = null

    private var isStarted = false
    private var wasStartedBeforeFreeze = false
    private var isPlaying = false

    private data class PatientAndLevelInfo(val id: Int, val level: Patient.PatientLevel, val timestamp : Long) {
        var played : Boolean = false
        override fun equals(other: Any?): Boolean {
            if (other !is PatientAndLevelInfo || other.id != this.id || other.level != this.level ||
                    Math.abs(other.timestamp - this.timestamp) > 500) return false
            return true
        }

        override fun hashCode(): Int {
            var result = id
            result = 31 * result + level.hashCode()
            result = 31 * result + timestamp.hashCode()
            result = 31 * result + played.hashCode()
            return result
        }
    }
    private val changeQueue = ConcurrentLinkedQueue<PatientAndLevelInfo>()

    init {
        mpData.addListener(this)
    }

    /**
     * Starts the cycle of the auditory display
     */
    fun start() {
        if (!this.isStarted && this.mpData.isAlarmsEnabled) {
            this.isStarted = true
            this.playSequence = PlaySequence()
            this.playSequence?.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mpData)
        }
    }

    /**
     * Stops the cycle. The current sound will finished.
     */
    fun stop() {
        if (this.isStarted) {
            this.playSequence?.cancel(true)
            this.isStarted = false
            this.wasStartedBeforeFreeze = false
        }
    }

    override fun patientChanged(patient: Patient) {}
    override fun patientLevelChanged(patient: Patient) {
        if (!this.isStarted) {
            return
        }

        this.playSequence?.requestInstantPlay(patient)
    }

    override fun freezeOccured() {
        if (this.isStarted) {
            this.wasStartedBeforeFreeze = true
            this.stop()
        }
    }

    override fun unfreezeOccured() {
        if (this.wasStartedBeforeFreeze) {
            this.start()
            this.wasStartedBeforeFreeze = false
        }
    }

    override fun scenarioStarted() {
        this.stop()
        this.start()
    }

    override fun scenarioStopped() {
        this.stop()
    }

    /**
     * Sequence player class
     */
    private inner class PlaySequence : AsyncTask<MultiPatientData, Int, Long>() {
        private val waitLock = Object()
        private val soundLock = Object()
        private var mediaPlayer : MediaPlayer? = null

        /**
         * Plays what's in the queue
         *
         * @param mpData
         * @return
         */
        override fun doInBackground(vararg mpData: MultiPatientData): Long? {
            try {
                while (true) {
                    while (this@AuditoryDisplayController.changeQueue.isNotEmpty()) {
                        if (this.isCancelled) break
                        val nextPatient = this@AuditoryDisplayController.changeQueue.peek()
                        if (nextPatient.played) {
                            this@AuditoryDisplayController.changeQueue.remove()
                            continue
                        }
                        when (nextPatient.level) {
                            Patient.PatientLevel.CRITICAL ->
                                // play critical
                                this.playSound(R.raw.critical_v03)
                            Patient.PatientLevel.WARNING ->
                                // play warning
                                this.playSound(R.raw.warning_v03)
                            Patient.PatientLevel.NORMAL ->
                                // play boop
                                this.playSound(R.raw.boop_v02)
                        }
                        nextPatient.played = true
                    }

                    if (this.isCancelled) break
                    synchronized(waitLock) {
                        waitLock.wait()
                    }
                }
            } catch (int : InterruptedException) {
                if (!this.isCancelled) GlobalLogger.app().logError(int.toString())
            } finally {
                AuditoryDisplayController@isPlaying = false
                AuditoryDisplayController@isStarted = false
                AuditoryDisplayController@wasStartedBeforeFreeze = false
                this.mediaPlayer?.release()
            }

            return 0L
        }

        /**
         * Needs probably changing, in general a strategy because there could be too many sounds per patient/vital sign
         */
        fun requestInstantPlay(patient: Patient) {
            val newPatientAndLevelInfo = PatientAndLevelInfo(patient.id, patient.patientLevel, System.currentTimeMillis())
            if (!this@AuditoryDisplayController.changeQueue.contains(newPatientAndLevelInfo)) {
                this@AuditoryDisplayController.changeQueue.add(newPatientAndLevelInfo)
            }

            synchronized(this.waitLock) {
                this.waitLock.notifyAll()
            }
        }

        /**
         * Plays a sound in a 'blocking' fashion, i.e. waiting for it to finish
         *
         * @param sound
         */
        private fun playSound(sound: Int) {
            try {
                AuditoryDisplayController@isPlaying = true
                this.mediaPlayer = MediaPlayer.create(this@AuditoryDisplayController.context, sound)
                this.mediaPlayer?.setOnCompletionListener { player ->
                    try {
                        player.release()
                    } catch (interruptedException: InterruptedException) {
                        // don't care just clean up in finally
                    } finally {
                        AuditoryDisplayController@isPlaying = false
                    }
                }
                this.mediaPlayer?.start()
                Thread.sleep(this.mediaPlayer?.duration!!.toLong()  )
            } catch (ex: Exception) {
                AuditoryDisplayController@isPlaying = false
                this.mediaPlayer?.release()
            }
        }
    }

    companion object {
        private var instance: AuditoryDisplayController? = null

        fun createInstance(context: Context, mpData: MultiPatientData): AuditoryDisplayController {
            if (AuditoryDisplayController.instance == null) {
                AuditoryDisplayController.instance = AuditoryDisplayController(context, mpData)
            } else AuditoryDisplayController.instance?.context = context
            AuditoryDisplayController.instance?.start()

            return AuditoryDisplayController.instance!!
        }
    }
}
