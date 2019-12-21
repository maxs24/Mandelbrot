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
        minimumSize = dim
        maximumSize = dim

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


        btnStart.addActionListener {
            val fr = 100
            var out:SeekableByteChannel? = null
            editPainter.buf?.let {
                try {
                    out = NIOUtils.writableFileChannel("./outt.mp4")
                    val encoder = AWTSequenceEncoder(out,Rational.R(30,1))
                    for (k in 0..100){
                        encoder.encodeImage(it)
                    }
                    encoder.finish()
                } finally {
                    NIOUtils.closeQuietly(out)
                }
            }

        }

        durVideo = JSpinner(SpinnerNumberModel(10, 1, 75, 1))
        val mas = DefaultListModel<String>()
        val imgCoords = ArrayList<CartesianPlane>()
        frameList = JList(mas)

        btnAdd.addActionListener {
            editPainter.buf?.let {
                //mas.addElement(mas)
                imgCoords.add(CartesianPlane(plane.xMin, plane.xMax, plane.yMin, plane.yMax))
                mas.addElement("1")

            }
        }
        val gl = GroupLayout(contentPane)
        layout = gl
        gl.setHorizontalGroup(
            gl.createSequentialGroup()
                .addGap(4)
                .addComponent(
                    editmainPanel,
                    (dim.width * 0.8).toInt(),
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
        gl.setVerticalGroup(
            gl.createSequentialGroup()
                .addGap(4)
                .addGroup(
                    gl.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(
                            editmainPanel,
                            (dim.height * 0.8).toInt(),
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