package ru.mehmat.graphics.windows.components

import ru.mehmat.graphics.convertation.CartesianScreenPlane
import ru.mehmat.graphics.convertation.Converter
import ru.mehmat.graphics.painters.FractalPainter
import ru.mehmat.math.fractals.Mandelbrot
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.GroupLayout
import javax.swing.JFrame
import javax.swing.WindowConstants
import kotlin.math.ln

class JuliaWindow: JFrame(), ActionListener {
        override fun actionPerformed(p0: ActionEvent?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    private val mainPanel: MainPanel
    private val dim: Dimension
    private val painter: FractalPainter

    init {
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        dim = Dimension(500, 500)
        minimumSize = dim

        var plane = CartesianScreenPlane(
            -1,
            -1,
            -1.5,
            1.5,
            -1.5,
            1.5
        )

        val m = Mandelbrot(2)
        painter = FractalPainter(plane, m)
        mainPanel = MainPanel(painter)

    val wind:JFrame = JFrame()
    var panel: MainPanel = MainPanel(painter)

    wind.isVisible = true
    wind.title = "Множество Жюлиа"
    wind.minimumSize = dim

    panel.addMouseListener(
    object : MouseAdapter(){
        override fun mousePressed(e: MouseEvent) {
            super.mousePressed(e)
            painter.x_c = Converter.xScr2Crt(e.x, plane)
            painter.y_c = Converter.yScr2Crt(e.y, plane)
            painter.isJulia = true
            val painter: FractalPainter = FractalPainter(plane,m)
            panel = MainPanel(painter)
            painter.create(painter.x_c,painter.y_c)
            panel.repaint()
        }
    }
    )
        val gl2 = GroupLayout(wind.contentPane)
        wind.layout = gl2
        gl2.setVerticalGroup(
            gl2.createSequentialGroup()
                .addGap(4)
                .addComponent(
                    panel,
                    (dim.height).toInt(),
                    GroupLayout.DEFAULT_SIZE,
                    GroupLayout.DEFAULT_SIZE
                )
    .addGap(4)
        )
        gl2.setHorizontalGroup(
            gl2.createSequentialGroup()
                .addGap(4)
                .addGroup(
                    gl2.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(
                            panel,
                            (dim.width).toInt(),
                            GroupLayout.DEFAULT_SIZE,
                            GroupLayout.DEFAULT_SIZE
                        )
                )
                .addGap(4)
        )
        wind.pack()
    }
}
