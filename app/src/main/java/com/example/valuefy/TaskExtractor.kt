package com.example.valuefy

class TaskExtractor {
    private val taskKeywords = listOf("need to", "must", "should", "action item", "remember to", "follow up", "assign", "schedule", "plan", "deadline")

    fun extractTasks(transcribedText: String): List<String> {
        val sentences = transcribedText.split(". ")
        val tasks = mutableListOf<String>()

        for (sentence in sentences) {
            for (keyword in taskKeywords) {
                if (sentence.contains(keyword, ignoreCase = true)) {
                    tasks.add(sentence.trim())
                    break
                }
            }
        }

        return tasks
    }
}
