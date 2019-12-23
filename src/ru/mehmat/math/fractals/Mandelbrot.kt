package ru.mehmat.math.fractals

import ru.mehmat.math.Complex

class Mandelbrot(var n:Int) {
    var minIter=50
    var maxIter = minIter
    val R = 2.0
    fun isInSet(x: Double, y: Double): Float {
        val c = Complex(x, y)
        var z = Complex()
        val R2 = R*R
        for (i in 1..maxIter){
            z = z.deg(z, n) + c
            if (z.arg2 > R2){
                return i.toFloat() / maxIter
            }
        }
        return 0.0F
    }

    fun isInSetJulia(x: Double, y: Double,x_c:Double,y_c:Double): Float {
        val c = Complex(x_c, y_c)
        var z = Complex(x,y)
        val R2 = R*R
        for (i in 1..maxIter){
            z = z*z + c
            if (z.arg2 > R2){
                return i.toFloat() / maxIter
            }
        }
        return 0.0F
    }
}