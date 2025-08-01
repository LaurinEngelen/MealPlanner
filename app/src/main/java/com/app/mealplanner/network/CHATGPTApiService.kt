package com.app.mealplanner.network

import android.content.Context
import com.app.mealplanner.BuildConfig
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

class CHATGPTApiService(private val context: Context) {
    private val client = OkHttpClient()


    val JSON_FORMAT = "{\n" +
            "  \"name\": \"\",\n" +
            "  \"description\": \"\",\n" +
            "  \"ingredients\": [],\n" +
            "  \"preparations\": [],\n" +
            "  \"servings\": 0,\n" +
            "  \"prepTime\": \"\",\n" +
            "  \"notes\": \"\"\n" +
            "}"

    fun extractRecipeFromUrl(url: String): String? {
        val apiKey = BuildConfig.CHATGPT_API_KEY
        val endpoint = "https://api.openai.com/v1/chat/completions"
        val prompt = "Extrahiere das Rezept aus folgendem Link: $url. Gib nur das Rezept auf Deutsch als JSON in folgendem Format $JSON_FORMAT zurück."
        val json = JSONObject()
        json.put("model", "gpt-3.5-turbo")
        val messagesArray = org.json.JSONArray()
        val userMessage = JSONObject()
        userMessage.put("role", "user")
        userMessage.put("content", prompt)
        messagesArray.put(userMessage)
        json.put("messages", messagesArray)
        val body = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())
        val request = Request.Builder()
            .url(endpoint)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                android.util.Log.e("CHATGPTApiService", "API call failed: ${response.code} key ${apiKey}")
                return null
            }
            val responseBody = response.body?.string() ?: return null
            android.util.Log.d("CHATGPTApiService", "API response: $responseBody")
            val responseJson = JSONObject(responseBody)
            val result = responseJson.optJSONArray("choices")?.optJSONObject(0)?.optJSONObject("message")?.optString("content")
            if (result == null) {
                android.util.Log.e("CHATGPTApiService", "No recipe text found in response JSON")
            }
            return result
        }
    }
}
