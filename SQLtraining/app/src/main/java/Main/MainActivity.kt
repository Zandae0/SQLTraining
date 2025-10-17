package Main

import Utils.SessionManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.sqltraining.R

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ambil NavController dari NavHostFragment
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // 🔹 Cek apakah user sudah login
        val sessionManager = SessionManager(this)

        if (sessionManager.isLoggedIn()) {
            // Kalau sudah login, langsung arahkan ke Dashboard
            navController.navigate(
                R.id.dashboardFragment,
                null,
                NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true) // hapus backstack biar ga balik ke login
                    .build()
            )
        } else {
            // Kalau belum login, arahkan ke Login Fragment
            navController.navigate(
                R.id.loginFragment,
                null,
                NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }
}
