package ru.mehmat.graphics.windows

import jcodecc.javase.src.main.java.org.jcodec.api.awt.AWTSequenceEncoder
import org.jcodec.common.io.NIOUtils
import org.jcodec.common.io.SeekableByteChannel
import org.jcodec.common.model.Rational
import ru.mehmat.graphics.convertation.CartesianPlane
import ru.mehmat.graphics.convertation.CartesianScreenPlane
import ru.mehmat.graphics.painters.FractalPainter
import ru.mehmat.graphics.windows.components.MainPanel
import ru.mehmat.math.fractals.Mandelbrot
import java.awt.Dimension
import java.awt.image.BufferedImage
import java.util.*
import javax.swing.*
import kotlin.collections.ArrayList


class EditWindow() : JFrame() {

    private val editmainPanel: MainPanel
    private val editcontrolPanel: JPanel
    private val btnAdd: JButton
    private val btnRemove: JButton
    private val btnStart: JButton
    private val durVideo: JSpinner
    private val frameListPanel: JPanel
    private var frameList: JList<String>

    private val dim: Dimension
    private val editPainter: FractalPainter

    init {
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        dim = Dimension(700, 500)

        val plane = CartesianScreenPlane(
            -1,
            -1,
            -1.5,
            1.5,
            -1.5,
            1.5
        )

////////
        val m = Mandelbrot(2)
        editPainter = FractalPainter(plane, m)
        editPainter.proportion = true
        editmainPanel = MainPanel(editPainter)
        editmainPanel.dinIter = true
        editcontrolPanel = JPanel()
        frameListPanel = JPanel()
        btnAdd = JButton("Добавить")
        btnRemove = JButton("Удалить")
        btnStart = JButton("Начать создание видео")




        durVideo = JSpinner(SpinnerNumberModel(10, 1, 75, 1))
        val mas = DefaultListModel<String>()
        val imgCoords = ArrayList<CartesianPlane>()
        frameList = JList(mas)


        btnStart.addActionListener {
            val time = durVideo.value.toString().toInt()
            val timforone = time / (imgCoords.size - 1)
            val fps = 5
            val framecount = timforone * fps
            var out: SeekableByteChannel? = null

            try {
                out = NIOUtils.writableFileChannel("./outt.mp4")
                val encoder = AWTSequenceEncoder(out, Rational.R(fps, 1))
                for (k in 1..(imgCoords.size - 1)) {
                    val dxmin = Math.abs(imgCoords[k].xMin - imgCoords[k-1].xMin)/framecount
                    val dxmax = Math.abs(imgCoords[k-1].xMax - imgCoords[k].xMax)/framecount
                    val dymin = Math.abs(imgCoords[k].yMin - imgCoords[k-1].yMin)/framecount
                    val dymax = Math.abs(imgCoords[k-1].yMax - imgCoords[k].yMax)/framecount
                    for (i in 0..(framecount - 1)) {
                        editPainter.create()
                        plane.xMin += dxmin
                        plane.xMax -= dxmax
                        plane.yMin += dymin
                        plane.yMax -= dymax
                        editPainter.buf?.let {
                            encoder.encodeImage(it)
                        }
                    }
                }
                encoder.finish()
            } finally {
                NIOUtils.closeQuietly(out)
            }


        }


        btnAdd.addActionListener {
            imgCoords.add(CartesianPlane(plane.xMin, plane.xMax, plane.yMin, plane.yMax))
            mas.addElement("xMin: " + plane.xMin.toString() + " xMax: " + plane.xMax.toString() + " yMin: " + plane.yMin.toString() + " yMax: " + plane.yMax.toString())
        }
        val gl = GroupLayout(contentPane)
        layout = gl
        var wdh = if ((dim.width * 0.8).toInt() % 2 == 0) (dim.width * 0.8).toInt() else (dim.width * 0.8).toInt() - 1

        gl.setHorizontalGroup(
            gl.createSequentialGroup()
                .addGap(4)
                .addComponent(
                    editmainPanel,
                    wdh,
                    GroupLayout.DEFAULT_SIZE,
                    GroupLayout.DEFAULT_SIZE
                )
                .addGap(4)
                .addComponent(
                    editcontrolPanel,
                    (dim.width * 0.4).toInt(),
                    GroupLayout.PREFERRED_SIZE,
                    GroupLayout.PREFERRED_SIZE
                )
                .addGap(4)
        )

        var hdh =
            if ((dim.height * 0.9).toInt() % 2 == 0) (dim.height * 0.9).toInt() else (dim.height * 0.9).toInt() - 1
        gl.setVerticalGroup(
            gl.createSequentialGroup()
                .addGap(4)
                .addGroup(
                    gl.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(
                            editmainPanel,
                            hdh,
                            GroupLayout.DEFAULT_SIZE,
                            GroupLayout.DEFAULT_SIZE
                        )
                        .addComponent(
                            editcontrolPanel,
                            dim.height.toInt(),
                            GroupLayout.DEFAULT_SIZE,
                            GroupLayout.DEFAULT_SIZE
                        )
                )
        )


        val gl2 = GroupLayout(editcontrolPanel)
        editcontrolPanel.layout = gl2
        gl2.setHorizontalGroup(
            gl2.createSequentialGroup()
                .addGap(4)
                .addGroup(
                    gl2.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(
                            btnAdd,
                            GroupLayout.PREFERRED_SIZE,
                            GroupLayout.PREFERRED_SIZE,
                            GroupLayout.PREFERRED_SIZE
                        )
                        .addComponent(frameListPanel)
                        .addComponent(btnRemove)
                        .addComponent(durVideo)
                        .addComponent(btnStart)
                )
                .addGap(4)
        )
        gl2.setVerticalGroup(
            gl2.createSequentialGroup()
                .addGap(4)
                .addComponent(btnAdd)
                .addGap(4)
                .addComponent(
                    frameListPanel,
                    dim.height,
                    GroupLayout.PREFERRED_SIZE,
                    Int.MAX_VALUE
                )
                .addGap(4)
                .addComponent(btnRemove)
                .addGap(4)
                .addComponent(
                    durVideo,
                    GroupLayout.PREFERRED_SIZE,
                    GroupLayout.PREFERRED_SIZE,
                    GroupLayout.PREFERRED_SIZE
                )
                .addGap(4)
                .addComponent(btnStart)
                .addGap(4)
        )

        val gl3 = GroupLayout(frameListPanel)
        frameListPanel.layout = gl3
        gl3.setVerticalGroup(
            gl3.createSequentialGroup()
                .addComponent(
                    frameList,
                    GroupLayout.DEFAULT_SIZE,
                    GroupLayout.DEFAULT_SIZE,
                    GroupLayout.DEFAULT_SIZE
                )
        )
        gl3.setHorizontalGroup(
            gl3.createSequentialGroup()
                .addComponent(frameList)
        )

        pack()
        isVisible = true
    }
}