package proiect.pdm.bfit.data.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import androidx.room.Room
import kotlinx.coroutines.runBlocking
import proiect.pdm.bfit.data.BFitDatabase
import proiect.pdm.bfit.data.UserEntity

class UserContentProvider : ContentProvider() {
    companion object {
        const val USERS = 1
        const val USER_ID = 2
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(UserContract.AUTHORITY, "users", USERS)
            addURI(UserContract.AUTHORITY, "users/#", USER_ID)
        }
    }

    private lateinit var database: BFitDatabase

    override fun onCreate(): Boolean {
        context?.let {
            database = Room.databaseBuilder(
                it.applicationContext,
                BFitDatabase::class.java,
                "bfit_database"
            ).build()
        }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return when (uriMatcher.match(uri)) {
            USERS -> database.userDao().getAllCursor()
            USER_ID -> {
                val id = uri.lastPathSegment?.toLongOrNull()
                id?.let { database.userDao().getUserByIdCursor(it) }
            }
            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return when (uriMatcher.match(uri)) {
            USERS -> {
                val user = UserEntity(
                    id = 0,
                    username = values?.getAsString(UserContract.Columns.USERNAME) ?: "",
                    password = values?.getAsString(UserContract.Columns.PASSWORD) ?: "",
                    heightCm = values?.getAsInteger(UserContract.Columns.HEIGHT_CM) ?: 0,
                    weightKg = values?.getAsInteger(UserContract.Columns.WEIGHT_KG) ?: 0,
                    age = values?.getAsInteger(UserContract.Columns.AGE) ?: 0,
                    activityLevel = values?.getAsString(UserContract.Columns.ACTIVITY_LEVEL) ?: "",
                    goal = values?.getAsString(UserContract.Columns.GOAL) ?: "",
                    pfpLocation = values?.getAsString(UserContract.Columns.PFP_LOCATION) ?: ""
                )
                val id = runBlocking { database.userDao().insert(user) }
                Uri.withAppendedPath(UserContract.CONTENT_URI, id.toString())
            }
            else -> null
        }
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return when (uriMatcher.match(uri)) {
            USER_ID -> {
                val id = uri.lastPathSegment?.toLongOrNull()
                if (id != null && values != null) {
                    val user = UserEntity(
                        id = id,
                        username = values.getAsString(UserContract.Columns.USERNAME) ?: "",
                        password = values.getAsString(UserContract.Columns.PASSWORD) ?: "",
                        heightCm = values.getAsInteger(UserContract.Columns.HEIGHT_CM) ?: 0,
                        weightKg = values.getAsInteger(UserContract.Columns.WEIGHT_KG) ?: 0,
                        age = values.getAsInteger(UserContract.Columns.AGE) ?: 0,
                        activityLevel = values.getAsString(UserContract.Columns.ACTIVITY_LEVEL) ?: "",
                        goal = values.getAsString(UserContract.Columns.GOAL) ?: "",
                        pfpLocation = values.getAsString(UserContract.Columns.PFP_LOCATION) ?: ""
                    )
                    runBlocking { database.userDao().update(user) }
                    1
                } else 0
            }
            else -> 0
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return when (uriMatcher.match(uri)) {
            USER_ID -> {
                val id = uri.lastPathSegment?.toLongOrNull()
                id?.let { database.userDao().deleteUserById(it) } ?: 0
            }
            else -> 0
        }
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            USERS -> "vnd.android.cursor.dir/vnd.${UserContract.AUTHORITY}.users"
            USER_ID -> "vnd.android.cursor.item/vnd.${UserContract.AUTHORITY}.users"
            else -> null
        }
    }
} 