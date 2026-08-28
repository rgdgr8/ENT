package technology.cariad.entertainment.metadataservice.view.adapter

import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import technology.cariad.entertainment.metadataservice.R
import technology.cariad.entertainment.metadataservice.view.StationSelectionListener
import technology.cariad.entertainment.metadataserviceclient.api.Station

/**
 * [RecyclerView.Adapter] between a list of [Station] objects and the [RecyclerView].
 */
class StationListAdapter(
    private val stationList: List<Pair<String, Station?>>,
    private val selectionListener: StationSelectionListener
) :
    RecyclerView.Adapter<StationListAdapter.StationListViewHolder>() {

    /**
     * [RecyclerView.ViewHolder] wrapping a single row of the [RecyclerView].
     */
    inner class StationListViewHolder(val stationListRow: View) :
        RecyclerView.ViewHolder(stationListRow), View.OnClickListener, View.OnLongClickListener {

        init {
            stationListRow.setOnClickListener(this)
            stationListRow.setOnLongClickListener(this)
        }

        /**
         * @see [View.OnClickListener.onClick]
         */
        override fun onClick(v: View) {
            val bearerTextView = v.findViewById<TextView>(R.id.bearerValueTextView)
            val bearer = bearerTextView.text.toString()

            selectionListener.onStationSelected(bearer)
        }

        /**
         * @see [View.OnLongClickListener.onLongClick]
         */
        override fun onLongClick(v: View): Boolean {
            val bearerTextView = v.findViewById<TextView>(R.id.bearerValueTextView)
            val bearer = bearerTextView.text.toString()
            selectionListener.onStationRemoved(bearer)
            return true
        }
    }

    /**
     * @see [RecyclerView.Adapter.onCreateViewHolder]
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationListViewHolder {

        val row =
            LayoutInflater.from(parent.context).inflate(R.layout.station_list_item, parent, false)
        return StationListViewHolder(row)
    }

    /**
     * @see [RecyclerView.Adapter.onBindViewHolder]
     */
    override fun onBindViewHolder(holder: StationListViewHolder, position: Int) {
        val bearerTextView = holder.stationListRow.findViewById<TextView>(R.id.bearerValueTextView)
        bearerTextView.text = stationList[position].first

        val station = stationList[position].second ?: return

        val logoView = holder.stationListRow.findViewById<ImageView>(R.id.logoImageView)
        val uriString = station.logo?.uri

        if (uriString != null) {
            logoView.background = null
            logoView.setImageURI(Uri.parse(uriString))
        } else {
            logoView.setBackgroundResource(R.drawable.no_image_available)
        }

        val nameTextView = holder.stationListRow.findViewById<TextView>(R.id.nameValueTextView)
        nameTextView.text = station.metadata?.let {
            listOf(it.mediumName, it.shortName, it.longName).firstOrNull { name ->
                !name.isNullOrEmpty()
            }
        }

        val genreTextView = holder.stationListRow.findViewById<TextView>(R.id.genreValueTextView)
        genreTextView.text = station.metadata?.genres?.reduceOrNull { acc, s -> "$acc $s" } ?: ""
    }

    /**
     * @see [RecyclerView.Adapter.onViewRecycled]
     */
    override fun onViewRecycled(holder: StationListViewHolder) {
        super.onViewRecycled(holder)

        val logoView = holder.stationListRow.findViewById<ImageView>(R.id.logoImageView)
        (logoView.drawable as? BitmapDrawable)?.bitmap
            ?.apply { if (!isRecycled) recycle() }
        logoView.setImageBitmap(null)
    }

    /**
     * @see [RecyclerView.Adapter.getItemCount]
     */
    override fun getItemCount() = stationList.size
}

