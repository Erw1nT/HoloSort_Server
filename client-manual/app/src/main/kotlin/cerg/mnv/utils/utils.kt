package cerg.mnv.utils

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.AsyncTask
import android.os.StatFs
import logging.GlobalLogger
import transfer.*
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KFunction

data class SoundDownload(val soundToInitialize: SoundToInitialize) {
    private var fileTransferRequest = FileTransferRequest(soundToInitialize.soundFile)
    private var fileWriter : BinaryFileWriter = BinaryFileWriter(this.fileTransferRequest)

    fun resetToRetry() {
        this.fileTransferRequest = FileTransferRequest(soundToInitialize.soundFile)
        this.fileWriter = BinaryFileWriter(this.fileTransferRequest)
    }

    fun getLocalPath() : String? {
        return this.fileWriter.downloadLocation?.absolutePath
    }

    fun abortSoundDownload() {
        this.fileWriter.cancelDownload()
    }

    fun hasBeenCancelled() : Boolean {
        return this.fileWriter.hasBeenCancelled()
    }

    fun hasFinished() : Boolean {
        return this.fileWriter.isFinished()
    }

    fun hasStarted() : Boolean {
        return this.fileWriter.hasStarted()
    }

    fun isDownloading() : Boolean {
        return (this.fileWriter.hasStarted() && !this.fileWriter.isFinished() && !this.fileWriter.hasBeenCancelled())
    }

    fun showsNoActivity() : Boolean {
        if (this.hasFinished() || this.hasBeenCancelled()) return false

        val timeOfInactivity = this.fileWriter.getTimeOfInactivity()
        if (timeOfInactivity > 30 * 1000) return true

        val lastActivity = this.fileWriter.getTimeOfLastActivity()
        if ((System.currentTimeMillis() - lastActivity) > 60 * 1000) return true

        return false
    }

    fun processBinaryTransferResponse(binaryTransferResponse: BinaryTransferResponse) {
        this.fileWriter.addPackage(binaryTransferResponse)
    }

    fun getBinaryTransferAbort() : BinaryTransferAbort {
        return BinaryTransferAbort(this.fileTransferRequest.uuid, this.fileTransferRequest.file, "client abort")
    }

    fun getBinaryTransferRequest() : FileTransferRequest {
        return this.fileTransferRequest
    }

    fun getBinaryTransferUuid() : String {
        return this.fileTransferRequest.uuid
    }
}

/**
 * Helper class that keeps track of which sound downloads and triggers them
 */
object SoundDownloads {
    private val DOWNLOAD_DIR : String = System.getProperty("java.io.tmpdir")
    private val sounds = ConcurrentHashMap<String, SoundDownload>()
    private var downloadScheduler : SoundDownloadScheduler? = null

    fun getSoundDownload(name: String) : SoundDownload? {
        return this.sounds[name]
    }

    fun findSoundDownloadByUuid(uuid: String) : SoundDownload? {
        this.sounds.values.forEach {
            if (it.getBinaryTransferUuid() == uuid) return it
        }

        return null
    }

    /**
     * Adds sound download to list of sounds that need to be downloaded
     * if sound is not in list already. In case it is and has been cancelled it is being reset to retry
     *
     * Returns whether the download needs to be downloaded still (or retried) or not
     */
    fun addSoundDownload(soundDownload: SoundDownload) : Boolean {
        val foundSound = getSoundDownload(soundDownload.soundToInitialize.name)
        if (foundSound == null)  {
            this.sounds[soundDownload.soundToInitialize.name] = soundDownload
            return true
        }

        val isSame = foundSound.soundToInitialize.isSameSoundInitialization(soundDownload.soundToInitialize)
        if (isSame && foundSound.hasBeenCancelled()) {
            foundSound.resetToRetry()
            return true
        } else if (!isSame) {
            this.sounds[soundDownload.soundToInitialize.name] = soundDownload
        }

        return false
    }

    private fun triggerSoundDownloads(send : KFunction<Unit>, maxDownloadsInParallel : Int = 2) : Int {
        var counter = 0
        this.sounds.values.forEach {
            if (counter > maxDownloadsInParallel) return@forEach

            // we check if some have been started and show no activity
            // in which case we abort and reset to retry them
            if (it.hasStarted() && it.showsNoActivity()) {
                it.abortSoundDownload()
                it.resetToRetry()
            }

            if (!it.hasBeenCancelled() && !it.hasStarted()) {
                try {
                    send.call(BinaryTransferTypes.FILE, it.getBinaryTransferRequest().toJSONObject())
                    counter++
                } catch (ex : Exception) {
                    GlobalLogger.app().logError(ex.toString())
                }
            }
        }

        return counter
    }

    fun stopSoundDownloads(send : KFunction<Unit>?) {
        this.sounds.values.forEach {
            if (it.isDownloading()) {
                send?.call(BinaryTransferTypes.ABORT, it.getBinaryTransferAbort().toJSONObject())
                it.abortSoundDownload()
            }
        }
    }

    fun startCheckingForDownloads(send : KFunction<Unit>) {
        if (this.downloadScheduler != null) {
            this.stopCheckingForDownloads()
        }
        this.downloadScheduler = SoundDownloadScheduler()
        this.downloadScheduler?.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, send)
    }

    fun stopCheckingForDownloads() {
        this.downloadScheduler?.cancel(true)
        this.downloadScheduler = null
    }

    private class SoundDownloadScheduler : AsyncTask<Any, Void, Unit>() {
        private val waitLock = Object()
        override fun doInBackground(vararg params: Any) {
            try {
                while (true) {
                    val numbersOfParallelDownloads = if (params.size > 1) params[1] as Int else 2
                    @Suppress("UNCHECKED_CAST")
                    if (triggerSoundDownloads(params[0] as KFunction<Unit>, numbersOfParallelDownloads) == 0) break

                    synchronized(waitLock) {
                        waitLock.wait(2000)
                    }
                }
            } catch (interrupt : InterruptedException) {
            } catch (any : Exception) {
                GlobalLogger.app().logError(any.toString())
            }
        }
    }

    private fun hasAvailableSpaceInTmpDirectory(bytesNeeded : Long) : Boolean {
        return bytesNeeded < getFreeSpaceInBytes(DOWNLOAD_DIR)
    }

    fun makeAvailableSpaceInTmpDirectory(soundInitializationRequest: SoundInitializationRequest) : Boolean {
        if (soundInitializationRequest.totalNumberOfBytesNeeded >= getTotalSpaceInBytes(DOWNLOAD_DIR)) return false

        if (this.hasAvailableSpaceInTmpDirectory(soundInitializationRequest.totalNumberOfBytesNeeded)) return true

        // try to delete files
        val newFiles = soundInitializationRequest.getSoundsToInialize()
        val downloadFolderContents = File(DOWNLOAD_DIR).listFiles()
        fun isAcceptedSoundFile(ext: String) : Boolean { return ext in arrayListOf("wav", "mp3") }

        // we delete non sound extensions and files that don't have the same name
        downloadFolderContents.forEach {
            if (!isAcceptedSoundFile(it.extension)) it.delete()
            else if (newFiles.count { f -> f.name != it.name } == 0) {
                // check if we happen to have a prior download that is finished
                val potentialDownload = getSoundDownload(it.name)
                if (potentialDownload == null || potentialDownload.hasFinished()) it.delete()
            }
        }

        return soundInitializationRequest.totalNumberOfBytesNeeded < getFreeSpaceInBytes(DOWNLOAD_DIR)
    }
}

fun getFreeSpaceInBytes(path : String) : Long {
    return StatFs(path).freeBytes
}

fun getTotalSpaceInBytes(path : String) : Long {
    return StatFs(path).totalBytes
}


/**
 * While SoundDownloads keeps track of the downloads
 * the Sound Library uses the former to get the sound files
 * and prepare Media players
 */
object SoundLibrary {
    private val players = ConcurrentHashMap<String, Player>()

    class Player(private val soundFile : String, private val prepareNonBlocking :Boolean = true) {
        private val mediaPlayer = MediaPlayer()
        var isReady = false
            private set
        var errorOccurred = false
            private set

        init {
            this.prepare(this.prepareNonBlocking)
        }

        @Synchronized
        fun prepare(nonBlocking: Boolean = true) {
            try {
                this.mediaPlayer.reset()
            } catch (ignored: Exception) {}
            try {
                this.mediaPlayer.setDataSource(this.soundFile)

                if (nonBlocking) {
                    this.mediaPlayer.setOnPreparedListener {
                        this.isReady = true
                        this.errorOccurred = false
                    }
                    this.mediaPlayer.prepareAsync()
                } else {
                    this.mediaPlayer.prepare()
                    this.isReady = true
                    this.errorOccurred = false
                }
            } catch (ex : Exception) {
                this.errorOccurred = true
            }
        }

        private fun isPlaying() : Boolean {
            return this.mediaPlayer.isPlaying
        }

        fun start() {
            try {
                if (!this.isPlaying() && this.isReady) this.mediaPlayer.start()
            } catch (ex: Exception) {
                GlobalLogger.app().logError("Failed to start Player")
            }
        }

        fun stop() {
            if (this.isPlaying()) {
                this.mediaPlayer.pause()
                this.mediaPlayer.seekTo(0)
            }
        }

        fun release() {
            try {
                this.mediaPlayer.stop()
            } catch (ignored: Exception) {}
            try {
                this.mediaPlayer.release()
            } catch (ignored: Exception) {}
        }
    }

    fun addPlayer(soundDownload: SoundDownload?) : Boolean {
        if (soundDownload == null || !soundDownload.hasFinished() || soundDownload.getLocalPath() == null)  return false

        val existingPlayer = this.players[soundDownload.soundToInitialize.name]
        existingPlayer?.release()

        return try {
            this.players[soundDownload.soundToInitialize.name] = Player(soundDownload.getLocalPath() as String)
            true
        } catch (ex: Exception) {
            false
        }

    }

    fun getPlayer(name : String) : Player? {
        var p = this.players[name]
        if (p != null) {
            if (p.errorOccurred) {
                val relatedDownload = SoundDownloads.getSoundDownload(name)
                if (relatedDownload != null && relatedDownload.hasFinished() &&
                        relatedDownload.getLocalPath() != null) {
                    p = Player(relatedDownload.getLocalPath() as String, false)
                    this.players[name] = p
                } else {
                    p.prepare(false)
                }
            }
            return p
        }

        // see if we can find it in the downloads and it has finished
        val d = SoundDownloads.getSoundDownload(name) ?: return null
        if (!d.hasFinished() || d.getLocalPath() == null) return null

        return try {
            this.players[name] = Player(d.getLocalPath() as String, false)
            this.players[name]
        } catch (ex: Exception) {
            null
        }
    }

    fun stopAllPlayers() {
        this.players.values.forEach {
            it.stop()
        }
    }

    fun removeAllPlayers(release : Boolean = false) {
        if (release) {
            this.players.values.forEach { it.release() }
        } else this.stopAllPlayers()
        this.players.clear()
    }

    fun areAllPlayersReady() : Boolean {
        this.players.values.forEach {
            if (!it.isReady) {
                if (it.errorOccurred) {
                    it.prepare(false)
                }
                if (!it.isReady) return false
            }
        }

        return true
    }
}

fun setSoundLevels(audioManager: AudioManager, levelInPercent: Int) {
    val l by lazy {
        when {
            levelInPercent < 0 -> 0
            levelInPercent > 100 -> 100
            else -> levelInPercent
        }
    }
    audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            ((audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 100F) * l).toInt(),
            0)

    audioManager.setStreamVolume(
            AudioManager.STREAM_NOTIFICATION,
            ((audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)  / 100F) * l).toInt(),
            0)

    audioManager.setStreamVolume(
            AudioManager.STREAM_SYSTEM,
            ((audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM) / 100F) * l).toInt(),
            0)

    audioManager.setStreamVolume(
            AudioManager.STREAM_ALARM,
            ((audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) / 100F)  * l).toInt(),
            0)

    audioManager.setStreamVolume(
            AudioManager.STREAM_RING,
            ((audioManager.getStreamMaxVolume(AudioManager.STREAM_RING) / 100F) * l).toInt(),
            0)

    audioManager.setStreamVolume(
            AudioManager.STREAM_DTMF,
            ((audioManager.getStreamMaxVolume(AudioManager.STREAM_DTMF) / 100F) * l).toInt(),
            0)

    audioManager.setStreamVolume(
            AudioManager.STREAM_VOICE_CALL,
            ((audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL) / 100F) * l).toInt(),
            0)
}
