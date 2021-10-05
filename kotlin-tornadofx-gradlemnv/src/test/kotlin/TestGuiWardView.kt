package test

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import models.MapGrid
import kotlin.concurrent.timer


class TestGuiWardView : Application()
{

    var window = Stage()


    override fun start(primaryStage: Stage)
    {
        window = primaryStage

        var mapDef =    "8x8x3\n" +
                "xxxxxRRR\n" +
                "xxRRxRRR\n" +
                "xxRRxRRR\n" +
                "xx..xRRR\n" +
                "xx.....x\n" +
                "xxx..xxx\n" +
                "xxxNN.RR\n" +
                "xxxNN.RR"

        var mapGrid = MapGrid.parseMapGridDef(mapDef)

        val root = WardView()
        root.initView(mapGrid!!)
        root.monitors[1]?.patientName = "BOBO"
        root.monitors[1]?.vitalSignLabels = arrayOf("HR", "BP")
        root.patientRooms[1]?.isDoorOpen = false


        val scene = Scene(root, 800.0, 800.0)
        primaryStage.scene = scene
        primaryStage.show()

        timer(initialDelay = 3000L, period = 3000L, action = {
            root.patientRooms[1]?.isDoorOpen = !root.patientRooms[1]!!.isDoorOpen
        }, daemon = true)


    }

}

fun main(args: Array<String>)
{
    Application.launch(TestGuiWardView::class.java, *args)
}