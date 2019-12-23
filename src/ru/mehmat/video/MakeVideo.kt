package ru.mehmat.video

import jcodecc.javase.src.main.java.org.jcodec.api.awt.AWTSequenceEncoder
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jcodec.common.io.NIOUtils
import org.jcodec.common.io.SeekableByteChannel
import org.jcodec.common.model.Rational
import org.jcodec.movtool.SetFPS
import ru.mehmat.graphics.convertation.CartesianPlane
import ru.mehmat.graphics.convertation.CartesianScreenPlane
import ru.mehmat.graphics.painters.FractalPainter
import ru.mehmat.math.fractals.Mandelbrot
import java.awt.Color
import java.awt.image.BufferedImage
import javax.swing.DefaultListModel
import kotlin.math.ln

class MakeVideo(val time: Int,val fps: Int,val imgCoords: DefaultListModel<CartesianPlane>,val plane: CartesianScreenPlane,val m:Mandelbrot,val cs:(Float)->Color) {

    val timforone = time / (imgCoords.size - 1)
    val framecount = timforone * fps
    var out: SeekableByteChannel? = null
    val masBuf = ArrayList<ArrayList<BufferedImage>>()
    fun createVideo() {
        try {
            out = NIOUtils.writableFileChannel("./outt.mp4")
            val encoder = AWTSequenceEncoder(out, Rational.R(fps, 1))
            runBlocking {
                for (j in 1..(imgCoords.size - 1)) {
                    masBuf.add(ArrayList<BufferedImage>())
                    val jl =launch {
                        val k = j
                        val plane2 = CartesianScreenPlane(
                            plane.realWidth, plane.realHeight, imgCoords[k - 1].xMin,
                            imgCoords[k - 1].xMax, imgCoords[k - 1].yMin, imgCoords[k - 1].yMax
                        )
                        val square=9
                        val dxmin = Math.abs(imgCoords[k].xMin - imgCoords[k - 1].xMin) / framecount
                        val dxmax = Math.abs(imgCoords[k - 1].xMax - imgCoords[k].xMax) / framecount
                        val dymin = Math.abs(imgCoords[k].yMin - imgCoords[k - 1].yMin) / framecount
                        val dymax = Math.abs(imgCoords[k - 1].yMax - imgCoords[k].yMax) / framecount
                        for (i in 0..(framecount - 1)) {
                            val mm = Mandelbrot(2)
                            val painter = FractalPainter(plane2, mm)
                            plane2.xMin += dxmin
                            plane2.xMax -= dxmax
                            plane2.yMin += dymin
                            plane2.yMax -= dymax
                            painter.proportion = true
                            painter.setColorScheme(cs)
                            var coeffIncrease= (35/painter.fractal.minIter.toDouble())* ln(square/((painter.plane.xMax-painter.plane.xMin)
                                    *(painter.plane.yMax-painter.plane.yMin)))
                            if (coeffIncrease-1>1e-10) painter.fractal.maxIter=(painter.fractal.minIter*coeffIncrease).toInt()
                            painter.create()
                            painter.buf?.let {
                                masBuf[k - 1].add(it)
                            }
                        }
                    }
                }
            }
            masBuf.forEach { i ->
                i.forEach { j ->
                    encoder.encodeImage(j)
                }
            }
            encoder.finish()
        } finally {
            NIOUtils.closeQuietly(out)
        }
    }
}