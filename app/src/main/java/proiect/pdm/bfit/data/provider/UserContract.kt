package proiect.pdm.bfit.data.provider

import android.net.Uri
import android.provider.BaseColumns

object UserContract {
    const val AUTHORITY = "proiect.pdm.bfit.provider"
    val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/users")

    object Columns : BaseColumns {
        const val ID = "_id"
        const val USERNAME = "username"
        const val PASSWORD = "password"
        const val HEIGHT_CM = "heightCm"
        const val WEIGHT_KG = "weightKg"
        const val AGE = "age"
        const val ACTIVITY_LEVEL = "activityLevel"
        const val GOAL = "goal"
        const val PFP_LOCATION = "pfpLocation"
    }
} 