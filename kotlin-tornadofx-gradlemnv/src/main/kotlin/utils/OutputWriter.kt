package utils

import java.util.concurrent.LinkedBlockingQueue
import java.io.File
import java.io.PrintWriter
import java.util.Calendar
import java.text.SimpleDateFormat
import kotlin.reflect.full.memberProperties


class OutputWriter (val softwareName: String = "MPMGAME", versionId: Int = 10, val experimenterName: String = "Dev",
                      val mpmVersion: String = "2018-11-28-14-43", val selectedOrder: String = "ABAB", val experimentParams: String = "Params",
                      val participantId: String = "P999", val logRoot: String = System.getProperty("user.home") + "/Desktop")
{
    val csvBlockingQueue = LinkedBlockingQueue<OutputObject>()
    val declaredFields = OutputObject::class.java.declaredFields
    val memberProperties = OutputObject::class.memberProperties
    val logHeadings = Array<String>(declaredFields.size, {i -> declaredFields[i].name})
    val dateFormatter = SimpleDateFormat("yyyyMMdd_HHmmss")
    val calendar = Calendar.getInstance()
    val logTime = System.currentTimeMillis()
    val fileName = "$softwareName${String.format("%02d", versionId)}_MPM${mpmVersion}_Game_${participantId}_${experimentParams}_${getMillisAsDateTimeString(logTime)}.csv"
    val file = File(logRoot, fileName)
    val writer = PrintWriter(file)
    val CSV_DELIM = ", "
    @Volatile var writeLock = false

    init {

    }

    fun getMillisAsDateTimeString(millis: Long): String
    {
        calendar.timeInMillis = millis
        return dateFormatter.format(calendar.time)
    }

    fun writeWithDelim(s: String)
    {
        writer.print(s + CSV_DELIM)
    }

    fun writeHeading()
    {
        writer.println("#$CSV_DELIM $fileName")

        logHeadings.forEach {
            if (it == "Hash") writeWithDelim("#")
            else writeWithDelim(it)
        }
        writer.println()
        writer.flush()
    }

    @Synchronized
    fun writeOutput(outputObject: OutputObject)
    {
        csvBlockingQueue.add(outputObject)

        if (!writeLock)
        {
            writeLock = true

            while (csvBlockingQueue.size > 0)
            {
                val lo = csvBlockingQueue.take()
                declaredFields.forEach {
                    f ->
                    val property = memberProperties.find { it.name == f.name }
                    writeWithDelim(property!!.get(lo) as String)
                }
                writer.println()
                writer.flush()
            }

            writeLock = false
        }
    }

}