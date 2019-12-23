package ru.mehmat.graphics.painters

import ru.mehmat.graphics.convertation.CartesianScreenPlane
import ru.mehmat.graphics.convertation.Converter
import ru.mehmat.math.fractals.Mandelbrot
import java.awt.Color
import java.awt.Graphics
import java.awt.image.BufferedImage
import kotlin.concurrent.thread
import kotlin.math.abs

class FractalPainter(var plane: CartesianScreenPlane,
                     val fractal: Mandelbrot
)
{
    var proportion:Boolean=false
    var xmin=0.0
    var xmax=0.0
    var ymin=0.0
    var ymax=0.0
    var cs: (Float) -> Color
    var readyBuf: BufferedImage? = null
    val buf: BufferedImage?
        get() = readyBuf
    private val threads: MutableList<Thread> = mutableListOf()
    var created = false
    var x_c:  Double = 0.0
    var  y_c:  Double = 0.0
    var isJulia :Boolean = false

    init {
        cs = { if (abs(it) < 1e-10) Color.BLACK else Color.WHITE }

        /*plane.addResizeListener { old, new ->
            if (old != new && plane.realWidth > 0 && plane.realHeight > 0) {
                created = false
            }
        }*/

        if (plane.realWidth > 0 && plane.realHeight > 0)
            readyBuf = BufferedImage(plane.realWidth, plane.realHeight, BufferedImage.TYPE_INT_RGB)

    }

    fun paint(gr: Graphics) {
        gr.drawImage(readyBuf, 0, 0, plane.realWidth, plane.realHeight, null)
    }



    fun create() {
        val buf = BufferedImage(plane.realWidth, plane.realHeight, BufferedImage.TYPE_INT_RGB)
        val g = buf.graphics
        g.clearRect(
            0,
            0,
            plane.realWidth,
            plane.realHeight
        )
        g.color = Color.BLACK
        for (th in threads) {
            if (th.isAlive) try {
                th.interrupt()
            } catch (e: InterruptedException) {
            }
        }
        threads.clear()

        val maxThreads = 4

        for (k in 0 until maxThreads) {
            val kWidth = plane.width / maxThreads
            threads.add(k, thread {
                val min = k * kWidth
                val max = if (k == maxThreads - 1) plane.width else (k + 1) * kWidth - 1
                for (i in min..max) {
                    for (j in 0..plane.height) {
                        val x =
                            Converter.xScr2Crt(i, plane)
                        val y =
                            Converter.yScr2Crt(j, plane)
                        val d = fractal.isInSet(x, y)
                        synchronized(g) {
                            g.color = cs(d)
                            g.fillRect(i, j, 1, 1)
                        }
                    }
                }
            })
        }
        for (t in threads) {
            t.join()
        }
        created = true
        readyBuf = buf
    }

    fun create( x2:Double,  y2: Double) {
        val buf = BufferedImage(plane.realWidth, plane.realHeight, BufferedImage.TYPE_INT_RGB)
        val g = buf.graphics
        g.clearRect(
            0,
            0,
            plane.realWidth,
            plane.realHeight
        )
        g.color = Color.BLACK
        for (th in threads) {
            if (th.isAlive) try {
                th.interrupt()
            } catch (e: InterruptedException) {
            }
        }
        threads.clear()

        val maxThreads = 4

        for (k in 0 until maxThreads) {
            val kWidth = plane.width / maxThreads
            threads.add(k, thread {
                val min = k * kWidth
                val max = if (k == maxThreads - 1) plane.width else (k + 1) * kWidth - 1
                for (i in min..max) {
                    for (j in 0..plane.height) {
                        val x =
                            Converter.xScr2Crt(i, plane)
                        val y =
                            Converter.yScr2Crt(j, plane)
                        val d = fractal.isInSetJulia(x, y,x2,y2)
                        synchronized(g) {
                            g.color = cs(d)
                            g.fillRect(i, j, 1, 1)
                        }
                    }
                }
            })
        }
        for (t in threads) {
            t.join()
        }
        created = true
        readyBuf = buf
    }

    fun setColorScheme(cs: (Float) -> Color) {
        created = false
        this.cs = cs
    }

}

//Java-style
/*
class P(var g: Graphics) : Runnable {
    override fun run() {
        g.color = Color.BLUE
        g.fillRect(10, 10, 300, 300)
    }
}*/
