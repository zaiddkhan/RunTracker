package com.example.runtracker.adapters

import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runtracker.R
import com.example.runtracker.TrackingUtility
import com.example.runtracker.db.Run
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter: RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    inner class RunViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)

    val diffCallBack = object : DiffUtil.ItemCallback<Run>(){
        override fun areItemsTheSame(oldItem:Run,newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    val differ = AsyncListDiffer(this,diffCallBack)

    fun submitList(list:List<Run>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_run,parent,false)
        )
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = differ.currentList[position]
        holder.itemView.apply {
            Glide.with(this).load(run.img).into(findViewById(R.id.ivRunImage))

            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timeInMillis
            }

            val dateFormat = SimpleDateFormat("dd.MM.yy",Locale.getDefault())
            findViewById<TextView>(R.id.tvDate).text = dateFormat.format(calendar.time)

            val avgSpeed = "${run.avgSpeedInKMPH}km/h"
            findViewById<TextView>(R.id.tvAvgSpeed).text = avgSpeed

            val distanceInKms = "${run.distanceInMetres / 1000f}km"
            findViewById<TextView>(R.id.tvDistance).text = distanceInKms

            findViewById<TextView>(R.id.tvTime).text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

            val caloriesBurned = "${run.caloriesBurnt}kcal"
            findViewById<TextView>(R.id.tvCalories).text = caloriesBurned


        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}