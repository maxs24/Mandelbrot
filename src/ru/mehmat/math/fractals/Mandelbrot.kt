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
            z = deg(z, n) + c
            if (z.arg2 > R2){
                return i.toFloat() / maxIter
            }
        }
        return 0.0F
    }

    private fun deg(z: Complex, n: Int): Complex{
        var c = Complex(1.0,0.0)
        for(i in 1..n)
        {
            c= c*z
        }
        return c
    }

}