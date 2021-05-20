package com.jora.pointrapplication.fragments

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jora.pointrapplication.R
import com.jora.pointrapplication.adapters.LogRecyclerViewAdapter
import com.jora.pointrapplication.helpers.Constants
import com.jora.pointrapplication.model.PointrLog
import com.jora.pointrapplication.model.LogType
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "Pointr"

class LogFragment : Fragment() {
    private lateinit var logRecyclerView: RecyclerView
    private var viewToBeCreated: View? = null
    private var logRecyclerViewAdapter: LogRecyclerViewAdapter? = null

    private var isRetrievingLogs = AtomicBoolean(false)
    private val timerHandler : Handler by lazy { Handler(Looper.getMainLooper()) }
    private val timerRunnable: Runnable by lazy { Runnable {
        getLogs()
        timerHandler.postDelayed(timerRunnable, Constants.LogUpdateInterval.value.toLong())
    }}

    private var logTypeIdentifier = "D"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewToBeCreated = inflater.inflate(R.layout.fragment_log, container, false)
        setLogRecyclerView()
        logRadioButtons()
        return viewToBeCreated
    }

    override fun onResume() {
        super.onResume()

        timerHandler.postDelayed(timerRunnable, 0)
    }

    override fun onStop() {
        super.onStop()
        timerHandler.removeCallbacks(timerRunnable)
    }

    private fun logRadioButtons() {
        val logRatioButtons = viewToBeCreated?.findViewById<RadioGroup>(R.id.logTypeRadioGroup)
        logRatioButtons?.setOnCheckedChangeListener { _, id ->
            val checkedRadioButton = viewToBeCreated?.findViewById<RadioButton>(id)
            logTypeIdentifier = when (checkedRadioButton?.text ) {
                LogType.Verbose.value -> "V"
                LogType.Debug.value -> "D"
                LogType.Info.value -> "I"
                LogType.Warning.value -> "W"
                LogType.Error.value -> "E"
                else -> "D"
            }
            logRecyclerViewAdapter?.filterLogList(LogType.valueOf(checkedRadioButton!!.text.toString()))
            getLogs()
        }
    }

    private fun setLogRecyclerView() {
        logRecyclerView = viewToBeCreated?.findViewById(R.id.logRecyclerView) ?: return

        logRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@LogFragment.activity).apply { stackFromEnd = true }
            itemAnimator = DefaultItemAnimator()
            logRecyclerViewAdapter = LogRecyclerViewAdapter()
            adapter = logRecyclerViewAdapter
        }
    }

    private fun getLogs() {
        if (isRetrievingLogs.get()) return

        isRetrievingLogs.getAndSet(true)
        Runtime.getRuntime().exec("logcat -c")
        val process = Runtime.getRuntime().exec("logcat -d *:$logTypeIdentifier")
        val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))

        var line : String? = ""
        while (bufferedReader.readLine().also { line = it } != null) {
            val extractedDate = extractDate(line!!.substring(0,18))
            val extractedType = if (extractedDate != null) extractType(line!!) else null
            logRecyclerViewAdapter?.updateLogList(PointrLog(extractedType, extractedDate,line ?: ""))
            logRecyclerView.scrollToPosition(logRecyclerViewAdapter?.getLastItemIdx() ?: 0)
        }

        isRetrievingLogs.getAndSet(false)
    }

    private fun extractDate(dateString: String): Date? {
         try {
             val dateFormat = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.getDefault())
             return dateFormat.parse(dateString)
        } catch(exc: ParseException){
            Log.d(TAG, "Line didn't start with an date: ${exc.localizedMessage}")
        }
        return null
    }

    private fun extractType(source: String): LogType? {
        if (source.count() < 32) return null

        return when (source[31]) {
            'V' -> LogType.Verbose
            'D' -> LogType.Debug
            'I' -> LogType.Info
            'W' -> LogType.Warning
            'E' -> LogType.Error
            else -> null
        }
    }
}