package trycatch.dev.buscomplain.View.Activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_search_detail.*
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import org.jetbrains.anko.dip
import org.jetbrains.anko.makeCall
import org.jetbrains.anko.startActivity
import trycatch.dev.buscomplain.DataModel.BusDataModel
import trycatch.dev.buscomplain.R
import trycatch.dev.buscomplain.Util
import trycatch.dev.buscomplain.ViewModel.MapViewModel
import trycatch.dev.buscomplain.ViewModel.MapViewModelFactory

class SearchDetailActivity : AppCompatActivity(), MapView.POIItemEventListener, MapView.MapViewEventListener {

    private val mapViewModel by lazy {
        ViewModelProviders.of(this, MapViewModelFactory(applicationContext)).get(MapViewModel::class.java)
    }

    private val bottomSheetBehavior by lazy {
        BottomSheetBehavior.from(bottom_sheet)
    }

    private val disposable = CompositeDisposable()

    private lateinit var selectBus: BusDataModel

    private val PERMISSIONS_REQUEST_CALL_PHONE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_detail)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = intent.getStringExtra("busNumber")
        }

        map_view.removeAllPOIItems()

        disposable.add(
                mapViewModel.getBusById(intent.getStringExtra("routeId"))
                        .doOnComplete {
                            map_view.fitMapViewAreaToShowAllPOIItems()
                        }
                        .subscribe({
                    map_view.addPOIItem(
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
        )

        map_view.setPOIItemEventListener(this)
        map_view.setMapViewEventListener(this)

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home ->
                finish()
        }
        return super.onOptionsItemSelected(item)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CALL_PHONE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makeCall("120")
                }
                return
            }
        }
    }

}