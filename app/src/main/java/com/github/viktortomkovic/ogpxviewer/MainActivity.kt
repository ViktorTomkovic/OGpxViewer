package com.github.viktortomkovic.ogpxviewer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Range
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
        identifyColor(R.id.imageViewSitina, R.drawable.sitina)
        identifyColor(R.id.imageViewNightRun, R.drawable.nightrun)
        identifyColor(R.id.imageViewOwalk, R.drawable.owalk)
    }

    private fun identifyColor(@IdRes imageViewId: Int, @DrawableRes sourceId: Int) {
        var bitmap: Bitmap = BitmapFactory.decodeResource(resources, sourceId)
        if (isInColor) {
            val color = Color.valueOf(getColor(R.color.northLineMine))
//            bitmap = filterColorByRGBDifference(bitmap, color)
            bitmap = filterColorByHueRange(bitmap, Range<Double>(130.0,150.0), Range<Double>(50.0,100.0), Range<Double>(50.0,100.0))
        }
        val imageView: ImageView = findViewById(imageViewId)
        imageView.setImageBitmap(bitmap)
    }

    private fun filterColorByRGBDifference(bitmap: Bitmap, color: Color): Bitmap {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        val filteringColor = Scalar(
            color.red().toDouble() * 255,
            color.green().toDouble() * 255, color.blue().toDouble() * 255
        )
        Core.absdiff(mat, filteringColor, mat)
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY)
        Core.bitwise_not(mat, mat)
        val threshold = Core.minMaxLoc(mat).maxVal
        val thresholdWithMargin = threshold * 0.85
        Imgproc.threshold(mat, mat, thresholdWithMargin, 255.0, Imgproc.THRESH_TOZERO)

        val filteredBitmap = bitmap.copy(bitmap.config, true)
        Utils.matToBitmap(mat, filteredBitmap)

        return filteredBitmap
    }

    private fun filterColorByHueRange(bitmap: Bitmap, hueRange: Range<Double>, saturationRange: Range<Double>, valueRange: Range<Double>): Bitmap {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2HSV)

        val upper = Scalar(hueRange.upper, saturationRange.upper, valueRange.upper)
        val lower = Scalar(hueRange.lower, saturationRange.lower, valueRange.lower)
        val mask = mat.clone()

        Core.inRange(mat, lower, upper, mask)

        Core.bitwise_and(mat, mat, mask)
        Imgproc.cvtColor(mask, mask, Imgproc.COLOR_RGB2GRAY)

        val filteredBitmap = bitmap.copy(bitmap.config, true)
        Utils.matToBitmap(mat, filteredBitmap)

        return filteredBitmap

    }
}