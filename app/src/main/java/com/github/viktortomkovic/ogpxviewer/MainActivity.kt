package com.github.viktortomkovic.ogpxviewer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class MainActivity : AppCompatActivity() {
    private var isInColor: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        OpenCVLoader.initDebug()
        val button: FloatingActionButton = findViewById(R.id.floatingActionButton)
        button.setOnClickListener {
            run {
                transformImages()
                isInColor = !isInColor
            }
        }
    }

    private fun transformImages() {
        identifyPurple(R.id.imageViewSitina, R.drawable.sitina)
        identifyPurple(R.id.imageViewNightRun, R.drawable.nightrun)
        identifyPurple(R.id.imageViewOwalk, R.drawable.owalk)
    }

    private fun identifyPurple(@IdRes imageViewId: Int, @DrawableRes sourceId: Int) {
        var bitmap: Bitmap = BitmapFactory.decodeResource(resources, sourceId)
        if (isInColor) {
            bitmap = filterPurple(bitmap)
        }
        val imageView: ImageView = findViewById(imageViewId)
        imageView.setImageBitmap(bitmap)
    }

    private fun filterPurple(bitmap: Bitmap): Bitmap {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        val purpleColor = Color.valueOf(getColor(R.color.purpleMap2))
        val purple: Scalar = Scalar(
            purpleColor.red().toDouble()*255,
            purpleColor.green().toDouble()*255, purpleColor.blue().toDouble()*255
        )
        Core.absdiff(mat, purple, mat)
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY)
        Core.bitwise_not(mat, mat)
        val threshold = Core.minMaxLoc(mat).maxVal
        val thresholdWithMargin = threshold * 0.80
        Imgproc.threshold(mat, mat, thresholdWithMargin, 255.0, Imgproc.THRESH_TOZERO)

        val purpleBitmap = bitmap.copy(bitmap.config, true)
        Utils.matToBitmap(mat, purpleBitmap)

        return purpleBitmap
    }
}