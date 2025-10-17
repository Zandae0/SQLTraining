package Utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val PREF_NAME = "user_session"
    private val KEY_IS_LOGGED_IN = "is_logged_in"
    private val KEY_USERNAME = "username"

    private val pref: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = pref.edit()

    fun saveLoginSession(username: String) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_USERNAME, username)
        editor.apply()
    }

    fun getUsername(): String? {
        return pref.getString(KEY_USERNAME, null)
    }

    fun isLoggedIn(): Boolean {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun logout() {
        editor.clear()
        editor.apply()
    }
}