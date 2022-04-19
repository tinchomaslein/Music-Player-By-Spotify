package ayds.newyork.songinfo.moredetails.fulllogic

import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import ayds.newyork.songinfo.moredetails.fulllogic.DataBase
import android.content.ContentValues
import android.content.Context
import android.util.Log
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import java.util.ArrayList

class DataBase(context: Context?) : SQLiteOpenHelper(context, "dictionary.db", null, 1) {

    override fun onCreate(dataBase: SQLiteDatabase) {
        val createQuery : String =  "create table artists (id INTEGER PRIMARY KEY AUTOINCREMENT, artist string, info string, source integer)"
        dataBase.execSQL(createQuery)
        Log.i("DB", "DB created")
    }

    override fun onUpgrade(dataBase: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    companion object {
        private const val artistColumn : String = "artist"
        private const val infoColumn : String = "info"
        private const val sourceColumn : String = "source"
        private const val idColumn : String = "id"
        private const val tableArtists : String = "artists"

        fun testDB() {
            var dataBaseConnection: Connection? = null
            val dataBaseUrl : String = "jdbc:sqlite:./dictionary.db"
            val sqlQuery : String = "select * from $tableArtists"
            try {
                dataBaseConnection = createConnectionDataBase(dataBaseUrl)
                val statement = createStatementDataBase(dataBaseConnection)
                val rs = statement.executeQuery(sqlQuery)
                while (rs.next()) {
                    println("$idColumn = " + rs.getInt(idColumn))
                    println("$artistColumn = " + rs.getString(artistColumn))
                    println("$infoColumn = " + rs.getString(infoColumn))
                    println("$sourceColumn = " + rs.getString(sourceColumn))
                }
            } catch (exception : SQLException) {
                System.err.println(exception.message)
            } finally {
                closeConnectionDataBase(dataBaseConnection)
            }
        }

        private fun createConnectionDataBase(dataBaseUrl : String) : Connection{
            var dataBaseConnection: Connection? = null
            dataBaseConnection = DriverManager.getConnection(dataBaseUrl)
            return dataBaseConnection
        }

        private fun createStatementDataBase(dataBaseConnection : Connection) : Statement{
            val statementTimeOut : Int = 30
            val statement = dataBaseConnection.createStatement()
            statement.queryTimeout = statementTimeOut
            return statement
        }

        private fun closeConnectionDataBase(dataBaseConnection : Connection?) {
            try {
                dataBaseConnection?.close()
            } catch (closeFailException : SQLException) {
                System.err.println(closeFailException)
            }


        fun saveArtist(dbHelper: DataBase, artist: String?, info: String?) {
            // Gets the data repository in write mode

            val db = dbHelper.writableDatabase

            // Create a new map of values, where column names are the keys
            val values = ContentValues()
            values.put(artistColumn, artist)
            values.put(infoColumn, info)
            values.put(sourceColumn, 1)

            // Insert the new row, returning the primary key value of the new row
            val newRowId = db.insert("$tableArtists", null, values)
        }

        fun getInfo(dbHelper: DataBase, artist: String): String? {
            val db = dbHelper.readableDatabase

            // Define a projection that specifies which columns from the database
            // you will actually use after this query.
            val projection = arrayOf(
                idColumn,
                artistColumn,
                infoColumn
            )

            // Filter results WHERE "title" = 'My Title'
            val selection = "$artistColumn = ?"
            val selectionArgs = arrayOf(artist)

            // How you want the results sorted in the resulting Cursor
            val sortOrder = "$artistColumn DESC"
            val cursor = db.query(
                "$tableArtists",  // The table to query
                projection,  // The array of columns to return (pass null to get all)
                selection,  // The columns for the WHERE clause
                selectionArgs,  // The values for the WHERE clause
                null,  // don't group the rows
                null,  // don't filter by row groups
                sortOrder // The sort order
            )
            val items: MutableList<String> = ArrayList()
            while (cursor.moveToNext()) {
                val info = cursor.getString(
                    cursor.getColumnIndexOrThrow(infoColumn)
                )
                items.add(info)
            }
            cursor.close()
            return if (items.isEmpty()) null else items[0]
        }
    }
}