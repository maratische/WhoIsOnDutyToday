package ru.fix

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.*
import org.jetbrains.annotations.Nullable
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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
    val workDays = ArrayList<LocalDate>()
    val dayOffDays = ArrayList<LocalDate>()
    var firstDay = LocalDate.of(config.year, config.month,1);
    do {
        if (firstDay.dayOfWeek in listOf<DayOfWeek>(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
            dayOffDays.add(firstDay)
        } else if (config.selebrations != null && firstDay.dayOfMonth in config.selebrations) {
            dayOffDays.add(firstDay)
        } else {
            workDays.add(firstDay)
        }
        firstDay = firstDay.plusDays(1);

    } while (firstDay.dayOfMonth != 1)

    var schedule = shufflePeople(workDays, config)
    var scheduleDay1 = shufflePeople(dayOffDays, config)
    var scheduleDay2 = shufflePeople(dayOffDays, config)
    for (day in scheduleDay2.keys.sorted()) {
        schedule.put(day, scheduleDay1.get(day)+"/"+scheduleDay2.get(day))
    }

    for (day in schedule.keys.sorted()) {
        println("${day} - ${schedule.get(day)}")
    }

}

private fun shufflePeople(workDays: ArrayList<LocalDate>, config: Config): HashMap<LocalDate, String> {
    Collections.shuffle(workDays);
    var schedule = HashMap<LocalDate, String>()
    var index = 0;
    for (day in workDays) {
        val name = config.persons.get(index++ % config.persons.size).name
        schedule.put(day, name)
    }
    return schedule
}