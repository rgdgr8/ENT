/*
 * Copyright (c) 2019-2025 e.solutions GmbH
 * Copyright (c) 2026 Cariad SE
 * All rights reserved.
 *
 * Originally developed by e.solutions GmbH
 * Modified by: Christian Winter (T2-CH), Volkswagen Group / Cariad SE
 */
package technology.cariad.entertainment.metadataservice.view

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import technology.cariad.entertainment.metadataservice.BuildConfig
import technology.cariad.entertainment.metadataservice.R
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.model.CoverArtLookupModel
import technology.cariad.entertainment.metadataservice.model.MusicMetadataModel
import technology.cariad.entertainment.metadataservice.model.OnlineStateModel
import technology.cariad.entertainment.metadataservice.model.PermissionDataModel
import technology.cariad.entertainment.metadataservice.model.SongInfoModel
import technology.cariad.entertainment.metadataservice.model.SongLyricsModel
import technology.cariad.entertainment.metadataservice.model.StationAsiaModel
import technology.cariad.entertainment.metadataservice.model.StationModel
import technology.cariad.entertainment.metadataservice.service.ServiceHolder
import technology.cariad.entertainment.metadataservice.view.adapter.TabAdapter
import technology.cariad.entertainment.metadataserviceclient.api.IMetadataServiceAidl
import technology.cariad.entertainment.metadataserviceclient.api.IMetadataServiceClientAdapter
import cariad.technology.metadataservice.view.StatusFragment

/**
 * Callback interface to communicate with the activity from child fragments.
 */
interface StationSelectionListener {

    /**
     * Called when a station is selected.
     * @param bearer The bearer of the selected station.
     */
    fun onStationSelected(bearer: String)

    /**
     * Called when a station is removed.
     * @param bearer The bearer of the removed station.
     */
    fun onStationRemoved(bearer: String)
}

/**
 * Main activity of the metadata service demo application.
 * It hosts a set of fragments displaying the actual content.
 * The code within this demo is not production ready and only meant for testing purposes.
 * It is ugly and not following best practices in any way.
 */
class DemoActivity : AppCompatActivity(), StationSelectionListener {

    private val log by logger()

    private lateinit var tabAdapter: TabAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager

    /**
     * The single instance of the station model used throughout the demo app.
     * This can be accessed from fragments by casting the hosting activity to the concrete type.
     */
    lateinit var stationModel: StationModel
    lateinit var stationAsiaModel: StationAsiaModel
    lateinit var songInfoModel: SongInfoModel
    lateinit var songLyricsModel: SongLyricsModel
    lateinit var coverArtLookupModel: CoverArtLookupModel
    lateinit var onlineStateModel: OnlineStateModel
    lateinit var permissionDataModel: PermissionDataModel
    lateinit var musicMetadataModel: MusicMetadataModel

    private var service = ServiceHolder()

    private var serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            log.info(this::onServiceConnected.name) { "service connected" }

            service.instance = IMetadataServiceClientAdapter.fromBinder(binder, this@DemoActivity)

            this@DemoActivity.runOnUiThread {
                Toast.makeText(this@DemoActivity, "service connected", Toast.LENGTH_SHORT).show()
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            log.info(this::onServiceDisconnected.name) { "service disconnected" }

            service.instance = null
            abortRequests()

            startService()

            this@DemoActivity.runOnUiThread {
                Toast.makeText(this@DemoActivity, "service disconnected", Toast.LENGTH_SHORT)
                    .show()
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                )
            }
        }
    }

    /**
     * @see [AppCompatActivity.onCreate]
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)
        setContentView(R.layout.activity_demo)

        initView()
        initFragments()
        initModel()

        startService()
    }

    override fun onDestroy() {
        unbindService(serviceConnection)
        super.onDestroy()
    }

    private fun startService() {
        val implicit = Intent(BuildConfig.METADATA_SERVICE_AIDL_ACTION)
        val matches = packageManager.queryIntentServices(implicit, 0)
        if (matches.size == 1) {
            val explicit = Intent(implicit)
            val serviceInfo = matches[0].serviceInfo
            val compName = ComponentName(serviceInfo.applicationInfo.packageName, serviceInfo.name)
            explicit.component = compName
            val success = bindService(explicit, serviceConnection, Context.BIND_AUTO_CREATE)
            log.info("onCreate") { "bindService success=$success" }
        } else {
            log.error("onCreate") {
                "bindService failed - no service found or multiple instances running"
            }
        }
    }

    private fun initView() {
        viewPager = findViewById(R.id.fragmentNavigationViewPager)
        viewPager.isSaveEnabled = false
        tabLayout = findViewById(R.id.fragmentNavigationTabLayout)

        tabAdapter = TabAdapter(supportFragmentManager)

        viewPager.adapter = tabAdapter
        tabLayout.setupWithViewPager(viewPager)

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                // Force a refresh of the Settings screen whenever it becomes visible so it
                // never shows stale values after switching tabs.
                val fragment = tabAdapter.getItem(position)
                if (fragment is SettingsFragment) {
                    fragment.refreshSettings()
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun initFragments() {
        tabAdapter.addFragment(StatusFragment(), "Status")
        tabAdapter.addFragment(StationListFragment(), "Radio Station")
        tabAdapter.addFragment(CoverArtLookupFragment(), "Cover Art")
        tabAdapter.addFragment(SongInfoFragment(), "Song Recog.")
        // Hidden until fully functional:
        // tabAdapter.addFragment(SongLyricsFragment(), "Song Lyrics")
        // tabAdapter.addFragment(MusicMetadataFragment(), "Music Metadata")
        // tabAdapter.addFragment(PermissionDataFragment(), "Permissions")
        tabAdapter.addFragment(SettingsFragment(), "Settings")

        // Keep all tab fragments alive so their views are not destroyed/recreated on every
        // tab switch (which caused the Settings screen to occasionally not refresh).
        viewPager.offscreenPageLimit = (tabAdapter.count - 1).coerceAtLeast(1)
    }

    private fun initModel() {
        stationModel = StationModel(service)
        stationAsiaModel = StationAsiaModel(service)
        songInfoModel = SongInfoModel(service)
        songLyricsModel = SongLyricsModel(service)
        onlineStateModel = OnlineStateModel(service)
        coverArtLookupModel = CoverArtLookupModel(service)
        permissionDataModel = PermissionDataModel(service)
        musicMetadataModel = MusicMetadataModel(service)
    }

    private fun abortRequests() {
        stationModel.reset()
        stationAsiaModel.reset()
        songInfoModel.reset()
        songLyricsModel.reset()
        onlineStateModel.reset()
        coverArtLookupModel.reset()
        permissionDataModel.reset()
        musicMetadataModel.reset()
    }

    /**
     * @see [StationSelectionListener.onStationSelected]
     */
    override fun onStationSelected(bearer: String) {
        val bundle = Bundle()
        bundle.putString("bearer", bearer)

        val fragment = StationDetailsFragment()
        fragment.arguments = bundle

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack("StationList")
        transaction.commit()
    }

    /**
     * @see [StationSelectionListener.onStationRemoved]
     */
    override fun onStationRemoved(bearer: String) {
        stationModel.removeStation(bearer)
    }

    /**
     * Callback when the back buttons is clicked
     * @param view The view of the button
     */
    @Suppress("UNUSED_PARAMETER")
    fun onBackClicked(view: View) {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        }
    }
}

