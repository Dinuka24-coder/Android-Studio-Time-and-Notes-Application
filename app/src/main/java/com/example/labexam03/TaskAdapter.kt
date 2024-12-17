package com.example.labexam03

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lab03ex.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TaskAdapter(
    private val taskList: MutableList<String>,
    private val taskActionListener: TaskActionListener,
    private val context: Context
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("task_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // To track the checkbox states
    private val checkboxStates = mutableMapOf<String, Boolean>()

    init {
        loadCheckboxStates()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.tvTask.text = task

        // Load the checkbox state for this task
        holder.checkboxTask.isChecked = checkboxStates[task] ?: false

        // Save the checkbox state when it's clicked
        holder.checkboxTask.setOnCheckedChangeListener { _, isChecked ->
            checkboxStates[task] = isChecked
            saveCheckboxStates()
        }

        // Edit task on long press
        holder.btnEdit.setOnClickListener {
            taskActionListener.onEdit(position)
        }

        // Delete task on button click
        holder.btnDelete.setOnClickListener {
            taskActionListener.onDelete(position)
        }
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    // ViewHolder to reference views in item_task.xml
    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTask: TextView = itemView.findViewById(R.id.tvTask)
        val btnEdit: TextView = itemView.findViewById(R.id.btnEdit)
        val btnDelete: TextView = itemView.findViewById(R.id.btnDelete)
        val checkboxTask: CheckBox = itemView.findViewById(R.id.checkboxTask)
    }

    // Save checkbox states in SharedPreferences
    private fun saveCheckboxStates() {
        val json = gson.toJson(checkboxStates)
        sharedPreferences.edit().putString("checkbox_states", json).apply()
    }

    // Load checkbox states from SharedPreferences
    private fun loadCheckboxStates() {
        val json = sharedPreferences.getString("checkbox_states", null)
        val type = object : TypeToken<MutableMap<String, Boolean>>() {}.type
        val savedStates: MutableMap<String, Boolean>? = gson.fromJson(json, type)
        if (savedStates != null) {
            checkboxStates.putAll(savedStates)
        }
    }

    // Interface for edit and delete task actions
    interface TaskActionListener {
        fun onEdit(position: Int)
        fun onDelete(position: Int)
    }
}
