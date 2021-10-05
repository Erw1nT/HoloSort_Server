package test

import controllers.WardController
import models.Clinician
import models.FootDirection
import models.MapGrid
import java.lang.System.exit
import java.lang.Thread.sleep

fun main(args : Array<String>)
{
    var c = Clinician()

    var mapDef =    "8x8x3\n" +
            "xxxxxRRR\n" +
            "xxRRxRRR\n" +
            "xxRRxRRR\n" +
            "xx..xRRR\n" +
            "xx.....x\n" +
            "xxx..xxx\n" +
            "xxxNN.RR\n" +
            "xxxNN.RR"

    var m = MapGrid.parseMapGridDef(mapDef)!!

    var w = WardController(c, m)

    w.reset()

    println(w)

    w.moveClinician(FootDirection.NORTH)

    println(w)
    sleep(2000L)

    w.moveClinician(FootDirection.SOUTH)

    println(w)
    sleep(2000L)

    w.moveClinician(FootDirection.SOUTH)

    println(w)
    sleep(2000L)

    w.moveClinician(FootDirection.SOUTH)

    println(w)
    sleep(2000L)


    w.moveClinician(FootDirection.EAST)

    println(w)
    sleep(2000L)


    w.moveClinician(FootDirection.EAST)

    println(w)
    sleep(2000L)

    w.moveClinician(FootDirection.EAST)

    println(w)
    sleep(2000L)


    w.moveClinician(FootDirection.WEST)

    println(w)
    sleep(2000L)

    w.moveClinician(FootDirection.WEST)

    println(w)
    sleep(2000L)


    w.moveClinician(FootDirection.NORTH)

    println(w)
    sleep(2000L)


    w.moveClinician(FootDirection.NORTH)

    println(w)
    sleep(2000L)

    w.moveClinician(FootDirection.NORTH)

    println(w)
    sleep(2000L)


    w.moveClinician(FootDirection.NORTH)

    println(w)
    sleep(2000L)

    w.moveClinician(FootDirection.NORTH)

    println(w)

    sleep(10000L)


    w.moveClinician(FootDirection.SOUTH)

    println(w)

    w.moveClinician(FootDirection.SOUTH)

    println(w)

    sleep(10000L)
    exit(0)
}