package com.github.viktortomkovic.ogpxviewer.debug

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.github.viktortomkovic.ogpxviewer.R
import org.opencv.android.Utils
import org.opencv.core.Mat

class DebugAdapter: RecyclerView.Adapter<DebugAdapter.ImageViewHolder>() {

    private val data: MutableList<Bitmap> = ArrayList()

    class ImageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val debugView: ImageView = itemView.findViewById(R.id.debug_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val viewHolder = layoutInflater.inflate(R.layout.debug_item, parent, false)
        return ImageViewHolder(viewHolder)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val bitmap = data[position]

        holder.debugView.setImageBitmap(bitmap)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun add(bitmap: Bitmap) {
        data.add(bitmap)
    }

    fun add(mat: Mat, bitmap: Bitmap) {
        val copyBitmap = bitmap.copy(bitmap.config, true)
        Utils.matToBitmap(mat, copyBitmap)
        data.add(copyBitmap)
    }

    fun reset() {
        data.clear()
    }
}