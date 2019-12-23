package ru.mehmat.graphics.windows

import jcodecc.javase.src.main.java.org.jcodec.api.awt.AWTSequenceEncoder
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jcodec.common.io.NIOUtils
import org.jcodec.common.io.SeekableByteChannel
import org.jcodec.common.model.Rational
import ru.mehmat.graphics.convertation.CartesianPlane
import ru.mehmat.graphics.convertation.CartesianScreenPlane
import ru.mehmat.graphics.painters.FractalPainter
import ru.mehmat.graphics.windows.components.MainPanel
import ru.mehmat.math.fractals.Mandelbrot
import ru.mehmat.video.MakeVideo
import java.awt.Dimension
import java.awt.image.BufferedImage
import java.util.*
import javax.swing.*
import kotlin.collections.ArrayList
import java.awt.AWTEventMulticaster.getListeners
import java.awt.Color
import java.awt.Rectangle
import java.lang.Exception
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.sin


class EditWindow() : JFrame() {

    private val editmainPanel: MainPanel
    private val editcontrolPanel: JPanel
    private val btnAdd: JButton
    private val btnRemove: JButton
    private val btnStart: JButton
    private val durVideo: JSpinner
    private val frameListPanel: JScrollPane
    private var frameList: JList<ImageIcon>

    private val cs: (Float) -> Color = {
        Color.getHSBColor(
            abs(cos(5 * it)),
            (log10(abs(sin(10 * it)))),
            abs(sin(10 * it)).toFloat()
        )
    }
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
        val m = Mandelbrot(2)
        editPainter = FractalPainter(plane, m)
        editPainter.proportion = true
        editmainPanel = MainPanel(editPainter)
        editmainPanel.dinIter = true
        editcontrolPanel = JPanel()
        frameListPanel = JScrollPane()
        btnAdd = JButton("Добавить")
        btnRemove = JButton("Удалить")
        btnStart = JButton("Начать создание видео")
        editPainter.setColorScheme(cs)
        val wdh = if ((dim.width * 0.8).toInt() % 2 == 0) (dim.width * 0.8).toInt() else (dim.width * 0.8).toInt() - 1
        val hdh =
            if ((dim.height * 0.9).toInt() % 2 == 0) (dim.height * 0.9).toInt() else (dim.height * 0.9).toInt() - 1

        durVideo = JSpinner(SpinnerNumberModel(10, 1, 75, 1))
        val images = DefaultListModel<ImageIcon>()
        val imgCoords = DefaultListModel<CartesianPlane>()
        frameList = JList(images)
        frameListPanel.setViewportView(frameList)
        btnRemove.addActionListener {
            if (images.size() != 0) {
                try {
                    images.remove(frameList.anchorSelectionIndex)
                    imgCoords.remove(frameList.anchorSelectionIndex)
                }catch (e:Exception){

                }
            }
        }

        btnStart.addActionListener {
            val time = durVideo.value.toString().toInt()
            val fps = 15
            MakeVideo(time,fps,imgCoords,plane,m,cs).createVideo()
        }


        btnAdd.addActionListener {
            imgCoords.addElement(CartesianPlane(plane.xMin, plane.xMax, plane.yMin, plane.yMax))
            editPainter.buf?.let {
                val buf = BufferedImage((dim.width * 0.4).toInt(), 100, BufferedImage.TYPE_INT_RGB)
                buf.graphics.drawImage(it, 0, 0, (dim.width * 0.4).toInt(), 100, null)
                images.addElement(ImageIcon(buf))
            }


        }
        val gl = GroupLayout(contentPane)
        layout = gl


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
                    (dim.width * 0.4).toInt() + 10,
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


        pack()
        isVisible = true
    }
}