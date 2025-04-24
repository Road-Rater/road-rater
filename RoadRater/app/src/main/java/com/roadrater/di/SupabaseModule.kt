package com.roadrater.di

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json
import io.github.jan.supabase.auth.AuthConfig
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.realtime.Realtime
import org.koin.dsl.module

val SupabaseModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = "https://syvccmvneltcyoyuoryn.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InN5dmNjbXZuZWx0Y3lveXVvcnluIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDUzOTkwNjksImV4cCI6MjA2MDk3NTA2OX0.bpTsUN95QEqvmuwSNNSRvTtKwn7__qNJrNEfqXpGpmU"
        ) {
            defaultSerializer = KotlinXSerializer(Json { ignoreUnknownKeys = true })
            defaultLogLevel = LogLevel.DEBUG
            install(Postgrest)
//            install(Auth) {
//                platformGoTrueConfig()
//                flowType = FlowType.PKCE
//            }
            install(Realtime)
        }
    }
}