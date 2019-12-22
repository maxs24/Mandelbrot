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
import java.io.*
import java.lang.Exception
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.stream.FileImageOutputStream
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.math.*
import kotlin.system.exitProcess
import javax.swing.JOptionPane
import javax.swing.JFileChooser
import java.io.IOException
import java.io.FileWriter





class Window : JFrame(),  ActionListener{

    override fun actionPerformed(p0: ActionEvent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    //private var controlPanel: JPanel
/*private val btnExit: JButton
         private val cbColor: JCheckBox
    private val cbProp: JCheckBox
         private val btnSaveImg: JButton*/
    private val mainPanel: MainPanel

    private val dim: Dimension

    private val painter: FractalPainter

    private val disXmin = -1.5
    private val disXmax = 1.5
    private val disYmin = -1.5
    private val disYmax =  1.5

    private val menubar: JMenuBar
    private val menuFile: JMenu

    private var discharge: JMenuItem
    private var save: JMenuItem
    private var open: JMenuItem
    private var close: JMenuItem

    private val menuFractal: JMenu
    private val subColor: JMenu
    private var itm_subColor: JMenuItem
    private var iterFractal: JCheckBoxMenuItem
    private var prop: JCheckBoxMenuItem
    private var subType: JMenu
    private var typeS2: JMenuItem
    private var typeS3: JMenuItem
    private var typeS4: JMenuItem
    private var typeS12: JMenuItem
    private var julia: JMenuItem
    private var anim: JMenuItem


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
            disXmin,
            disXmax,
            disYmin,
            disYmax
        )

////////
        val m = Mandelbrot(2)
        painter = FractalPainter(plane, m)
        mainPanel = MainPanel(painter)
        //controlPanel = JPanel()


        // создаем панель меню
        menubar = JMenuBar()
        // создаем 1е меню
        menuFile = JMenu("Файл")
        // ------------------------------------
        // добавление простых элементов меню
        // элемент 1
        discharge = JMenuItem("Исходная область")
        menuFile.add(discharge)
        //вызов определяется внизу после анимации


        // элемент 2
        save = JMenuItem("Сохранить как..")

        menuFile.add(save)

        open = JMenuItem("Открыть..")
        menuFile.add(open)
        open.addActionListener(this)


        close = JMenuItem("Закрыть")
        //itm.addActionListener(this)
        close.addActionListener {
            //закрыть окно по нажатию
            exitProcess(0)
        }
        menuFile.add(close)
        menubar.add(menuFile)


        // добавляем панель меню в окно
        menuFractal = JMenu("Фрактал")

        //подменю цветовых схем
        subColor = JMenu("Цветовая схема")
        //val submenu = JMenu("Sub")
        itm_subColor = JMenuItem("Черно-белая")
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

        iterFractal = JCheckBoxMenuItem("Динамические итерации")
        iterFractal.addActionListener{
            if (iterFractal.isSelected) {
                mainPanel.dinIter = true
                val coeffIncrease =(35/painter.fractal.minIter.toDouble())*ln(
                    mainPanel.getSquare / ((painter.plane.xMax - painter.plane.xMin)
                            * (painter.plane.yMax - painter.plane.yMin))
                )
                if (coeffIncrease - 1 > 1e-10) painter.fractal.maxIter =
                    (painter.fractal.minIter * coeffIncrease).toInt()
            }else{
                mainPanel.dinIter=false
                painter.fractal.maxIter=painter.fractal.minIter
            }
            painter.created=false
            mainPanel.repaint()
        }
        menuFractal.add(iterFractal)

        prop = JCheckBoxMenuItem("Соблюдение пропорций")
        prop.addActionListener  {
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
        subType = JMenu("Тип фрактала")
        typeS2 = JMenuItem("Множество Мандельброта для степени 2")
        typeS2.addActionListener {
            painter.plane.xMin = disXmin
            painter.plane.xMax = disXmax
            painter.plane.yMin = disYmin
            painter.plane.yMax = disYmax
            m.n = 2
            painter.created = false
            mainPanel.repaint()
        }
        subType.add(typeS2)

        typeS3 = JMenuItem("Множество Мандельброта для степени 3")
        typeS3.addActionListener {
            painter.plane.xMin = disXmin
            painter.plane.xMax = disXmax
            painter.plane.yMin = disYmin
            painter.plane.yMax = disYmax
            m.n = 3
            painter.created = false
            mainPanel.repaint()
        }
        subType.add(typeS3)

        typeS4 = JMenuItem("Множество Мандельброта для степени 4")
        typeS4.addActionListener {
            painter.plane.xMin = disXmin
            painter.plane.xMax = disXmax
            painter.plane.yMin = disYmin
            painter.plane.yMax = disYmax
            m.n = 4
            painter.created = false
            mainPanel.repaint()
        }
        subType.add(typeS4)
        typeS12 = JMenuItem("Множество Мандельброта для степени 12")
        typeS12.addActionListener {
            painter.plane.xMin = disXmin
            painter.plane.xMax = disXmax
            painter.plane.yMin = disYmin
            painter.plane.yMax = disYmax
            m.n = 12
            painter.created = false
            mainPanel.repaint()
        }
        subType.add(typeS12)

        julia = JMenuItem("Множество Жулиа")
        julia.addActionListener {
            var x: Int
            var y: Int
        }
        subType.add(julia)

        menuFractal.add(subType)

        //Анимация
        anim = JMenuItem("Анимация")
        anim.addActionListener{
            EditWindow()
        }
        menuFractal.add(anim)

        discharge.addActionListener{
            painter.plane.xMin = disXmin
            painter.plane.xMax = disXmax
            painter.plane.yMin = disYmin
            painter.plane.yMax = disYmax
            painter.created = false

            val cs = cs0
            painter.setColorScheme(cs)

            painter.proportion = false
            if(prop.isSelected){
                prop.isSelected=false
            }


            if(iterFractal.isSelected){
                iterFractal.isSelected=false
            }

            mainPanel.startApprox=false
            mainPanel.dinIter=false
            painter.fractal.maxIter=painter.fractal.minIter
            mainPanel.repaint()
        }
        save.addActionListener {

            val IMG= painter.buf?.let { it1 -> BufferedImageWrap(it1) }
            val ab= SaveClass();
            ab.xmin=painter.plane.xMin
            ab.xmax=painter.plane.xMax
            ab.ymax=painter.plane.yMax
            ab.ymin=painter.plane.yMin
            ab.IMG=IMG
            ab.proportion=prop.isSelected
            if (painter.cs==cs0){ab.COLOR_CHEM=0}
            else if (painter.cs==cs1){ab.COLOR_CHEM=1}
            else if (painter.cs==cs2){ab.COLOR_CHEM=2}
            else if (painter.cs==cs3){ab.COLOR_CHEM=3 }

            ab.DimamikIteration=iterFractal.isSelected





            ab.typeFracta=painter.fractal.n
            //ab.Height=painter.plane.realHeight
            //ab.Width=painter.plane.realWidth
            ab.Width=mainPanel.width
            ab.Height=mainPanel.height


            var filefilter = FileNameExtensionFilter("CLASS FRACTAL", "frc")
            var s: String? = null
            val d = JFileChooser()
            d.isAcceptAllFileFilterUsed = false
            d.fileFilter = filefilter
            d.currentDirectory = File(".")
            d.dialogTitle = "Сохранить файл"
            d.approveButtonText = "Сохранить"
            val res: Int = d.showSaveDialog(parent)
            if (res == JFileChooser.APPROVE_OPTION) {
                s = d.selectedFile.absolutePath ?: ""
                if (!d.fileFilter.accept(d.selectedFile)) {
                    s += "." + (filefilter?.extensions?.get(0) ?: "")
                }
            }

            val fileOutputStream = FileOutputStream(s)
            val objectOutputStream = ObjectOutputStream(fileOutputStream)
            objectOutputStream.writeObject(ab)
            objectOutputStream.close()
            //println(ab.Height)





        }
        open.addActionListener{
            var filefilter = FileNameExtensionFilter("CLASS FRACTAL", "frc")

            val d = JFileChooser()
            d.isAcceptAllFileFilterUsed = false
            d.fileFilter = filefilter
            d.currentDirectory = File(".")
            d.dialogTitle="Выберите файл"
            d.approveButtonText = "Выбрать"
            // Определяем фильтры типов файлов
            d.addChoosableFileFilter(filefilter)
            // Определение режима - только файл
            d.fileSelectionMode=JFileChooser.FILES_ONLY
            val result = d.showOpenDialog(null)

            if (result == JFileChooser.APPROVE_OPTION ) {//если выбрали файл


                val fileInputStream = FileInputStream(d.selectedFile)
                val objectInputStream = ObjectInputStream(fileInputStream)
                var bc = objectInputStream.readObject() as SaveClass
                objectInputStream.close()

                painter.readyBuf = bc.IMG?.image
                prop.isSelected = bc.proportion
                painter.proportion = bc.proportion

                if (bc.COLOR_CHEM == 0) {
                    painter.setColorScheme(cs0)
                } else if (bc.COLOR_CHEM == 1) {
                    painter.setColorScheme(cs1)
                } else if (bc.COLOR_CHEM == 2) {
                    painter.setColorScheme(cs2)
                } else if (bc.COLOR_CHEM == 3) {
                    painter.setColorScheme(cs3)
                }


                iterFractal.isSelected = bc.DimamikIteration
                mainPanel.dinIter = bc.DimamikIteration



                painter.fractal.n = bc.typeFracta
                painter.plane.xMax = bc.xmax
                painter.plane.xMin = bc.xmin
                painter.plane.yMax = bc.ymax
                painter.plane.yMin = bc.ymin

                this.setSize(Dimension(bc.Width, bc.Height))


                //bc.IMG?.image?.let { it1 -> painter.paints(graphics, it1) }
                // painter.readyBuf= bc.IMG?.image

                //painter.created=true
                var g=mainPanel.graphics
                // var g= painter.buf?.graphics
                if (g != null) {
                    g.drawImage(bc.IMG?.image, 0, 0, plane.realWidth, plane.realHeight, null)
                }


                if (iterFractal.isSelected) {
                    mainPanel.dinIter = true
                    val coeffIncrease = (35 / painter.fractal.minIter.toDouble()) * ln(
                        mainPanel.getSquare / ((painter.plane.xMax - painter.plane.xMin)
                                * (painter.plane.yMax - painter.plane.yMin))
                    )
                    if (coeffIncrease - 1 > 1e-10) painter.fractal.maxIter =
                        (painter.fractal.minIter * coeffIncrease).toInt()
                } else {
                    mainPanel.dinIter = false
                    painter.fractal.maxIter = painter.fractal.minIter
                }

                if (bc.COLOR_CHEM == 0) {
                    painter.setColorScheme(cs0)
                } else if (bc.COLOR_CHEM == 1) {
                    painter.setColorScheme(cs1)
                } else if (bc.COLOR_CHEM == 2) {
                    painter.setColorScheme(cs2)
                } else if (bc.COLOR_CHEM == 3) {
                    painter.setColorScheme(cs3)
                }



            }

        }



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
internal class BufferedImageWrap(image: BufferedImage) : Serializable {
    val width: Int
    val height: Int
    var colors: Array<IntArray>? = null


    val image: BufferedImage
        get() {
            val result = BufferedImage(
                width,
                height, BufferedImage.TYPE_INT_RGB
            )
            for (i in 0 until width)
                for (j in 0 until height)
                    result.setRGB(i, j, colors!![i][j])

            return result
        }

    init {
        this.width = image.width
        this.height = image.height
        this.colors = Array(width) { IntArray(height) }
        for (x in 0 until width)
            for (y in 0 until height)
                colors!![x][y] = image.getRGB(x, y)
    }

}


internal class SaveClass : Serializable {
    var xmin: Double = 0.0
    var xmax: Double = 0.0
    var ymin: Double = 0.0
    var ymax: Double = 0.0
    var proportion:Boolean=false
    var IMG:BufferedImageWrap? =null
    var COLOR_CHEM:Int=0
    var DimamikIteration:Boolean=false
    var typeFracta:Int =0
    var Height:Int=0
    var Width :Int=0


}


