package com.example.valuefy

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TaskManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("tasks", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveTask(task: String) {
        val tasks = getTasks().toMutableList()

        if (!tasks.contains(task)) { // ✅ Prevents duplicates
            tasks.add(task)
            val editor = sharedPreferences.edit()
            editor.putString("tasks", gson.toJson(tasks))
            editor.apply()
            Log.d("TaskManager", "Task saved: $task") // ✅ Debug Log
        } else {
            Log.d("TaskManager", "Duplicate task ignored: $task")
        }
    }

    fun getTasks(): List<String> {
        val json = sharedPreferences.getString("tasks", null) ?: "[]"
        val type = object : TypeToken<List<String>>() {}.type
        val tasks: List<String> = gson.fromJson(json, type) ?: emptyList()
        Log.d("TaskManager", "Loaded tasks: $tasks") // ✅ Debug Log
        return tasks
    }
}
