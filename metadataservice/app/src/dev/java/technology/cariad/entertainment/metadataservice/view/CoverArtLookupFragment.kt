package technology.cariad.entertainment.metadataservice.view

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import technology.cariad.entertainment.metadataservice.R
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.databinding.FragmentCoverArtLookupBinding
import technology.cariad.entertainment.metadataservice.model.CoverArtLookupModel
import technology.cariad.entertainment.metadataserviceclient.api.CoverArtRequestData
import java.util.Observable
import java.util.Observer

class CoverArtLookupFragment : Fragment(), Observer {

    private val log by logger()

    private lateinit var coverArtLookupModel: CoverArtLookupModel

    private lateinit var artistEditText: EditText
    private lateinit var albumEditText: EditText
    private lateinit var titleEditText: EditText
    private lateinit var binding: FragmentCoverArtLookupBinding

    /**
     * @see [Observer.update]
     */
    override fun update(o: Observable?, arg: Any?) {

        val uri = coverArtLookupModel.getCoverArtImageUri()

        try {
            val coverArtBitmap = with(Uri.parse(uri)) {
                requireContext().contentResolver.openInputStream(this).use {
                    BitmapFactory.decodeStream(it)
                }
            }
            binding.coverArtImageView.setImageBitmap(coverArtBitmap)

        } catch (e: Exception) {
            log.error(this::update.name) { "${e.message}" }

            binding.coverArtImageView.setImageResource(R.drawable.no_image_available)
        }

        log.info(this::update.name) { "Cover art image is updated" }
    }

    /*
    * @see [Fragment.onCreateView]
    */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCoverArtLookupBinding.inflate(layoutInflater)
        log.info(this::onCreateView.name) { "cover art lookup fragment is added to the view" }

        coverArtLookupModel = (activity as DemoActivity).coverArtLookupModel

        artistEditText = binding.artistEditText
        albumEditText = binding.albumEditText
        titleEditText = binding.titleEditText

        return binding.root
    }

    /**
     * @see [Fragment.onResume]
     */
    override fun onResume() {
        super.onResume()

        coverArtLookupModel.addObserver(this)
        binding.searchCoverArtButton.setOnClickListener {
            // Cleanup UI just before requesting another cover art image
            binding.coverArtImageView.setImageResource(R.drawable.no_image_available)

            val coverArtRequestData = CoverArtRequestData(
                artist = artistEditText.text?.toString(),
                album = albumEditText.text?.toString(),
                title = titleEditText.text?.toString()

            )
            coverArtLookupModel.searchForCoverArt(coverArtRequestData)

            log.info(this::onResume.name) { "search cover art button is clicked" }
        }
    }

    /**
     * @see [Fragment.onPause]
     */
    override fun onPause() {
        super.onPause()

        coverArtLookupModel.disposeCoverArtLookupRequest()
        coverArtLookupModel.deleteObserver(this)
    }
}
