package trycatch.dev.buscomplain.View.Activity

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_searchable.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.startActivity
import trycatch.dev.buscomplain.DataModel.RouteInfoDataModel
import trycatch.dev.buscomplain.R
import trycatch.dev.buscomplain.View.Adapter.BusListAdapter
import trycatch.dev.buscomplain.ViewModel.SearchViewModel
import trycatch.dev.buscomplain.ViewModel.SearchViewModelFactory


class SearchableActivity : AppCompatActivity(), AnkoLogger {
    private val searchViewModel by lazy {
        ViewModelProviders.of(this, SearchViewModelFactory(applicationContext)).get(SearchViewModel::class.java)
    }

    private val busListAdapter by lazy {
        BusListAdapter(applicationContext, items)
    }

    private val items = mutableListOf<RouteInfoDataModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searchable)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bus_list.layoutManager = LinearLayoutManager(this)
        bus_list.adapter = busListAdapter
        busListAdapter.setOnClick(object : BusListAdapter.OnClick {
            override fun onClick(view: View, position: Int) {
                startActivity<SearchDetailActivity>(
                        "busNumber" to items[position].busRouteNm,
                        "routeId" to items[position].busRouteId
                )
            }
        })

        val intent = intent
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            search.setQuery(query, false)
            searchQuery(query)
        }

        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                search.clearFocus()
                searchQuery(query!!)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home ->
                finish()
        }
        return super.onOptionsItemSelected(item)
    }

    fun searchQuery(query: String){
        items.clear()
        busListAdapter.notifyDataSetChanged()
        searchViewModel.getBusRouteList(query)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    busListAdapter.notifyDataSetChanged()
                }
                .subscribe({
                    error { it }
                    items.add(it)
                }){
                    it.printStackTrace()
                }
    }
}