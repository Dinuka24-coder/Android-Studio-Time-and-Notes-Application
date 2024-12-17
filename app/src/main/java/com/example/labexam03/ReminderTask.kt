package com.example.labexam03

data class ReminderTask(
    val id: Int,
    var description: String,
    var hour: Int,
    var minute: Int,
    var isDone: Boolean // Updated to include isDone
)



