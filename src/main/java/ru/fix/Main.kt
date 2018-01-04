package ru.fix

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

open class Day (val day: LocalDate, var name: String?, var name2: String?);
class WorkDay(day: LocalDate) : Day(day, null, null);
class DayOffAM(day: LocalDate) : Day(day, null, null);
class DayOffPM(day: LocalDate) : Day(day, null, null);

data class Config(val persons: List<Person>, val month: Int, val year: Int, val selebrations : List<Int>?, val spreadsheetId : String)

data class Person(val name: String, val cannot : List<Int>?, val favours : List<Int>?)

private fun convert(jsonFile: File) : Config {
    val mapper = jacksonObjectMapper()
    mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)

    return mapper.readValue<Config>(jsonFile)
}

fun main(args: Array<String>) {
    val jsonFile: File = File(args[0])
    val config = convert(jsonFile)
    println(config)
    var valueMin = 1000.0
    var workDaysZipMin = ArrayList<Day>()

    for (index in 1..10000) {
        //получаем коллекцию рабочих дней и выходных
        val workDays = buildWorkDays(config)

        //проверяем соотношения
        //считаем балл
        var valueWorkDay = calcValue(workDays)
//        println("valueWorkDay: ${valueWorkDay}")
        if (valueWorkDay < valueMin) {
            valueMin = valueWorkDay
            workDaysZipMin = zipWorkDays(workDays)
        }
    }

    println("valueMin: ${valueMin}")
//    for (day in workDaysZip) {
//        println("${day.day} - ${day.name}, ${day.name2}")
//    }

    val calendar = CreateCalendar()
    calendar.build(workDaysZipMin, config.spreadsheetId)

}

/*
сжимаем
 */
private fun zipWorkDays(workDays: ArrayList<Day>): ArrayList<Day> {
    val workDaysZip = ArrayList<Day>()
    var dayPrev: Day? = null
    for (day in workDays) {
        if (dayPrev != null && dayPrev.day.equals(day.day)) {
            dayPrev.name2 = day.name
        } else {
            workDaysZip.add(day)
            dayPrev = day
        }
    }
    return workDaysZip
}

/*
получаем коллекцию рабочих дней и выходных
 */
private fun buildWorkDays(config: Config): ArrayList<Day> {
    val workDays = ArrayList<Day>()
    var firstDay = LocalDate.of(config.year, config.month, 1);
    do {
        if (firstDay.dayOfWeek in listOf<DayOfWeek>(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
            workDays.add(DayOffAM(firstDay))
            workDays.add(DayOffPM(firstDay))
        } else if (config.selebrations != null && firstDay.dayOfMonth in config.selebrations) {
            workDays.add(DayOffAM(firstDay))
            workDays.add(DayOffPM(firstDay))
        } else {
            workDays.add(WorkDay(firstDay))
        }
        firstDay = firstDay.plusDays(1);

    } while (firstDay.dayOfMonth != 1)

    val peoples = ArrayList<String>()
    for ((index, day) in workDays.withIndex()) {
        peoples.add(config.persons.get(index % config.persons.size).name)
    }

    shuffle(peoples);

    for ((index, day) in workDays.withIndex()) {
        day.name = peoples.get(index)
    }
    return workDays
}

/*
рассчитываем бал по результату случайного набора
 */
private fun calcValue(workDays: ArrayList<Day>): Double {
    var valueWorkDay = 0.0
    val workDaysCounter = HashMap<String, Int>()
    for (day in workDays) {
        if (day is WorkDay) {
            workDaysCounter.put(day.name ?: "", 1 + workDaysCounter.getOrDefault(day.name, 0))
        }
    }
    val weekEndDaysCounter = HashMap<String, Int>()
    for (day in workDays) {
        if (day is DayOffPM || day is DayOffAM) {
            weekEndDaysCounter.put(day.name ?: "", 1 + weekEndDaysCounter.getOrDefault(day.name, 0))
        }
    }
    //распечатка результата
    var averageWorkDays = workDaysCounter.values.stream().collect(Collectors.averagingInt { value -> value })
    for (key in workDaysCounter.entries) {
        valueWorkDay += Math.abs(averageWorkDays - key.value)*5
    }
    var averageweekEndDays = weekEndDaysCounter.values.stream().collect(Collectors.averagingInt { value -> value })
    for (key in weekEndDaysCounter.entries) {
        valueWorkDay += Math.abs(averageweekEndDays - key.value)*5
    }
    //поиск повторений
    var name = " ";
    var name2 = " ";
    for (day in workDays) {
        if (name.equals(day.name ?: "")
                || name.equals(day.name2 ?: "")
                || name2.equals(day.name ?: "")
                || name2.equals(day.name2 ?: "")) {
            valueWorkDay += 1
        }

        name = day.name ?: " "
        name2 = day.name2 ?: " "
    }

    return valueWorkDay
}

fun shuffle(list : ArrayList<String>) : ArrayList<String> {
    Collections.shuffle(list)
    //check dublicates
    var prev = "";
    for ((index,man2) in list.withIndex()) {
        var man = list[index]
        if (man.equals(prev) && index < list.size - 1) {
            //текущий равен предыдущему, меняем со следующим
            var nextMan = index + 1
            while (list[index].equals(list[nextMan]) && nextMan < list.size-1) {
                nextMan++
            }
            //меняем
            list[index] = list[nextMan]
            list[nextMan] = man
            prev = list[index]
        } else {
            prev = man
        }
    }
    return list;
}

