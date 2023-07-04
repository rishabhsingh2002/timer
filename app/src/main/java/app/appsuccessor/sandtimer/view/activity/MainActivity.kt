package app.appsuccessor.sandtimer.view.activity

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import app.appsuccessor.sandtimer.R
import app.appsuccessor.sandtimer.databinding.ActivityMainBinding
import app.appsuccessor.sandtimer.datasource.remote.CityApiClient
import app.appsuccessor.sandtimer.view.fragment.AlarmFragment
import app.appsuccessor.sandtimer.view.fragment.BedTimeFragment
import app.appsuccessor.sandtimer.view.fragment.ClockFragment
import app.appsuccessor.sandtimer.view.fragment.StopWatchFragment
import app.appsuccessor.sandtimer.view.fragment.TimerFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {
    private lateinit var ui: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this@MainActivity, "Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                this@MainActivity, "Please enable notification to get updates", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val navIconsActive = arrayListOf(
        R.drawable.ic_timer_active,
        R.drawable.ic_clock_active,
        R.drawable.ic_alarm_active,
        R.drawable.ic_stopwatch_active,
        R.drawable.ic_bedtime_active
    )
    private val navLabels = arrayListOf(
        "Timer",
        "Clock",
        "Alarm",
        "Stopwatch",
        "Bedtime"
    )

    private val navIcons = arrayListOf(
        R.drawable.ic_timer_inactive,
        R.drawable.ic_clock_inactive,
        R.drawable.ic_alarm_inactive,
        R.drawable.ic_stopwatch_inactive,
        R.drawable.ic_bedtime_inactive
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)

        // Check notification permission
        checkNotificationPermission()

        getAllTimeZonesList()

        newTabSetUp()
        requestOverlayPermission()
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        } else {
            // Permission Granted - Overlay functionality is available
        }
    }


    private fun newTabSetUp() {
        val adapter = CustomPagerAdapter(this)
        ui.viewPager.adapter = adapter
        val navigation = ui.tabLayout

        ui.viewPager.setPageTransformer(DepthPageTransformer())
        ui.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                for (i in 0 until navigation.tabCount) {
                    val tab = navigation.getTabAt(i)
                    if (tab != null) {
                        val tabView = tab.customView
                        val tabLabel = tabView?.findViewById<TextView>(R.id.nav_label)
                        val tabIcon = tabView?.findViewById<ImageView>(R.id.nav_icon)

                        if (position == i) {
                            tabLabel?.visibility = View.VISIBLE
                            tabIcon?.setImageResource(navIconsActive[i])
                            // Set a smaller size for the icon of the first tab when selected
                            if (i == 0) {
                                tabIcon?.scaleX = 1.0f
                                tabIcon?.scaleY = 1.0f
                            } else {
                                // Set the original size for the icons of other tabs when selected
                                tabIcon?.scaleX = 1.2f
                                tabIcon?.scaleY = 1.2f
                            }
                        } else {
                            tabLabel?.visibility = View.GONE
                            tabIcon?.setImageResource(navIcons[i])
                            // Set a smaller size for the icon of the first tab when not selected
                            if (i == 0) {
                                tabIcon?.scaleX = 0.8f
                                tabIcon?.scaleY = 0.8f
                            } else {
                                // Set the original size for the icons of other tabs when not selected
                                tabIcon?.scaleX = 1.0f
                                tabIcon?.scaleY = 1.0f
                            }
                        }
                    }
                }
            }
        })

        TabLayoutMediator(navigation, ui.viewPager) { tab, position ->
            val tabView =
                LayoutInflater.from(this).inflate(R.layout.tab_title, null) as LinearLayout
            val tabLabel = tabView.findViewById<TextView>(R.id.nav_label)
            val tabIcon = tabView.findViewById<ImageView>(R.id.nav_icon)

            tabLabel.text = navLabels[position]
            if (position == 0) {
                tabLabel.visibility = View.VISIBLE
                tabIcon.setImageResource(navIconsActive[position])
                // Set a smaller size for the icon of the first tab
                tabIcon.scaleX = 1.0f
                tabIcon.scaleY = 1.0f
            } else {
                tabLabel.visibility = View.GONE
                tabIcon.setImageResource(navIcons[position])
                // Set the original size for the icons of other tabs
                tabIcon.scaleX = 1.2f
                tabIcon.scaleY = 1.2f
            }

            tab.customView = tabView
        }.attach()
        // Disable click functionality for tab selection
        for (i in 0 until navigation.tabCount) {
            val tab = navigation.getTabAt(i)
            tab?.view?.isClickable = false
        }
    }


    private fun checkNotificationPermission() {
        val permission = Manifest.permission.ACCESS_NOTIFICATION_POLICY
        val granted = PackageManager.PERMISSION_GRANTED
        val isPermissionGranted = ContextCompat.checkSelfPermission(this, permission) == granted

        if (!isPermissionGranted) {
            requestNotificationPermission()
        }
    }

    private fun requestNotificationPermission() {
        val permission = Manifest.permission.ACCESS_NOTIFICATION_POLICY
        val shouldShowRationale = shouldShowRequestPermissionRationale(permission)

        if (shouldShowRationale) {
            showPermissionSnackbar()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun showPermissionSnackbar() {
        val rootView = findViewById<View>(android.R.id.content)
        val snackbar = Snackbar.make(
            rootView,
            "Please grant notification permission to receive updates",
            Snackbar.LENGTH_INDEFINITE
        )

        snackbar.setAction("Grant") {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }

        snackbar.show()
    }

    private fun getAllTimeZonesList() {
        sharedPreferences = getSharedPreferences("AppPrefs", AppCompatActivity.MODE_PRIVATE)

        val cityApiClient = CityApiClient.create()
        val call = cityApiClient.getAllTimeZones()

        call.enqueue(object : Callback<List<String>> {
            override fun onResponse(
                call: Call<List<String>>, response: Response<List<String>>
            ) {
                if (response.isSuccessful) {
                    val timeZones = response.body()

                    // Save the time zones in shared preferences
                    val editor = sharedPreferences.edit()
                    editor.putStringSet("timeZones", timeZones?.toSet())
                    editor.apply()
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                // Handle the failure case here
            }
        })
    }

    inner class CustomPagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int {
            return navLabels.size
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> TimerFragment()
                1 -> ClockFragment()
                2 -> AlarmFragment()
                3 -> StopWatchFragment()
                4 -> BedTimeFragment()
                else -> TimerFragment()
            }
        }
    }
}

class DepthPageTransformer : ViewPager2.PageTransformer {
    private val MIN_SCALE = 0.75f

    override fun transformPage(page: View, position: Float) {
        when {
            position <= 0 -> {
                // Page is currently displayed
                page.translationX = 0f
                page.alpha = 1f
                page.scaleX = 1f
                page.scaleY = 1f
            }

            position <= 1 -> {
                // Page is to the right of the current page
                val scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position))
                page.alpha = 1 - position
                page.pivotY = 0.5f * page.height
                page.translationX = page.width * -position
                page.scaleX = scaleFactor
                page.scaleY = scaleFactor
            }

            else -> {
                // Page is to the left of the current page
                page.alpha = 0f
            }
        }
    }
}

