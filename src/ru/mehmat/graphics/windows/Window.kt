package ru.mehmat.graphics.windows

import ru.mehmat.graphics.convertation.CartesianScreenPlane
import ru.mehmat.graphics.painters.FractalPainter
import ru.mehmat.graphics.windows.components.MainPanel
import ru.mehmat.math.fractals.Mandelbrot
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.image.BufferedImage
import java.io.File
import java.lang.Exception
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.stream.FileImageOutputStream
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.sin

class Window : JFrame() {
    private val mainPanel: MainPanel
    private val controlPanel: JPanel
    private val btnExit: JButton
    private val cbColor: JCheckBox
    private val cbProp: JCheckBox
    private val btnSaveImg: JButton

    private val dim: Dimension

    private val painter: FractalPainter
    private val cs0: (Float) -> Color = {
        if (abs(it) < 1e-10) Color.BLACK else Color.WHITE
    }
    private val cs1: (Float) -> Color = {
        Color.getHSBColor(
            abs(cos(100 * it)),
            (log10(abs(sin(10 * it)))),
            abs(sin(100 * it))
        )
    }
    private val cs2: (Float) -> Color = {
        Color.getHSBColor(
            abs(cos(5 * it)),
            (log10(abs(sin(10 * it)))),
            abs(sin(10 * it)).toFloat()
        )
    }
    private val cs3: (Float) -> Color = { value ->
        if (value >= 1) Color.BLACK
        if (value < 0) Color.WHITE
        Color(
            Math.abs(Math.sin(Math.PI / 8 + 12 * value)).toFloat(),
            Math.abs(Math.cos(Math.PI / 6 - 12 * value)).toFloat(),
            Math.abs(Math.cos(Math.PI / 2 + 12 * value)).toFloat()
        )
    }

    init {
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        dim = Dimension(500, 500)
        minimumSize = dim

        val plane = CartesianScreenPlane(
            -1,
            -1,
            -2.0,
            1.0,
            -1.0,
            1.0
        )

        val m = Mandelbrot()
        painter = FractalPainter(plane, m)

        mainPanel = MainPanel(painter)
        controlPanel = JPanel()
        controlPanel.border =
            BorderFactory.createTitledBorder(
                "Управление отображением"
            )
        btnExit = JButton("Выход")
        btnExit.addActionListener {
            System.exit(0)
        }
        cbColor = JCheckBox("Цвет", false)
        cbProp = JCheckBox(
            "Соблюдение пропорций",
            false
        )

        btnSaveImg = JButton("Сохранить")
        btnSaveImg.addActionListener {
            painter.buf?.let {
                val buf = BufferedImage(it.width,it.height+100,BufferedImage.TYPE_INT_RGB)
                buf.graphics.drawImage(it,0,0,it.width,it.height,null)
                buf.graphics.color= Color.white
                //buf.graphics.fillRect(0,it.height,it.width,it.height+100)
                //buf.graphics.color= Color.black
                buf.graphics.drawString("xmin= "+ plane.xMin,10,it.height+70)
                buf.graphics.drawString("xmax= "+ plane.xMax,10,it.height+40)
                buf.graphics.drawString("ymin= "+ plane.yMin,it.width/2,it.height+70)
                buf.graphics.drawString("ymax= "+ plane.yMax,it.width/2,it.height+40)
                saveImageFile(buf, this)
            }
        }
        setColorScheme()
        cbColor.addActionListener {
            setColorScheme()
            mainPanel.repaint()
        }
        cbProp.addActionListener{
            painter.proportion=cbProp.isSelected
            if (cbProp.isSelected) {
                painter.xmin=painter.plane.xMin
                painter.xmax=painter.plane.xMax
                painter.ymin=painter.plane.yMin
                painter.ymax=painter.plane.yMax
                val srpY = painter.plane.yMax - painter.plane.yMin
                val srpX = painter.plane.xMax - painter.plane.xMin
                val cf = plane.realWidth.toDouble() / plane.realHeight.toDouble()
                if (srpY < srpX) {
                    val rsY = srpX / cf
                    painter.plane.yMax += (rsY - srpY) / 2
                    painter.plane.yMin -= (rsY - srpY) / 2
                } else {
                    val rsX = srpY * cf
                    painter.plane.xMin -= (rsX - srpX) / 2
                    painter.plane.xMax += (rsX - srpX) / 2
                }
            }
            else{
                painter.plane.xMin=painter.xmin
                painter.plane.xMax=painter.xmax
                painter.plane.yMin=painter.ymin
                painter.plane.yMax=painter.ymax
            }
            painter.created=false
            mainPanel.repaint()

            //cbProp.isSelected
        }

        val gl = GroupLayout(contentPane)
        layout = gl
        gl.setVerticalGroup(
            gl.createSequentialGroup()
                .addGap(4)
                .addComponent(
                    mainPanel,
                    (dim.height * 0.7).toInt(),
                    GroupLayout.DEFAULT_SIZE,
                    GroupLayout.DEFAULT_SIZE
                )
                .addGap(4)
                .addComponent(
                    controlPanel,
                    GroupLayout.PREFERRED_SIZE,
                    GroupLayout.PREFERRED_SIZE,
                    GroupLayout.PREFERRED_SIZE
                )
                .addGap(4)
        )
        gl.setHorizontalGroup(
            gl.createSequentialGroup()
                .addGap(4)
                .addGroup(
                    gl.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(
                            mainPanel,
                            (dim.width * 0.9).toInt(),
                            GroupLayout.DEFAULT_SIZE,
                            GroupLayout.DEFAULT_SIZE
                        )
                        .addComponent(controlPanel)
                )
                .addGap(4)
        )

        val gl2 = GroupLayout(controlPanel)
        controlPanel.layout = gl2

        gl2.setVerticalGroup(
            gl2.createSequentialGroup()
                .addGap(4)
                .addGroup(
                    gl2.createParallelGroup(
                        GroupLayout.Alignment.CENTER
                    )
                        .addGroup(
                            gl2.createSequentialGroup()
                                .addComponent(
                                    cbColor,
                                    GroupLayout.PREFERRED_SIZE,
                                    GroupLayout.PREFERRED_SIZE,
                                    GroupLayout.PREFERRED_SIZE
                                )
                                .addGap(4)
                                .addComponent(
                                    cbProp,
                                    GroupLayout.PREFERRED_SIZE,
                                    GroupLayout.PREFERRED_SIZE,
                                    GroupLayout.PREFERRED_SIZE
                                )
                        )
                        .addComponent(
                            btnSaveImg,
                            GroupLayout.PREFERRED_SIZE,
                            GroupLayout.PREFERRED_SIZE,
                            GroupLayout.PREFERRED_SIZE
                        )
                        .addComponent(
                            btnExit,
                            GroupLayout.PREFERRED_SIZE,
                            GroupLayout.PREFERRED_SIZE,
                            GroupLayout.PREFERRED_SIZE
                        )

                )
                .addGap(4)
        )
        gl2.setHorizontalGroup(
            gl2.createSequentialGroup()
                .addGap(4)
                .addGroup(
                    gl2.createParallelGroup(
                        GroupLayout.Alignment.LEADING
                    )
                        .addComponent(cbColor)
                        .addComponent(cbProp)

                )
                .addGap(4, 4, Int.MAX_VALUE)
                .addComponent(btnSaveImg)
                .addGap(4)
                .addComponent(btnExit)
                .addGap(4)
        )

        pack()
        painter.plane.realWidth = mainPanel.width
        painter.plane.realHeight = mainPanel.height
        isVisible = true
    }

    private fun setColorScheme() {
        val cs = if (cbColor.isSelected) cs3 else cs0
        painter.setColorScheme(cs)
    }

    private fun getFileName(fileFilter: FileNameExtensionFilter, parent: Component? = null): String? {
        var s: String? = null
        val d = JFileChooser()
        d.isAcceptAllFileFilterUsed = false
        d.fileFilter = fileFilter
        d.currentDirectory = File(".")
        d.dialogTitle = "Сохранить файл"
        d.approveButtonText = "Сохранить"
        val res: Int = d.showSaveDialog(parent)
        if (res == JFileChooser.APPROVE_OPTION) {
            s = d.selectedFile.absolutePath ?: ""
            if (!d.fileFilter.accept(d.selectedFile)) {
                s += "." + (fileFilter?.extensions?.get(0) ?: "")
            }
        }
        return s
    }

    private fun saveImageFile(img: BufferedImage, parent: Component? = null): Boolean {
        val filefilter = FileNameExtensionFilter("JPG File", "jpg")
        val fileName = getFileName(filefilter, parent)
        if (fileName != null) {
            val res = saveImage(fileName, img)
            return res
        }
        return true
    }

    private fun saveImage(fileName: String, img: BufferedImage): Boolean =
        saveImage(File(fileName), img)


    private fun saveImage(file: File, img: BufferedImage): Boolean {
        var ok = false
        if (!file.exists() || file.canWrite()) {
            var wr: FileImageOutputStream? = null
            try {
                wr = FileImageOutputStream(file)
                val iwr = ImageIO.getImageWritersByFormatName("JPG").next()
                val iwp = iwr.defaultWriteParam
                iwp.compressionMode = ImageWriteParam.MODE_EXPLICIT
                iwp.compressionQuality = 1F
                iwr.output = wr
                val iioi = IIOImage(img, null, null)
                iwr.write(null, iioi, iwp)
                ok = true
            } catch (ex: Exception) {
                println(ex)
            } finally {
                wr?.close()
                return ok
            }
        }
        return ok
    }
}