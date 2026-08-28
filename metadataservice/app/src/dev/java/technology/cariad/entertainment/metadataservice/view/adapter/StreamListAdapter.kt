package technology.cariad.entertainment.metadataservice.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import technology.cariad.entertainment.metadataservice.R
import technology.cariad.entertainment.metadataserviceclient.api.StationStream

/**
 * [RecyclerView.Adapter] between a list of [StationStream] objects and the [RecyclerView].
 */
class StreamListAdapter : RecyclerView.Adapter<StreamListAdapter.StreamListViewHolder>() {

    /**
     * The underlying data set used by the adapter to populate the view.
     */
    var streamList = listOf<StationStream>()
        set(list) {
            field = list
            notifyDataSetChanged()
        }

    /**
     * [RecyclerView.ViewHolder] wrapping a single row of the [RecyclerView].
     */
    inner class StreamListViewHolder(val streamListRow: View) :
        RecyclerView.ViewHolder(streamListRow)

    /**
     * @see [RecyclerView.Adapter.onCreateViewHolder]
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StreamListViewHolder {
        val row =
            LayoutInflater.from(parent.context).inflate(R.layout.stream_list_item, parent, false)
        return StreamListViewHolder(row)
    }

    /**
     * @see [RecyclerView.Adapter.onBindViewHolder]
     */
    override fun onBindViewHolder(holder: StreamListViewHolder, position: Int) {
        val uriValueTextView = holder.streamListRow.findViewById<TextView>(R.id.uriValueTextView)
        val offsetValueTextView =
            holder.streamListRow.findViewById<TextView>(R.id.offsetValueTextView)
        val qualityValueTextView =
            holder.streamListRow.findViewById<TextView>(R.id.qualityValueTextView)

        with(streamList[position]) {
            val qualityStr =
                when(quality) {
                    1 -> "High"
                    2 -> "Low"
                    else -> "Unknown"
                }

            uriValueTextView.text = url
            offsetValueTextView.text = offsetMillis.toString()
            qualityValueTextView.text = "$qualityStr($quality)"
        }
    }

    /**
     * @see [RecyclerView.Adapter.getItemCount]
     */
    override fun getItemCount() = streamList.size
}

