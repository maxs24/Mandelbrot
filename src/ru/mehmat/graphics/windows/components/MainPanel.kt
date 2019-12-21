package ru.mehmat.graphics.windows.components

import ru.mehmat.graphics.convertation.Converter
import ru.mehmat.graphics.painters.FractalPainter
import ru.mehmat.graphics.painters.SelectionRectPainter
import java.awt.Graphics
import java.awt.event.*
import javax.swing.JPanel
import javax.swing.SwingWorker
import kotlin.math.ln

class MainPanel (var painter: FractalPainter): JPanel(){
    val srp = SelectionRectPainter()
    private var square = 0.0
    val getSquare: Double
        get()=square
    var startApprox = false
    var dinIter = false
    inner class BackgroundProcess : SwingWorker<Unit, Unit>() {
        override fun doInBackground() {
            painter.create()
        }

        override fun done() {
            painter.paint(this@MainPanel.graphics)
        }
    }

    private var bgProcess = BackgroundProcess()

    init{
        if (startApprox==false) square=(painter.plane.xMax-painter.plane.xMin)*(painter.plane.yMax-painter.plane.yMin)
        addComponentListener(
            object: ComponentAdapter(){
                override fun componentResized(e: ComponentEvent?) {
                    painter.created = false
                    repaint()
                }
            })
        addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent?) {
                super.mouseReleased(e)
                if (e != null && e.button == MouseEvent.BUTTON1) {
                    srp.stop()
                    val x1 = Converter.xScr2Crt(srp.leftTopPoint.x, painter.plane)
                    val y1 = Converter.yScr2Crt(srp.leftTopPoint.y, painter.plane)
                    val x2 = Converter.xScr2Crt(srp.rightBottomPoint.x, painter.plane)
                    val y2 = Converter.yScr2Crt(srp.rightBottomPoint.y, painter.plane)
                    startApprox=true
                    if (!painter.proportion) {


                        painter.plane.xMin = x1
                        painter.plane.xMax = x2
                        painter.plane.yMax = y1
                        painter.plane.yMin = y2
                        painter.xmin=painter.plane.xMin
                        painter.xmax=painter.plane.xMax
                        painter.ymin=painter.plane.yMin
                        painter.ymax=painter.plane.yMax
                        painter.created = false
                        repaint()
                    } else {
                        painter.xmin=x1
                        painter.xmax=x2
                        painter.ymin=y2
                        painter.ymax=y1
                        var srpY = y1 - y2
                        var srpX = x2 - x1
                        var cf = width.toDouble() / height.toDouble()
                        if (srpY < srpX) {
                            var rsY = srpX / cf
                            painter.plane.xMin = x1
                            painter.plane.xMax = x2
                            painter.plane.yMax = y1 + (rsY - srpY) / 2
                            painter.plane.yMin = y2 - (rsY - srpY) / 2
                            painter.created = false
                            repaint()
                        } else {
                            var rsX = srpY * cf
                            painter.plane.xMin = x1 - (rsX - srpX) / 2
                            painter.plane.xMax = x2 + (rsX - srpX) / 2
                            painter.plane.yMax = y1
                            painter.plane.yMin = y2
                            painter.created = false
                            repaint()

                        }
                    }
                    if (dinIter){
                        val coeffIncrease= (35/painter.fractal.minIter.toDouble())*ln(square/((painter.plane.xMax-painter.plane.xMin)
                                *(painter.plane.yMax-painter.plane.yMin)))
                        if (coeffIncrease-1>1e-10) painter.fractal.maxIter=(painter.fractal.minIter*coeffIncrease).toInt()
                    }
                }
            }

            override fun mousePressed(e: MouseEvent?) {
                super.mousePressed(e)
                if (e != null && e.button == MouseEvent.BUTTON1) {
                    srp.start(e.point)
                    srp.g = graphics
                }
            }
        })
        addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent?) {
                super.mouseDragged(e)
                if (e != null) {
                    srp.shift(e.point)
                }
            }
        })
    }

    override fun paint(g: Graphics?) {
        painter.plane.realWidth = width
        painter.plane.realHeight = height
        super.paint(g)
        g?.let { painter.paint(it) }
        if (!painter.created) {
            if (!bgProcess.isDone) bgProcess.cancel(true)
            bgProcess = BackgroundProcess()
            bgProcess.execute()
        }
    }
}