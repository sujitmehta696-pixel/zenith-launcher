package com.example.data.cloud

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

data class AiSearchResult(
    val answerSummary: String,
    val suggestedAppKeywords: List<String>,
    val suggestedWallpaperCategories: List<String>,
    val suggestedSystemSetting: String?
)

class AiSearchRepository {

    private val schema = Schema.obj(
        properties = mapOf(
            "answerSummary" to Schema.string(description = "Concise 1-2 sentence helpful launcher answer for the user's query"),
            "suggestedAppKeywords" to Schema.array(
                items = Schema.string(),
                description = "Up to 4 short app name keywords likely relevant to the query"
            ),
            "suggestedWallpaperCategories" to Schema.array(
                items = Schema.string(),
                description = "Up to 2 matching wallpaper categories (e.g. AMOLED, Space, Nature, 3D, Live Wallpapers)"
            ),
            "suggestedSystemSetting" to Schema.string(
                description = "Optional Android setting name if the user wants a setting (e.g. Wi-Fi, Bluetooth, Display)",
                nullable = true
            )
        ),
        optionalProperties = listOf("suggestedSystemSetting")
    )

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    suspend fun searchWithAi(
        query: String,
        installedAppLabels: List<String>
    ): Result<AiSearchResult> {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            return Result.failure(IllegalArgumentException("Enter a query for AI Search."))
        }

        val appsContext = installedAppLabels.take(40).joinToString(", ")
        val prompt = """
            You are the smart AI assistant inside Android 3D Rolling Launcher OS.
            User query: "$cleanQuery"
            Some installed apps on this phone: [$appsContext]
            Provide a concise helpful answer, suggest matching app keywords from the phone if relevant,
            matching wallpaper categories if visual/theme related, or a matching Android system setting.
        """.trimIndent()

        return try {
            val response = withTimeout(60_000L) {
                model.generateContent(prompt)
            }
            val rawJson = response.text ?: throw IllegalStateException("AI returned an empty response.")
            val json = JSONObject(rawJson)
            val appKw = mutableListOf<String>()
            json.optJSONArray("suggestedAppKeywords")?.let { arr ->
                for (i in 0 until arr.length()) {
                    arr.optString(i).takeIf { it.isNotBlank() }?.let(appKw::add)
                }
            }
            val wpCats = mutableListOf<String>()
            json.optJSONArray("suggestedWallpaperCategories")?.let { arr ->
                for (i in 0 until arr.length()) {
                    arr.optString(i).takeIf { it.isNotBlank() }?.let(wpCats::add)
                }
            }
            val setting = json.optString("suggestedSystemSetting").takeIf {
                it.isNotBlank() && it != "null"
            }

            Result.success(
                AiSearchResult(
                    answerSummary = json.optString("answerSummary").ifBlank { "No summary available." },
                    suggestedAppKeywords = appKw,
                    suggestedWallpaperCategories = wpCats,
                    suggestedSystemSetting = setting
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI Search timed out", e)
            Result.failure(IllegalStateException("AI search timed out after 60 seconds. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI Search failed", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "AiSearchRepo"
    }
}
