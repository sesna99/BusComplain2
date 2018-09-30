package trycatch.dev.buscomplain.View.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_list_bus.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.textColor
import trycatch.dev.buscomplain.DataModel.RouteInfoDataModel
import trycatch.dev.buscomplain.R
import trycatch.dev.buscomplain.Util

class BusListAdapter(private val context: Context, var items: MutableList<RouteInfoDataModel>) : RecyclerView.Adapter<BusListAdapter.ViewHolder>(), AnkoLogger {

    private lateinit var onClickListener: OnClick

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_list_bus, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.busNumber.text = items[position].busRouteNm
        holder.busNumber.textColor = ContextCompat.getColor(context, Util.busColors[items[position].routeType.toInt()]!!)
        holder.busType.text = Util.busTypes[items[position].routeType.toInt()]!!
        holder.itemView.setOnClickListener{
            onClickListener.onClick(it, position)
        }
    }

    fun setOnClick(onClick: OnClick) {
        onClickListener = onClick
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val busNumber = view.bus_number!!
        val busType = view.bus_type!!
    }

    interface OnClick {
        fun onClick(view: View, position: Int)
    }
}