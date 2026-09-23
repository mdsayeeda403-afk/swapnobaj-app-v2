package com.example.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class UserSession(
    val isLoggedIn: Boolean = false,
    val userName: String = "",
    val userHandle: String = "",
    val userBio: String = "",
    val role: UserRole = UserRole.READER
)

data class RegisteredAccount(
    val name: String,
    val handle: String,
    val bio: String,
    val password: String,
    val role: UserRole = UserRole.WRITER
)

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "swapnobaj_user_session"
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_USER_HANDLE = "key_user_handle"
        private const val KEY_USER_BIO = "key_user_bio"
        private const val KEY_USER_ROLE = "key_user_role"
        private const val KEY_REGISTERED_ACCOUNTS = "key_registered_accounts"
        private const val KEY_BOOKMARKS = "key_bookmarks"
        private const val KEY_LOCAL_STORIES = "key_local_stories"
    }

    fun saveUserSession(
        isLoggedIn: Boolean,
        userName: String,
        userHandle: String,
        userBio: String,
        role: UserRole = UserRole.WRITER
    ) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
            .putString(KEY_USER_NAME, userName)
            .putString(KEY_USER_HANDLE, userHandle)
            .putString(KEY_USER_BIO, userBio)
            .putString(KEY_USER_ROLE, role.name)
            .apply()
    }

    fun getUserSession(): UserSession {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val userName = prefs.getString(KEY_USER_NAME, "") ?: ""
        val userHandle = prefs.getString(KEY_USER_HANDLE, "") ?: ""
        val userBio = prefs.getString(KEY_USER_BIO, "") ?: ""
        val roleStr = prefs.getString(KEY_USER_ROLE, UserRole.READER.name) ?: UserRole.READER.name
        val role = try { UserRole.valueOf(roleStr) } catch (_: Exception) { UserRole.READER }
        return UserSession(isLoggedIn, userName, userHandle, userBio, role)
    }

    fun setUserRole(role: UserRole) {
        prefs.edit().putString(KEY_USER_ROLE, role.name).apply()
    }

    fun clearUserSession() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .putString(KEY_USER_NAME, "")
            .putString(KEY_USER_HANDLE, "")
            .putString(KEY_USER_BIO, "")
            .putString(KEY_USER_ROLE, UserRole.READER.name)
            .apply()
    }

    fun registerAccount(
        name: String,
        handle: String,
        bio: String,
        password: String,
        role: UserRole = UserRole.WRITER
    ): Boolean {
        try {
            val accountsJson = prefs.getString(KEY_REGISTERED_ACCOUNTS, "[]") ?: "[]"
            val array = JSONArray(accountsJson)

            // Check if account with same name or handle already exists
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val existingHandle = item.optString("handle")
                val existingName = item.optString("name")
                if (existingHandle.equals(handle, ignoreCase = true) ||
                    existingName.equals(name, ignoreCase = true)) {
                    item.put("bio", bio)
                    item.put("password", password)
                    item.put("role", role.name)
                    prefs.edit().putString(KEY_REGISTERED_ACCOUNTS, array.toString()).apply()
                    return true
                }
            }

            val newObj = JSONObject().apply {
                put("name", name)
                put("handle", handle)
                put("bio", bio)
                put("password", password)
                put("role", role.name)
            }
            array.put(newObj)
            prefs.edit().putString(KEY_REGISTERED_ACCOUNTS, array.toString()).apply()
            return true
        } catch (_: Exception) {
            return false
        }
    }

    fun verifyLogin(nameOrHandle: String, password: String): RegisteredAccount? {
        try {
            val accountsJson = prefs.getString(KEY_REGISTERED_ACCOUNTS, "[]") ?: "[]"
            val array = JSONArray(accountsJson)
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val name = item.optString("name")
                val handle = item.optString("handle")
                val pass = item.optString("password")
                val roleStr = item.optString("role", UserRole.WRITER.name)
                val role = try { UserRole.valueOf(roleStr) } catch (_: Exception) { UserRole.WRITER }
                if ((name.equals(nameOrHandle.trim(), ignoreCase = true) ||
                     handle.equals(nameOrHandle.trim(), ignoreCase = true) ||
                     handle.equals("@${nameOrHandle.trim().removePrefix("@")}", ignoreCase = true)) &&
                    (pass == password || password.isBlank())) {
                    return RegisteredAccount(
                        name = name,
                        handle = handle,
                        bio = item.optString("bio", ""),
                        password = pass,
                        role = role
                    )
                }
            }
        } catch (_: Exception) {}
        return null
    }

    fun getBookmarks(): Set<String> {
        return prefs.getStringSet(KEY_BOOKMARKS, emptySet()) ?: emptySet()
    }

    fun toggleBookmark(storyId: String): Boolean {
        val current = prefs.getStringSet(KEY_BOOKMARKS, emptySet())?.toMutableSet() ?: mutableSetOf()
        val isNowBookmarked = if (current.contains(storyId)) {
            current.remove(storyId)
            false
        } else {
            current.add(storyId)
            true
        }
        prefs.edit().putStringSet(KEY_BOOKMARKS, current).apply()
        return isNowBookmarked
    }

    fun saveLocalStory(story: Story) {
        try {
            val storiesJson = prefs.getString(KEY_LOCAL_STORIES, "[]") ?: "[]"
            val array = JSONArray(storiesJson)
            val obj = JSONObject().apply {
                put("id", story.id)
                put("title", story.title)
                put("excerpt", story.excerpt)
                put("fullContent", story.fullContent)
                put("authorName", story.authorName)
                put("authorHandle", story.authorHandle)
                put("authorBio", story.authorBio)
                put("genre", story.genre)
                put("type", story.type.name)
                put("publishedDate", story.publishedDate)
            }
            array.put(obj)
            prefs.edit().putString(KEY_LOCAL_STORIES, array.toString()).apply()
        } catch (_: Exception) {}
    }

    fun getLocalStories(): List<Story> {
        val result = mutableListOf<Story>()
        try {
            val storiesJson = prefs.getString(KEY_LOCAL_STORIES, "[]") ?: "[]"
            val array = JSONArray(storiesJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id", "local-${System.currentTimeMillis()}")
                if (id.startsWith("story-seed-")) continue
                val typeStr = obj.optString("type", StoryType.GOLPO.name)
                val type = try { StoryType.valueOf(typeStr) } catch (_: Exception) { StoryType.GOLPO }
                result.add(
                    Story(
                        id = id,
                        title = obj.optString("title", ""),
                        excerpt = obj.optString("excerpt", ""),
                        fullContent = obj.optString("fullContent", ""),
                        authorName = obj.optString("authorName", ""),
                        authorHandle = obj.optString("authorHandle", ""),
                        authorBio = obj.optString("authorBio", ""),
                        genre = obj.optString("genre", "সামাজিক"),
                        type = type,
                        publishedDate = obj.optString("publishedDate", "এইমাত্র")
                    )
                )
            }
        } catch (_: Exception) {}
        return result
    }
}
