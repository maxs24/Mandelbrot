package ru.mehmat.graphics.windows

import ru.mehmat.graphics.convertation.CartesianScreenPlane
import ru.mehmat.graphics.painters.FractalPainter
import ru.mehmat.graphics.windows.components.MainPanel
import ru.mehmat.math.fractals.Mandelbrot
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
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
import kotlin.system.exitProcess

class Window : JFrame(),  ActionListener{

    override fun actionPerformed(p0: ActionEvent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val mainPanel: MainPanel
    //private var controlPanel: JPanel
    /*private val btnExit: JButton
    private val cbColor: JCheckBox
    private val cbProp: JCheckBox
    private val btnSaveImg: JButton*/

    private val dim: Dimension

    private val painter: FractalPainter

    private val cs0: (Float) -> Color = {
        if (abs(it) < 1e-10) Color.BLACK else Color.WHITE
    }
    private val cs1: (Float) -> Color = {
        Color(
            108*it/255,
            20*it/255,
            180*it/255
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

        var plane = CartesianScreenPlane(
            -1,
            -1,
            -1.5,
            1.5,
            -1.5,
            1.5
        )

////////
        val m = Mandelbrot(2)
        painter = FractalPainter(plane, m)
        mainPanel = MainPanel(painter)
        //controlPanel = JPanel()

        var q = false


        // создаем панель меню
        val menubar = JMenuBar()
        // создаем 1е меню
        val menuFile = JMenu("Файл")
        // ------------------------------------
        // добавление простых элементов меню
        // элемент 1
        var itmFile = JMenuItem("Исходная область")
        menuFile.add(itmFile)
        itmFile.addActionListener(this)
        // элемент 2
        itmFile = JMenuItem("Сохранить как..")
        itmFile.addActionListener {
            painter.buf?.let {
                val buf = BufferedImage(it.width, it.height + 100, BufferedImage.TYPE_INT_RGB)
                buf.graphics.drawImage(it, 0, 0, it.width, it.height, null)
                buf.graphics.color = Color.white
                //buf.graphics.fillRect(0,it.height,it.width,it.height+100)
                //buf.graphics.color= Color.black
                buf.graphics.drawString("xmin= " + plane.xMin, 10, it.height + 70)
                buf.graphics.drawString("xmax= " + plane.xMax, 10, it.height + 40)
                buf.graphics.drawString("ymin= " + plane.yMin, it.width / 2, it.height + 70)
                buf.graphics.drawString("ymax= " + plane.yMax, it.width / 2, it.height + 40)
                saveImageFile(buf, this)
            }
        }
        menuFile.add(itmFile)
        // элемент 3
        itmFile = JMenuItem("Закрыть")
        //itm.addActionListener(this)
        itmFile.addActionListener {
            //закрыть окно по нажатию
            exitProcess(0)
        }
        menuFile.add(itmFile)
        menubar.add(menuFile)


        // добавляем панель меню в окно
        val menuFractal = JMenu("Фрактал")

        //подменю цветовых схем
        var subColor = JMenu("Цветовая схема")
        //val submenu = JMenu("Sub")
        var itm_subColor = JMenuItem("Черно-белая")
        itm_subColor.addActionListener {
            val cs = cs0
            painter.setColorScheme(cs)
        }
        subColor.add(itm_subColor)
        itm_subColor = JMenuItem("Схема 1")
        itm_subColor.addActionListener {
            val cs = cs1
            painter.setColorScheme(cs)
        }
        subColor.add(itm_subColor)
        itm_subColor = JMenuItem("Схема 2")
        itm_subColor.addActionListener {
            val cs = cs2
            painter.setColorScheme(cs)
        }
        subColor.add(itm_subColor)
        itm_subColor = JMenuItem("Схема 3")
        itm_subColor.addActionListener {
            val cs = cs3
            painter.setColorScheme(cs)
        }
        subColor.add(itm_subColor)
        // добавляем вложенное меню
        menuFractal.add(subColor)

        var iterFractal = JCheckBoxMenuItem("Динамические итерации")
        iterFractal.addActionListener(this)
        menuFractal.add(iterFractal)

        var prop = JCheckBoxMenuItem("Соблюдение пропорций")
        prop.addActionListener {
            painter.proportion = prop.isSelected
            if (prop.isSelected) {
                painter.xmin = painter.plane.xMin
                painter.xmax = painter.plane.xMax
                painter.ymin = painter.plane.yMin
                painter.ymax = painter.plane.yMax
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
            } else {
                painter.plane.xMin = painter.xmin
                painter.plane.xMax = painter.xmax
                painter.plane.yMin = painter.ymin
                painter.plane.yMax = painter.ymax
            }
            painter.created = false
            mainPanel.repaint()
        }
        menuFractal.add(prop)


        //подменю типов фрактала
        var subType = JMenu("Тип фрактала")
        var typeS2 = JMenuItem("Множество Мандельброта для степени 2")
        typeS2.addActionListener {
            m.n = 2
            painter.created = false
            mainPanel.repaint()
        }
        subType.add(typeS2)

        var typeS3 = JMenuItem("Множество Мандельброта для степени 3")
        typeS3.addActionListener {
            m.n = 3
            painter.created = false
            mainPanel.repaint()
        }
        subType.add(typeS3)

        var typeS4 = JMenuItem("Множество Мандельброта для степени 4")
        typeS4.addActionListener {
            m.n = 4
            painter.created = false
            mainPanel.repaint()
        }
        subType.add(typeS4)

        var typeS12 = JMenuItem("Множество Мандельброта для степени 12")
        typeS12.addActionListener {
            m.n = 12
            painter.created = false
            mainPanel.repaint()
        }
        subType.add(typeS12)

        var julia = JMenuItem("Множество Жулиа")
        julia.addActionListener {
            var x: Int
            var y: Int
        }
        subType.add(julia)

        menuFractal.add(subType)

        //Анимация
        var anim = JMenuItem("Анимация")
        anim.addActionListener(this)
        menuFractal.add(anim)

        menubar.add(menuFractal)

        jMenuBar = menubar
        title = "Фрактал" // заголовок окна

        val gl = GroupLayout(contentPane)
        layout = gl
        gl.setVerticalGroup(
            gl.createSequentialGroup()
                .addGap(4)
                .addComponent(
                    mainPanel,
                    (dim.height).toInt(),
                    GroupLayout.DEFAULT_SIZE,
                    GroupLayout.DEFAULT_SIZE
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
                            (dim.width).toInt(),
                            GroupLayout.DEFAULT_SIZE,
                            GroupLayout.DEFAULT_SIZE
                        )
                    //.addComponent(controlPanel)
                )
                .addGap(4)
        )
        pack()
        painter.plane.realWidth = mainPanel.width
        painter.plane.realHeight = mainPanel.height
        isVisible = true
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