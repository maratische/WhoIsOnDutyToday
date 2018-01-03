package ru.fix

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

open class Day (val day: LocalDate, var name: String?, var name2: String?);
class WorkDay(day: LocalDate) : Day(day, null, null);
class DayOffAM(day: LocalDate) : Day(day, null, null);
class DayOffPM(day: LocalDate) : Day(day, null, null);

data class Config(val persons: List<Person>, val month: Int, val year: Int, val selebrations : List<Int>?)

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
    //получаем коллекцию рабочих дней и выходных
    val workDays = ArrayList<Day>()
    var firstDay = LocalDate.of(config.year, config.month,1);
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
    for ((index,day) in workDays.withIndex()) {
        peoples.add(config.persons.get(index % config.persons.size).name)
    }

    shuffle(peoples);

    for ((index,day) in workDays.withIndex()) {
        day.name = peoples.get(index)
    }

    //проверяем соотношения
    //TODO

    //сжимаем
    val workDaysZip = ArrayList<Day>()
    var dayPrev : Day? = null
    for (day in workDays) {
        if (dayPrev != null && dayPrev.day.equals(day.day)) {
            dayPrev.name2 = day.name
        } else {
            workDaysZip.add(day)
            dayPrev = day
        }
    }


    for (day in workDaysZip) {
        println("${day.day} - ${day.name}, ${day.name2}")
    }

    val calendar = CreateCalendar()
    calendar.build(workDaysZip)

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
            while (list[index].equals(list[nextMan]) && nextMan < list.size) {
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

