package technology.cariad.entertainment.metadataservice.view

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import technology.cariad.entertainment.metadataservice.R
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.common.util.toBearerString
import technology.cariad.entertainment.metadataservice.domain.entity.IBearerEntity
import technology.cariad.entertainment.metadataservice.model.StationModel
import technology.cariad.entertainment.metadataservice.service.mapper.BearerMapper
import technology.cariad.entertainment.metadataservice.view.adapter.ProviderIdentifierListAdapter
import technology.cariad.entertainment.metadataservice.view.adapter.StreamListAdapter
import technology.cariad.entertainment.metadataserviceclient.api.Station
import technology.cariad.entertainment.metadataserviceclient.util.TypedBearer
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Observable
import java.util.Observer

/**
 * Fragment displaying the details of a [Station]
 */
class StationDetailsFragment : Fragment(), Observer {

    private val log by logger()

    private var stationModel: StationModel? = null
    private var bearer: String = ""
    private var station: Station? = null

    //main
    private lateinit var requestedBearerValueTextView: TextView
    private lateinit var lastUpdatedValueTextView: TextView

    //logo
    private lateinit var logoImageView: ImageView
    private lateinit var logoUriValueTextView: TextView

    //logo small
    private lateinit var iconImageView: ImageView
    private lateinit var iconUriValueTextView: TextView

    //alternative bearers
    private lateinit var alternativeBearersTextView: TextView

    //metadata
    private lateinit var shortNameTextView: TextView
    private lateinit var mediumNameTextView: TextView
    private lateinit var longNameTextView: TextView
    private lateinit var descriptionValueTextView: TextView

    //genre
    private lateinit var genresValueTextView: TextView

    //streams
    private lateinit var streamsRecyclerView: RecyclerView
    private lateinit var streamsListAdapter: StreamListAdapter
    private lateinit var streamsViewManager: RecyclerView.LayoutManager

    //provider identifiers
    private lateinit var providerIdentifiersRecyclerView: RecyclerView
    private lateinit var providerIdentifiersListAdapter: ProviderIdentifierListAdapter
    private lateinit var providerIdentifiersViewManager: RecyclerView.LayoutManager

    /**
     * @see [Fragment.onCreateView]
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_station_details, container, false)

        //main
        requestedBearerValueTextView = view.findViewById(R.id.requestedBearerValueTextView)
        lastUpdatedValueTextView = view.findViewById(R.id.lastUpdatedValueTextView)

        //logo
        logoImageView = view.findViewById(R.id.logoImageView)
        logoUriValueTextView = view.findViewById(R.id.logoUriValueTextView)

        //logo small
        iconImageView = view.findViewById(R.id.iconImageView)
        iconUriValueTextView = view.findViewById(R.id.iconUriValueTextView)

        //alternative bearers
        alternativeBearersTextView = view.findViewById(R.id.alternativeBearersTextView)

        //metadata
        shortNameTextView = view.findViewById(R.id.shortNameValueTextView)
        mediumNameTextView = view.findViewById(R.id.mediumNameValueTextView)
        longNameTextView = view.findViewById(R.id.longNameValueTextView)
        descriptionValueTextView = view.findViewById(R.id.descriptionValueTextView)

        //genres
        genresValueTextView = view.findViewById(R.id.genresValueTextView)

        //streams
        streamsRecyclerView = view.findViewById(R.id.streamsRecyclerView)
        streamsViewManager = LinearLayoutManager(requireContext())
        streamsListAdapter = StreamListAdapter()

        streamsRecyclerView.apply {
            layoutManager = streamsViewManager
            adapter = streamsListAdapter
        }

        streamsRecyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        )

        //provider identifiers
        providerIdentifiersRecyclerView = view.findViewById(R.id.providerIdentifiersRecyclerView)
        providerIdentifiersViewManager = LinearLayoutManager(requireContext())
        providerIdentifiersListAdapter = ProviderIdentifierListAdapter()

        providerIdentifiersRecyclerView.apply {
            layoutManager = providerIdentifiersViewManager
            adapter = providerIdentifiersListAdapter
        }

        providerIdentifiersRecyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        )

        bearer = arguments?.getString("bearer") ?: ""

        stationModel = (activity as DemoActivity).stationModel
        station = stationModel?.getStation(bearer)

        updateUI()

        return view
    }

    /**
     * @see [Fragment.onResume]
     */
    override fun onResume() {
        super.onResume()

        stationModel?.addObserver(this)
    }

    /**
     * @see [Fragment.onPause]
     */
    override fun onPause() {
        super.onPause()

        stationModel?.deleteObserver(this)
    }

    /**
     * @see [Observer.update]
     */
    override fun update(o: Observable?, arg: Any?) {
        val changedBearer = arg as? String
        if (changedBearer == null || changedBearer == bearer) {

            station = stationModel?.getStation(bearer)
            activity?.runOnUiThread {
                updateUI()
            }
        }
    }

    private fun updateUI() {

        //main
        requestedBearerValueTextView.text = bearer
        lastUpdatedValueTextView.text =
            ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)

        //logo
        try {
            val logoBitmap = with(Uri.parse(station?.logo?.uri)) {
                requireContext().contentResolver.openInputStream(this).use {
                    BitmapFactory.decodeStream(it)
                }
            }
            logoImageView.setImageBitmap(logoBitmap)
            logoImageView.background = null

        } catch (e: Exception) {
            log.error(this::updateUI.name) { "logo: ${e.message}" }

            logoImageView.setImageResource(R.drawable.no_image_available)
        }

        logoUriValueTextView.text = station?.logo?.uri ?: ""

        //logo small
        try {
            val iconBitmap = with(Uri.parse(station?.logoSmall?.uri)) {
                requireContext().contentResolver.openInputStream(this).use {
                    BitmapFactory.decodeStream(it)
                }
            }
            iconImageView.setImageBitmap(iconBitmap)
            iconImageView.background = null

        } catch (e: Exception) {
            log.error(this::updateUI.name) { "logo small: ${e.message}" }

            iconImageView.setImageResource(R.drawable.no_image_available)
        }

        iconUriValueTextView.text = station?.logoSmall?.uri ?: ""

        //alternative bearers
        val bearerBuilder = StringBuilder()

        station?.bearers?.forEach {
            val typedBearer = TypedBearer.fromRawBearer(it)!!
            val bearerEntity: IBearerEntity = BearerMapper().mapValue(typedBearer)
            bearerBuilder.append("${bearerEntity.toBearerString()}\n")
        }
        if (bearerBuilder.isNotEmpty()) {
            bearerBuilder.setLength(bearerBuilder.length - 1)
            alternativeBearersTextView.text = bearerBuilder.toString()
        } else {
            alternativeBearersTextView.text = ""
        }

        //metadata
        shortNameTextView.text = station?.metadata?.shortName ?: ""
        mediumNameTextView.text = station?.metadata?.mediumName ?: ""
        longNameTextView.text = station?.metadata?.longName ?: ""
        descriptionValueTextView.text = station?.metadata?.description ?: ""

        //genres
        genresValueTextView.text =
            station?.metadata?.genres?.reduceOrNull { acc, s -> "$acc $s" } ?: ""

        //streams
        streamsListAdapter.streamList = station?.streams ?: listOf()

        //providerIds
        providerIdentifiersListAdapter.providerIdentifierList =
            station?.providerIdentifiers ?: listOf()
    }
}

