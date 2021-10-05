package utils

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean

/*object SimpleScheduler {
    private val tasks = CopyOnWriteArrayList<SimpleScheduledExecutorTask>()
    private var executorService : ScheduledExecutorService? = null

    fun startExecutorService() {
        if (this.executorService == null || this.executorService?.isShutdown!!)
            this.executorService = Executors.newScheduledThreadPool(3)
    }

    fun stopExecutorService() {
        this.cancelAllTasks()
        this.executorService?.shutdown()
    }

    fun runTask(simpleScheduledTask: SimpleScheduledTask) {
        this.startExecutorService()
        @Suppress("UNCHECKED_CAST")
        this.tasks.add(SimpleScheduledExecutorTask(
            simpleScheduledTask,
            this.executorService?.schedule(simpleScheduledTask, simpleScheduledTask.delay, TimeUnit.MILLISECONDS) as
                ScheduledFuture<SimpleScheduledTask>))
    }

    fun cancelAllTasks() {
        this.tasks.forEach { it.scheduledFuture.cancel(true) }
        this.tasks.clear()
    }

    fun getAllEventResponseTasks() : List<SimpleScheduledExecutorTask> {
        return this.tasks.filter { it.task is SimpleEventResponseTask }
    }

    fun getEventResponseTaskForEvent(event: Event) : SimpleScheduledExecutorTask? {
        return this.tasks.find { it.task is SimpleEventResponseTask &&
                event.id == it.task.event.id }
    }

    fun removeTask(task: SimpleScheduledTask) {
        this.tasks.removeIf { it.task == task }
    }
}


abstract class SimpleScheduledTask(protected val task: ((param1 : Boolean?, param2 : Event?) -> Unit)? = null, val delay: Long = 0L) : Thread() {
    private var startTime = 0L
    private var endTime = -1L

    init {
        this.isDaemon = true
    }

    protected fun setStartTime() {
        this.startTime = System.currentTimeMillis()
    }

    fun getStartTime() : Long {
        return this.startTime
    }

    private fun setEndTime() {
        this.endTime = System.currentTimeMillis()
    }

    fun hasEnded() : Boolean {
        return this.endTime > 0
    }

    protected fun finalizeTask() {
        try {
            this.setEndTime()
        } catch (ex: InterruptedException) { }
        try {
            SimpleScheduler.removeTask(this)
        } catch (ex: InterruptedException) { }
    }
}

/**
 * This Task will execute after a certain delay
 * @param task a function that is invoked after the delay (param1 and param 2 are unused)
 * @param delay the delay specified in milliseconds
 */
class SimpleDelayedTask(task: (param1 : Boolean?, param2 : Event?) -> Unit, delay: Long) : SimpleScheduledTask(task, delay) {
    override fun run() {
        try {
            this.setStartTime()
            this.task?.invoke(null,null)
        } catch (nothingToDo : InterruptedException) {
        } finally {
            this.finalizeTask()
        }
    }
}

/**
 * This Task will be started after a certain delay and end in one of the two following cases:
 * 1. once the timeout period is reached OR
 * 2. if the task is stopped programmatically by calling stopTask()
 *
 * IMPORTANT: The optional task is carried out AFTER the timeout/stopping occurred and receives as its first parameter
 *            a boolean value that informs us if the task has been stopped OR simply timed out
 *
 * @param task a function that is invoked after the delay
 * @param timeout the timeout period specified in milliseconds
 * @param delay the delay specified in milliseconds
 */
open class SimpleTimeoutTask(task: ((hasBeenStopped : Boolean?, event: Event?) -> Unit)? = null, private val timeout : Long, delay: Long = 0L) :
    SimpleScheduledTask(task, delay) {

    private val stopped = AtomicBoolean(false)
    protected val waitLock = Object()

    override fun run() {
        try {
            this.setStartTime()

            synchronized(this.waitLock) {
                this.waitLock.wait(this.timeout)
                this.task?.invoke(this.hasBeenStopped(), null)
            }
        } catch (nothingToDo : InterruptedException) {
        } finally {
            this.finalizeTask()
        }
    }

    @Synchronized
    fun stopTask() {
        try {
            synchronized(this.waitLock) {
                this.stopped.set(true)
                this.waitLock.notify()
            }
        } catch (nothingToDo : InterruptedException) {}
    }

    @Synchronized
    fun hasBeenStopped() : Boolean {
        return this.stopped.get()
    }
}

/**
 * This task waits until the stopTask() has been called (as to acknowledge a response to event) or
 * until a specified timeout occurs whichever one happens first
 *
 * IMPORTANT: The optional task is carried out AFTER the timeout/stopping occurred and receives as its first parameter
 *            a boolean value that informs us if the task has been stopped OR simply timed out
 *            Its second parameter is a reference to the event in question
 *
 * @param event the event linked to the timed wait (to be used in thee callback function)
 * @param task a function that is invoked after the delay
 * @param timeout the timeout period specified in milliseconds
 */
class SimpleEventResponseTask(val event : Event, task: ((hasBeenStopped : Boolean?, event : Event?) -> Unit)? = null, private val timeout : Long) :
    SimpleTimeoutTask(task, timeout) {

    override fun run() {
        try {
            this.setStartTime()

            synchronized(this.waitLock) {
                this.waitLock.wait(this.timeout)
                this.task?.invoke(this.hasBeenStopped(), this.event)
            }
        } catch (nothingToDo : InterruptedException) {
        } finally {
            this.finalizeTask()
        }
    }
}

data class SimpleScheduledExecutorTask(val task: SimpleScheduledTask, val scheduledFuture: ScheduledFuture<SimpleScheduledTask>)


 */
