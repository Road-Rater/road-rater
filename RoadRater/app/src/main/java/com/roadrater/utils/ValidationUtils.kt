package com.roadrater.utils
object ValidationUtils {
    fun isValidNumberPlate(plate: String): Boolean {
        // Check if plate is between 1-6 characters and alphanumeric
        return plate.length in 1..6 && plate.all { it.isLetterOrDigit() }
    }

    fun formatNumberPlate(plate: String): String {
        // Convert to uppercase and remove any spaces
        return plate.uppercase().replace(" ", "")
    }
}
