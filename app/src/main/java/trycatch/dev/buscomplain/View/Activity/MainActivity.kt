package trycatch.dev.buscomplain.View.Activity

import android.Manifest
import android.app.SearchManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.navigation.NavigationView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import org.jetbrains.anko.*
import trycatch.dev.buscomplain.DataModel.BusDataModel
import trycatch.dev.buscomplain.R
import trycatch.dev.buscomplain.Util
import trycatch.dev.buscomplain.ViewModel.MapViewModel
import trycatch.dev.buscomplain.ViewModel.MapViewModelFactory

class MainActivity : AppCompatActivity(), AnkoLogger, NavigationView.OnNavigationItemSelectedListener, MapView.POIItemEventListener, MapView.MapViewEventListener, MapView.CurrentLocationEventListener {

    private val mapViewModel by lazy {
        ViewModelProviders.of(this, MapViewModelFactory(applicationContext)).get(MapViewModel::class.java)
    }

    private val bottomSheetBehavior by lazy {
        BottomSheetBehavior.from(bottom_sheet)
    }

    private val locationManager by lazy {
        getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private lateinit var mapView: MapView

    private val disposable = CompositeDisposable()

    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0
    private val PERMISSIONS_REQUEST_CALL_PHONE = 1


    private val items: MutableList<BusDataModel> = mutableListOf()

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private lateinit var selectBus: BusDataModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = object:ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                drawer_layout.bringChildToFront(drawerView)
                drawer_layout.requestLayout()
            }
        }
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        mapViewModel.update()

        refresh.setOnClickListener {
            items.clear()
            draweMapPOI()
        }

        call.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.CALL_PHONE),
                        PERMISSIONS_REQUEST_CALL_PHONE)
            }
            else {
                makeCall("120")
            }
        }

        complain.setOnClickListener {
            startActivity<ComplainActivity>(
                    "company" to "${selectBus.corpNm}(주)",
                    "busNumber" to selectBus.busRouteNm,
                    "carNumber" to selectBus.plainNo
            )
        }

        compliment.setOnClickListener {
            startActivity<ComplimentActivity>(
                    "company" to "${selectBus.corpNm}(주)",
                    "busNumber" to selectBus.busRouteNm,
                    "carNumber" to selectBus.plainNo
            )
        }
    }

    override fun onResume() {
        super.onResume()

        initMapView()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
        else{
            if(items.size == 0) {
                getCurrentGps()
            }
            else{
                draweMapPOI()
            }
        }


    }

    private fun initMapView(){
        map_view.removeAllViews()
        mapView = MapView(this)
        map_view.addView(mapView)
        MapView.setMapTilePersistentCacheEnabled(true)

        mapView.setPOIItemEventListener(this)
        mapView.setMapViewEventListener(this)
    }

    private fun getCurrentGps(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0F, locationListener)
        }
        else{
            toast("권한을 허용해주세요.")
        }
    }

    private fun draweMapPOI(){
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude), true)
        mapView.removeAllPOIItems()
        if(items.size == 0) {
            disposable.add(
                    mapViewModel.getSurroundBus(longitude, latitude)
                            .subscribeOn(AndroidSchedulers.mainThread())
                            .doOnSubscribe {
                                refresh.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_animation))
                            }
                            .doOnComplete {
                                refresh.clearAnimation()
                            }
                            .subscribe({
                                mapView.addPOIItem(
                                        MapPOIItem().apply {
                                            itemName = "${it.busRouteNm}"
                                            mapPoint = MapPoint.mapPointWithGeoCoord(it.gpsY.toDouble(), it.gpsX.toDouble())
                                            markerType = MapPOIItem.MarkerType.CustomImage
                                            customImageResourceId = Util.busImg[it.busType.toInt()]!!
                                            userObject = it
                                            isShowCalloutBalloonOnTouch = true
                                            isShowDisclosureButtonOnCalloutBalloon = false
                                        }
                                )
                                items.add(it)
                            }) {
                                it.printStackTrace()
                            }
            )
        }
        else{
            items.toObservable()
                    .subscribeOn(Schedulers.newThread())
                    .subscribe({
                        mapView.addPOIItem(
                                MapPOIItem().apply {
                                    itemName = "${it.busRouteNm}"
                                    mapPoint = MapPoint.mapPointWithGeoCoord(it.gpsY.toDouble(), it.gpsX.toDouble())
                                    markerType = MapPOIItem.MarkerType.CustomImage
                                    customImageResourceId = Util.busImg[it.busType.toInt()]!!
                                    userObject = it
                                    isShowCalloutBalloonOnTouch = true
                                    isShowDisclosureButtonOnCalloutBalloon = false
                                }
                        )
                    }){
                        it.printStackTrace()
                    }
        }
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location?) {
            latitude = location?.latitude ?: 37.534701
            longitude = location?.longitude ?: 127.095245

            locationManager.removeUpdates(this)

            draweMapPOI()
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }

        override fun onProviderEnabled(provider: String?) {
        }

        override fun onProviderDisabled(provider: String?) {
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(items.size == 0) {
                        getCurrentGps()
                    }
                    else{
                        draweMapPOI()
                    }
                }
                return
            }

            PERMISSIONS_REQUEST_CALL_PHONE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makeCall("120")
                }
                return
            }
        }
    }

    override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {
    }

    override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?, p2: MapPOIItem.CalloutBalloonButtonType?) {
    }

    override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {
    }

    override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {
        val info = p1?.userObject
        if (info is BusDataModel) {
            error { info }
            selectBus = info
            bus_number.text = info.busRouteNm
            //bus_number.text = Regex("[^0-9]").replace(info.busRouteNm, "")
            car_number.text = info.plainNo
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            bottomSheetBehavior.peekHeight = dip(150)
        }
    }

    override fun onMapViewDoubleTapped(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewInitialized(p0: MapView?) {
    }

    override fun onMapViewDragStarted(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewMoveFinished(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewDragEnded(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {
        bottomSheetBehavior.peekHeight = 0
    }

    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {
    }

    override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {
    }

    override fun onCurrentLocationUpdateFailed(p0: MapView?) {
    }

    override fun onCurrentLocationUpdate(p0: MapView?, p1: MapPoint?, p2: Float) {
    }

    override fun onCurrentLocationUpdateCancelled(p0: MapView?) {
    }

    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_complain -> {
                startActivity<ComplainActivity>(
                        "company" to "",
                        "busNumber" to "",
                        "carNumber" to ""
                )
            }
            R.id.nav_compliment -> {
                startActivity<ComplimentActivity>(
                        "company" to "",
                        "busNumber" to "",
                        "carNumber" to ""
                )
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
        }

        return true
    }
}
