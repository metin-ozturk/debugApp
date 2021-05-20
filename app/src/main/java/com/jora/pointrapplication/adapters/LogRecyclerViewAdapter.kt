package com.jora.pointrapplication.adapters

import android.graphics.Color
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jora.pointrapplication.R
import com.jora.pointrapplication.helpers.Constants
import com.jora.pointrapplication.model.LogType
import com.jora.pointrapplication.model.PointrLog

abstract class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view)

class LogRecyclerViewAdapter() : RecyclerView.Adapter<BaseViewHolder>() {
    private var pointrLogList: ArrayList<PointrLog> = ArrayList()
    private var currentFilterType = LogType.Debug
    private val filteredLogList: ArrayList<PointrLog>
        get() {
            return when(currentFilterType) {
                LogType.Verbose -> pointrLogList
                LogType.Debug -> pointrLogList.filter { it.type != LogType.Verbose } as ArrayList<PointrLog>
                LogType.Info -> pointrLogList.filter { it.type != LogType.Verbose && it.type != LogType.Debug } as ArrayList<PointrLog>
                LogType.Warning -> pointrLogList.filter { it.type == LogType.Warning || it.type == LogType.Error } as ArrayList<PointrLog>
                LogType.Error -> pointrLogList.filter { it.type == LogType.Error } as ArrayList<PointrLog>
            }

        }

    inner class LogRecyclerItem(view: View) : BaseViewHolder(view) {
        internal var content = view.findViewById<TextView>(R.id.logItemContentTextView)
        internal var date = view.findViewById<TextView>(R.id.logItemTypeTextView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_log_recycler_view, parent, false)
        return LogRecyclerItem(itemView)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val retrievedHolder = holder as LogRecyclerItem
        val currentLog = filteredLogList[position]
        retrievedHolder.content.text = if (currentLog.date != null) currentLog.content.substring(19) else currentLog.content
        if (currentLog.date != null) retrievedHolder.date.text = Html.fromHtml("<h5> ${currentLog.content.substring(0, 18)} <h5>") else retrievedHolder.date.text = ""
        if (currentLog.type == LogType.Error || currentLog.type == LogType.Warning) retrievedHolder.content.setTextColor(Color.RED) else  retrievedHolder.content.setTextColor(Color.BLACK)
    }

    override fun getItemCount(): Int {
        return filteredLogList.size
    }

    fun updateLogList(newPointrLog: PointrLog) {
        if (newPointrLog.type == null || newPointrLog.date == null) return

        pointrLogList.add(newPointrLog)
        if (pointrLogList.size >= Constants.MaximumLogCount.value) {
            pointrLogList.subList(0, Constants.MaximumLogCount.value / 5).clear()
        }
        notifyDataSetChanged()

    }

    fun filterLogList(type: LogType) {
        currentFilterType = type
        notifyDataSetChanged()
    }

    fun getLastItemIdx() : Int = filteredLogList.size - 1
}