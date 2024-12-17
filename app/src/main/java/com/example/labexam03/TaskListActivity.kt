package com.example.labexam03

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lab03ex.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TaskListActivity : AppCompatActivity(), TaskAdapter.TaskActionListener {

    private lateinit var taskAdapter: TaskAdapter
    private val taskList = mutableListOf<String>()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        // Setup RecyclerView
        val recyclerViewTasks: RecyclerView = findViewById(R.id.recyclerViewTasks)
        taskAdapter = TaskAdapter(taskList, this, this)
        recyclerViewTasks.layoutManager = LinearLayoutManager(this)
        recyclerViewTasks.adapter = taskAdapter

        // Load tasks from SharedPreferences
        loadTasks()

        // Floating Action Button to add tasks
        val fabAddTask: FloatingActionButton = findViewById(R.id.fabAddTask)
        fabAddTask.setOnClickListener {
            showTaskDialog(null, -1)
        }

        // Setup BottomNavigationView for navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Navigate to Home (MainActivity)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_tips -> {
                    // Navigate to Tips (TipsPageActivity)
                    val intent = Intent(this, TipsPageActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    // Navigate to Profile (ProfilePageActivity)
                    val intent = Intent(this, ProfilePageActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun showTaskDialog(existingTask: String?, position: Int) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_task, null)
        builder.setView(dialogView)

        val editTextTask = dialogView.findViewById<EditText>(R.id.editTextTask)
        if (existingTask != null) {
            editTextTask.setText(existingTask)
        }

        builder.setTitle(if (existingTask == null) "Add Task" else "Edit Task")
            .setPositiveButton("OK") { _, _ ->
                val task = editTextTask.text.toString()
                if (existingTask == null) {
                    taskList.add(task)
                    taskAdapter.notifyItemInserted(taskList.size - 1)
                } else {
                    taskList[position] = task
                    taskAdapter.notifyItemChanged(position)
                }
                saveTasks()
            }
            .setNegativeButton("Cancel", null)

        val dialog = builder.create()
        dialog.show()
    }

    private fun saveTasks() {
        val json = gson.toJson(taskList)
        getSharedPreferences("task_prefs", MODE_PRIVATE).edit().putString("task_list", json).apply()
    }

    private fun loadTasks() {
        val sharedPreferences = getSharedPreferences("task_prefs", MODE_PRIVATE)
        val json = sharedPreferences.getString("task_list", null)
        val type = object : TypeToken<MutableList<String>>() {}.type
        val savedTasks: MutableList<String>? = gson.fromJson(json, type)
        if (savedTasks != null) {
            taskList.addAll(savedTasks)
        }
    }

    override fun onEdit(position: Int) {
        showTaskDialog(taskList[position], position)
    }

    override fun onDelete(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Yes") { _, _ ->
                taskList.removeAt(position)
                taskAdapter.notifyItemRemoved(position)
                saveTasks()
            }
            .setNegativeButton("No", null)
            .show()
    }
}
