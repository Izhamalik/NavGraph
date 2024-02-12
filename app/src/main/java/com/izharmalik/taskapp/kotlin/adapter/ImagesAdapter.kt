package com.izharmalik.taskapp.kotlin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.izharmalik.taskapp.kotlin.R
import com.izharmalik.taskapp.kotlin.api.Photos

class ImagesAdapter(private val photosList: List<Photos?>, private val itemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<ImagesAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(image: Photos)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.sample_thumb)
        val imageTitle: TextView = itemView.findViewById(R.id.sample_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sample_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val image = photosList[position]
        Glide.with(holder.itemView.context)
            .load(image?.thumbnailUrl)
            .into(holder.imageView)
        holder.imageTitle.text = image?.title
        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(image!!)
        }
    }

    override fun getItemCount(): Int {
        return photosList.size
    }
}