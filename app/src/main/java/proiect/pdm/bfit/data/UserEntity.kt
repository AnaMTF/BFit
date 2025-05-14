package proiect.pdm.bfit.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserEntity (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String, //should be unique
    val password: String,
    val heightCm: Int,
    val weightKg: Int,
    val age: Int,
    val activityLevel: String,
    val goal: String,
    val pfpLocation: String
)

