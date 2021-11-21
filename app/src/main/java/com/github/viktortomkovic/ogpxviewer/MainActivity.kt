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
import androidx.recyclerview.widget.RecyclerView
import com.github.viktortomkovic.ogpxviewer.debug.DebugAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc


class MainActivity : AppCompatActivity() {
    private var isInColor: Boolean = true
    private val debug: DebugAdapter = DebugAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        OpenCVLoader.initDebug()
        val button: FloatingActionButton = findViewById(R.id.floatingActionButton)
        findViewById<RecyclerView>(R.id.debugView).adapter = debug
        button.setOnClickListener {
            run {
                debug.reset()
                transformImages2(R.drawable.sitina)
                transformImages2(R.drawable.nightrun)
                transformImages2(R.drawable.owalk)
                debug.notifyDataSetChanged()
                isInColor = !isInColor
            }
        }
/*        val imageView: ImageView = findViewById(R.id.originalImage)
        imageView.setImageResource(imageId)

 */
        debug.add(BitmapFactory.decodeResource(getResources(), R.drawable.sitina))
    }

    private fun transformImages2(imageId: Int) {
        identifyColor2(imageId)
    }

    private fun transformImages() {
/*
        identifyColor(R.id.imageViewSitina, R.drawable.sitina)
        identifyColor(R.id.imageViewNightRun, R.drawable.nightrun)
        identifyColor(R.id.imageViewOwalk, R.drawable.owalk)
 */
    }

    private fun identifyColor(@IdRes imageViewId: Int, @DrawableRes sourceId: Int) {
        var bitmap: Bitmap = BitmapFactory.decodeResource(resources, sourceId)
        if (isInColor) {
            val color = Color.valueOf(getColor(R.color.northLineMine))
//            bitmap = filterColorByRGBDifference(bitmap, color)
//            bitmap = filterColorByHueRange(bitmap, Range<Double>(130.0,150.0), Range<Double>(50.0,100.0), Range<Double>(50.0,100.0))
            bitmap = drawCircles(bitmap)
        }
        val imageView: ImageView = findViewById(imageViewId)
        imageView.setImageBitmap(bitmap)
    }

    private fun identifyColor2(@DrawableRes sourceId: Int) {
        var bitmap: Bitmap = BitmapFactory.decodeResource(resources, sourceId)
        identifyHsv2(bitmap)
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
        val thresholdWithMargin = threshold * 0.80
        Imgproc.threshold(mat, mat, thresholdWithMargin, 255.0, Imgproc.THRESH_TOZERO)

        val filteredBitmap = bitmap.copy(bitmap.config, true)
        Utils.matToBitmap(mat, filteredBitmap)

        return filteredBitmap
    }

    private fun filterColorByHueRange(
        bitmap: Bitmap,
        hueRange: Range<Double>,
        saturationRange: Range<Double>,
        valueRange: Range<Double>
    ): Bitmap {
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

    private fun drawCircles(bitmap: Bitmap): Bitmap {
        val convertedBitmap =
            filterColorByRGBDifference(bitmap, Color.valueOf(getColor(R.color.purpleOfficial)))
        val matOrig = Mat()
        Utils.bitmapToMat(bitmap, matOrig)
        // Imgproc.cvtColor(matOrig, matOrig, Imgproc.COLOR_RGB2HSV_FULL)
        val mat = Mat()
        Utils.bitmapToMat(convertedBitmap, mat)

        val matCircles = Mat()
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY)
        Imgproc.threshold(mat, mat, 128.0, 255.0, Imgproc.THRESH_BINARY_INV)

//        Imgproc.medianBlur(mat, mat, 3)

        Imgproc.HoughCircles(
            mat,
            matCircles,
            Imgproc.CV_HOUGH_GRADIENT,
            1.0,
            20.0,
            100.0,
            20.0,
            5,
            50
        )

        for (i in 0 until matCircles.cols()) {
            val circleParameters = matCircles.get(0, i)
            val center = Point(circleParameters[0], circleParameters[1])
            val radius = circleParameters[2].toInt()
            Imgproc.circle(matOrig, center, radius, Scalar(00.0, 100.0, 00.0), 5, 4, 0)
        }

        val bitmapWithCircles = bitmap.copy(convertedBitmap.config, true)
        Utils.matToBitmap(matOrig, bitmapWithCircles)

        return bitmapWithCircles

    }

    private fun identifyHsv2(bitmap: Bitmap): Bitmap {
        val matOrig = Mat()
        Utils.bitmapToMat(bitmap, matOrig)

        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        val hsvMat = Mat()
        Imgproc.cvtColor(matOrig, hsvMat, Imgproc.COLOR_RGB2HSV)
        debug.add(mat, bitmap)

        val histogram = Mat.ones(1, 180, CvType.CV_8UC1)
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY)

        val mask = Mat.ones(Size(bitmap.width.toDouble(), bitmap.height.toDouble()), CvType.CV_8UC1)

        val hist_bins = 30 //number of histogram bins

        val hist_range = intArrayOf(0, 180) //histogram range

        val ranges = MatOfFloat(0f, 256f)
        val histSize = MatOfInt(25)

        Imgproc.calcHist(listOf(hsvMat), MatOfInt(0), Mat(), histogram, histSize, ranges)
        // debug.add(histogram, bitmap)

        Core.inRange(hsvMat, Scalar(150.0, 10.0, 10.0), Scalar(200.0, 255.0, 255.0), mask)
        debug.add(mask, bitmap)
        Core.inRange(hsvMat, Scalar(130.0, 5.0, 5.0), Scalar(220.0, 250.0, 250.0), mask)
        debug.add(mask, bitmap)
        Core.inRange(hsvMat, Scalar(130.0, 10.0, 10.0), Scalar(220.0, 255.0, 255.0), mask)
        debug.add(mask, bitmap)


        val bitmapWithCircles = bitmap.copy(bitmap.config, true)
        Utils.matToBitmap(matOrig, bitmapWithCircles)

        return bitmapWithCircles
    }
}
