package technology.cariad.entertainment.metadataservice.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import technology.cariad.entertainment.metadataservice.R
import technology.cariad.entertainment.metadataserviceclient.api.ProviderIdentifier

/**
 * [RecyclerView.Adapter] between a list of [ProviderIdentifier] objects and the [RecyclerView].
 */
class ProviderIdentifierListAdapter :
    RecyclerView.Adapter<ProviderIdentifierListAdapter.ProviderIdentifierListViewHolder>() {

    /**
     * The underlying data set used by the adapter to populate the view.
     */
    var providerIdentifierList = listOf<ProviderIdentifier>()
        set(list) {
            field = list
            notifyDataSetChanged()
        }

    /**
     * [RecyclerView.ViewHolder] wrapping a single row of the [RecyclerView].
     */
    inner class ProviderIdentifierListViewHolder(val providerIdentifierListRow: View) :
        RecyclerView.ViewHolder(providerIdentifierListRow)

    /**
     * @see [RecyclerView.Adapter.onCreateViewHolder]
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProviderIdentifierListViewHolder {
        val row =
            LayoutInflater.from(parent.context).inflate(R.layout.provider_identifier_item, parent, false)
        return ProviderIdentifierListViewHolder(row)
    }

    /**
     * @see [RecyclerView.Adapter.onBindViewHolder]
     */
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ProviderIdentifierListViewHolder, position: Int) {
        val originValueTextView =
            holder.providerIdentifierListRow.findViewById<TextView>(R.id.originValueTextView)
        val idValueTextView =
            holder.providerIdentifierListRow.findViewById<TextView>(R.id.idValueTextView)

        with(providerIdentifierList[position]) {
            val originString = when (originType) {
                ProviderIdentifier.ORIGIN_TYPE_RADIODNS -> "RadioDNS"
                ProviderIdentifier.ORIGIN_TYPE_RADIOPLAYER -> "Radioplayer"
                ProviderIdentifier.ORIGIN_TYPE_GRACENOTE -> "Gracenote"
                else -> "Invalid Origin"
            }
            originValueTextView.text = "$originString(${this.originType})"
            idValueTextView.text = id
        }
    }

    /**
     * @see [RecyclerView.Adapter.getItemCount]
     */
    override fun getItemCount() = providerIdentifierList.size
}

