package tasklist

import kotlinx.datetime.*
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

fun main() {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    val type = Types.newParameterizedType(MutableList::class.java, Task::class.java)
    val taskListAdapter = moshi.adapter<MutableList<Task>>(type)
    val jsonFile = File("tasklist.json")
    val taskList = TaskList()
    if (jsonFile.exists()) taskList.tasks = taskListAdapter.fromJson(jsonFile.readText())!!
    while (true) {
        println("Input an action (add, print, edit, delete, end):")
        when (readln()) {
            "add" -> taskList.add()
            "print" -> taskList.print()
            "edit" -> taskList.edit()
            "delete" -> taskList.delete()
            "end" ->{
                jsonFile.writeText(taskListAdapter.toJson(taskList.tasks))
                println("Tasklist exiting!")
                return
            }
            else -> println("The input action is invalid")
        }
    }
}

data class Task(var priority: String, var date: String, var time: String, var task: MutableList<String>)

class TaskList {
    var tasks: MutableList<Task> = mutableListOf()

    private fun getPriority(): String {
        while (true) {
            println("Input the task priority (C, H, N, L):")
            when (val priority = readln().trim().lowercase()) {
                "c" -> return priority
                "h" -> return priority
                "n" -> return priority
                "l" -> return priority
            }
        }
    }

    private fun stringToDate(date: String): LocalDate {
        val dateParts = date.split("-")
        val year = dateParts[0].toInt()
        val month = dateParts[1].toInt()
        val day = dateParts[2].toInt()
        return LocalDate(year, month, day)
    }

    private fun getDate(): String {
        while (true) {
            println("Input the date (yyyy-mm-dd):")
            val date = readln().trim()
            try {
                stringToDate(date)
                val dateParts = date.split("-")
                val year = dateParts[0]
                val formatMonth = dateParts[1].padStart(2, '0')
                val formatDay = dateParts[2].padStart(2, '0')
                return String.format("$year-$formatMonth-$formatDay")
            } catch (e: Exception ) {
                println("The input date is invalid")
            }
        }
    }

    private fun getTime(): String {
        while (true) {
            println("Input the time (hh:mm):")
            val time = readln().trim()
            try {
                val timeParts = time.split(":")
                val hour = timeParts[0].toInt()
                val minute = timeParts[1].toInt()
                LocalDateTime(1, 1, 1, hour, minute)
                val formatHour = timeParts[0].padStart(2, '0')
                val formatMinute = timeParts[1].padStart(2, '0')
                return String.format("$formatHour:$formatMinute")
            } catch (e: Exception) {
                println("The input time is invalid")
            }
        }
    }

    private fun getTask(): MutableList<String> {
        println("Input a new task (enter a blank line to end):")
        val currTask = mutableListOf<String>()
        while (true) {
            val task = readln().trim()
            if (task == "") break
            currTask.add(task)
        }
        return currTask
    }

    fun add() {
        val priority = getPriority()
        val date = getDate()
        val time = getTime()
        val currTask = getTask()
        if (currTask.size == 0) {
            println("The task is blank")
            return
        }
        tasks.add(Task(priority, date, time, currTask))
    }

    private fun tasksIsEmpty(): Boolean {
        if (tasks.size == 0) {
            println("No tasks have been input")
            return true
        }
        return false
    }

    private fun getIndex(): Int {
        if (tasksIsEmpty()) return -1
        while (true) {
            println("Input the task number (1-${tasks.size}):")
            try {
                val index = readln().trim().toInt()
                if (index in 1..tasks.size) return index - 1
                else throw Exception()
            } catch (e: Exception) {
                println("Invalid task number")
            }
        }
    }

    fun edit() {
        if (tasksIsEmpty()) return
        print()
        val index = getIndex()
        val task = tasks[index]
        while (true) {
            println("Input a field to edit (priority, date, time, task):")
            when (readln().trim()) {
                "priority" -> {
                    task.priority = getPriority()
                    break
                }
                "date" -> {
                    task.date = getDate()
                    break
                }
                "time" -> {
                    task.time = getTime()
                    break
                }
                "task" -> {
                    task.task = getTask()
                    break
                }
                else -> println("Invalid field")
            }
        }
        println("The task is changed")
    }

    fun delete() {
        if (tasksIsEmpty()) return
        print()
        val index = getIndex()
        tasks.removeAt(index)
        println("The task is deleted")
    }

    private fun getDueTag(date: String): String {
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date
        val otherDate = stringToDate(date)
        val diff = currentDate.daysUntil(otherDate)
        return when {
            diff > 0 -> "I"
            diff < 0 -> "O"
            else -> "T"
        }
    }

    private fun printPriorityColor(priority: String): String {
        return when (priority) {
            "c" -> "\u001B[101m \u001B[0m"
            "h" -> "\u001B[103m \u001B[0m"
            "n" -> "\u001B[102m \u001B[0m"
            "l" -> "\u001B[104m \u001B[0m"
            else -> " "
        }
    }

    private fun printDueColor(dueTag: String): String {
        return when (dueTag) {
            "I" -> "\u001B[102m \u001B[0m"
            "T" -> "\u001B[103m \u001B[0m"
            "O" -> "\u001B[101m \u001B[0m"
            else -> " "
        }
    }

    private fun printTaskLine(task: String) {
        val taskLines = task.chunked(44)
        for (line in taskLines) {
            println("|    |            |       |   |   |${line.padEnd(44, ' ')}|")
        }
    }

    fun print() {
        if (tasksIsEmpty()) return
        val header = "+----+------------+-------+---+---+--------------------------------------------+\n" +
                "| N  |    Date    | Time  | P | D |                   Task                     |\n" +
                "+----+------------+-------+---+---+--------------------------------------------+"
        val separator = "+----+------------+-------+---+---+--------------------------------------------+"
        println(header)
        for (i in 0 until tasks.size) {
            val currTask = tasks[i]
            val taskLines = currTask.task.map { it.chunked(44) }
            println("| ${(i + 1).toString().padEnd(2, ' ')} | ${currTask.date} | ${currTask.time} " +
                    "| ${printPriorityColor(currTask.priority)} | ${printDueColor(getDueTag(currTask.date))} " +
                    "|${taskLines[0][0].padEnd(44, ' ')}|")
            for (j in 1 until taskLines[0].size) printTaskLine(taskLines[0][j])
            for (j in 1 until currTask.task.size) printTaskLine(currTask.task[j])
            println(separator)
        }
    }
}
