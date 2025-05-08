package com.roadrater.database.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable

@Serializable
data class Car(
    val plate: String,
    val make: String,
    val model: String,
)

class CarRepository(private val supabaseClient: SupabaseClient) {

    suspend fun getCarByPlate(plate: String, supabaseClient: SupabaseClient): Car? {
        return supabaseClient
            .from("cars")
            .select {
                filter {
                    eq("plate", plate)
                }
                limit(1)
            }
            .decodeSingleOrNull()
        // } catch (e: Exception) {
        //   println("Error fetching car: ${e.localizedMessage}")
        // null
        // }
    }
}
