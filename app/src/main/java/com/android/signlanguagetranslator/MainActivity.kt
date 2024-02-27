package com.android.signlanguagetranslator

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.android.signlanguagetranslator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        Log.d("LOG", "navcotroller has been setup")
        /*activityMainBinding.navigation.setOnNavigationItemReselectedListener {
            // ignore the reselection
        }*/
        // Instantiate ModelDownloader
        /*val modelDownloader = ModelDownloader(this)

        // Call downloadModelIfNeeded to start downloading the model if needed
        modelDownloader.downloadModelIfNeeded()*/
    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount > 0) {
            // If there are fragments in the back stack, pop it
            fragmentManager.popBackStack()
        } else {
            // If no fragments in the back stack, finish the activity
            super.onBackPressed()
        }
    }

}