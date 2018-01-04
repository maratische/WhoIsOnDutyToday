package ru.fix

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.Sheet
import com.google.api.services.sheets.v4.model.SheetProperties
import com.google.api.services.sheets.v4.model.ValueRange
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList


/** Application name.  */
private val APPLICATION_NAME = "Google Sheets API Java Quickstart"

/** Directory to store user credentials for this application.  */
private val DATA_STORE_DIR = java.io.File(
        System.getProperty("user.home"), ".credentials/sheets.googleapis.com-java-quickstart")

/** Global instance of the [FileDataStoreFactory].  */
private var DATA_STORE_FACTORY: FileDataStoreFactory? = null

/** Global instance of the JSON factory.  */
private val JSON_FACTORY = JacksonFactory.getDefaultInstance()

/** Global instance of the HTTP transport.  */
private var HTTP_TRANSPORT: HttpTransport? = null

/** Global instance of the scopes required by this quickstart.
 *
 * If modifying these scopes, delete your previously saved credentials
 * at ~/.credentials/sheets.googleapis.com-java-quickstart
 */
private val SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS, SheetsScopes.SPREADSHEETS_READONLY)

fun main(args: Array<String>) {

    val workDays = ArrayList<Day>()
    val day = WorkDay(LocalDate.now())
    day.name="marat"
    workDays.add(day)
    val calendar = CreateCalendar()
    calendar.build(workDays, "1aA5EyPKcUebS5FeEzqcLaQd6sZehS0PpfuBaXNdULp4")
}

class CreateCalendar {

    fun init() {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
            DATA_STORE_FACTORY = FileDataStoreFactory(DATA_STORE_DIR)
        } catch (t: Throwable) {
            t.printStackTrace()
            System.exit(1)
        }

    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun authorize(): Credential {
        // Load client secrets.
        //        InputStream in =
        //                Quickstart.class.getResourceAsStream("/client_secret.json");
        val `in` = FileInputStream(System.getProperty("user.home") + "/.credentials/sheets.googleapis.com-java-quickstart/client_secret.json")
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(`in`))

        // Build flow and trigger user authorization request.
        val flow = GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY!!)
                .setAccessType("offline")
                .build()
        val credential = AuthorizationCodeInstalledApp(
                flow, LocalServerReceiver()).authorize("user")
        println(
                "Credentials saved to " + DATA_STORE_DIR.absolutePath)
        return credential
    }

    /**
     * Build and return an authorized Sheets API client service.
     * @return an authorized Sheets API client service
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getSheetsService(): Sheets {
        val credential = authorize()
        return Sheets.Builder(HTTP_TRANSPORT!!, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build()
    }


    fun build(workDays : ArrayList<Day>, spreadsheetId: String) {
        init()
        val sheetName = "01.2018"

        if (workDays.size == 0) {
            return;
        }
        //добиваем данные до полного месяца
        while (workDays.get(0).day.dayOfWeek != DayOfWeek.MONDAY) {
            val day = WorkDay(workDays.get(0).day.plusDays(-1))
            workDays.add(0, day)
        }
        while (workDays.last().day.dayOfWeek != DayOfWeek.SUNDAY) {
            val day = WorkDay(workDays.last().day.plusDays(1))
            workDays.add(day)
        }

        // Build a new authorized API client service.
        val service = getSheetsService()

        // Prints the names and majors of students in a sample spreadsheet:
        // https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
        //проверяем есть ли нужная страница
        val spreadsheetMetadata = service.spreadsheets().get(spreadsheetId).execute()
        val sheets = spreadsheetMetadata.get("sheets") as ArrayList<Sheet>
        var needCreateSheet = true;
        for (sheet in sheets) {
            val properties = sheet.get("properties") as SheetProperties
            val title = properties.get("title") as String
            if (title === sheetName) {
                needCreateSheet = false;
                break
            }
            System.out.printf("properties $properties")
        }
        if (needCreateSheet) {
            //TODO add new Sheet for every month
        }


        //update fields
        val values /*: List<List<Any>>*/ = ArrayList<ArrayList<Any>>()

        var line1 = ArrayList<Any>()
        var line2 = ArrayList<Any>()
        var line3 = ArrayList<Any>()
        var line4 = ArrayList<Any>()
        for (day in workDays) {
            if (day.day.dayOfWeek == DayOfWeek.MONDAY
                    && line1.size > 0) {
                values.add(line1)
                values.add(line2)
                values.add(line3)
                values.add(line4)

                line1 = ArrayList<Any>()
                line2 = ArrayList<Any>()
                line3 = ArrayList<Any>()
                line4 = ArrayList<Any>()
            }

            line1.add("" + day.day.dayOfWeek)
            line1.add("")
            line2.add("" + day.day.dayOfMonth)
            line2.add("")
            line3.add("")
            if (day.name != null) {
                line3.add("" + day.name)
            } else {
                line3.add("")
            }
            line4.add("")
            if (day.name2 != null) {
                line4.add("" + day.name2)
            } else {
                line4.add("")
            }
        }
        if (line1.size > 0) {
            values.add(line1)
            values.add(line2)
            values.add(line3)
            values.add(line4)
        }
        val range = "Sheet1!A1:N"
        val valueInputOption = "USER_ENTERED"
        val body = ValueRange()
                .setValues(values as List<List<Any>>)
        val result = service.spreadsheets().values().update(spreadsheetId, range, body)
                .setValueInputOption(valueInputOption)
                .execute()
        System.out.printf("%d cells updated.", result.updatedCells)
        System.out.printf("sheets $sheets")
//    val values = response.getValues()
//    if (values == null || values.size == 0) {
//        println("No data found.")
//    } else {
//        println("Name, Major")
//        for (row in values) {
//            // Print columns A and E, which correspond to indices 0 and 4.
//            System.out.printf("%s, %s\n", row[0], row[4])
//        }
//    }
    }

}