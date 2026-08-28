package technology.cariad.entertainment.metadataservice.view

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import technology.cariad.entertainment.metadataservice.R
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.databinding.FragmentStationAsiaBinding
import technology.cariad.entertainment.metadataservice.model.StationAsiaModel
import technology.cariad.entertainment.metadataserviceclient.api.StationBearerAsia
import technology.cariad.entertainment.metadataserviceclient.api.StationBearerAsia.Companion.TYPE_AM
import technology.cariad.entertainment.metadataserviceclient.api.StationBearerAsia.Companion.TYPE_FM
import java.util.Observable
import java.util.Observer

class StationAsiaFragment : Fragment(), Observer {

    private val log by logger()

    private lateinit var stationAsiaModel: StationAsiaModel
    private lateinit var binding: FragmentStationAsiaBinding

    /**
     * @see [Observer.update]
     */
    override fun update(o: Observable?, arg: Any?) {

        val station = stationAsiaModel.getStation()

        try {
            val logo = with(Uri.parse(station?.logo?.uri)) {
                requireContext().contentResolver.openInputStream(this).use {
                    BitmapFactory.decodeStream(it)
                }
            }
            binding.logoImageView.setImageBitmap(logo)


        } catch (e: Exception) {
            log.error(this::update.name) { "${e.message}" }

            binding.logoImageView.setImageResource(R.drawable.no_image_available)
        }

        binding.nameTextView.text = station?.name
        binding.providerTextView.text = station?.providerIdentifier?.id

        log.info(this::update.name) { "asia station data updated" }
    }

    /**
     * @see [Fragment.onCreateView]
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStationAsiaBinding.inflate(layoutInflater)
        log.info(this::onCreateView.name) { "asia station fragment is added to the view" }

        stationAsiaModel = (activity as DemoActivity).stationAsiaModel

        return binding.root
    }

    /**
     * @see [Fragment.onResume]
     */
    override fun onResume() {
        super.onResume()

        stationAsiaModel.addObserver(this)

        binding.mccEditText.doAfterTextChanged {
            val mcc = it.toString()
            if (mcc.length != 3 || mcc[0] != '4') {
                binding.mccEditText.error = "invalid mcc for asia region"
            }
        }

        binding.frequencyEditText.doAfterTextChanged {
            val freq = it.toString()
            if (freq.isEmpty()) {
                binding.frequencyEditText.error = "invalid frequency"
            } else {
                try {
                    freq.toInt()
                } catch (e: NumberFormatException) {
                    binding.frequencyEditText.error = "invalid frequency"
                }
            }
        }

        binding.requestButton.setOnClickListener {
            if (binding.frequencyEditText.error != null || binding.mccEditText.error != null ||
                binding.frequencyEditText.text.isEmpty() || binding.mccEditText.text.isEmpty()
            ) {
                return@setOnClickListener
            }

            binding.logoImageView.setImageResource(R.drawable.no_image_available)
            binding.nameTextView.text = ""
            binding.providerTextView.text = ""

            val type = when (binding.typeSpinner.selectedItem.toString()) {
                "AM" -> TYPE_AM
                "FM" -> TYPE_FM
                else -> TYPE_FM //default to FM for no particular reason
            }

            val freq = "${binding.frequencyEditText.text}"
            val mcc = "${binding.mccEditText.text}"

            val bearer = StationBearerAsia(type, freq, mcc)
            stationAsiaModel.requestStation(bearer)

            log.info(this::onResume.name) { "request station for bearer=$bearer" }
        }
    }

    /**
     * @see [Fragment.onPause]
     */
    override fun onPause() {
        super.onPause()

        stationAsiaModel.disposeRequest()
        stationAsiaModel.deleteObserver(this)
    }
}

