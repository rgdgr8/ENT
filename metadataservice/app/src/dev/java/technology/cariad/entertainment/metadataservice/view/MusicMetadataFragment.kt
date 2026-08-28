package technology.cariad.entertainment.metadataservice.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.databinding.FragmentMusicMetadataBinding
import technology.cariad.entertainment.metadataservice.model.MusicMetadataModel
import technology.cariad.entertainment.metadataserviceclient.api.MusicMetadata
import org.json.JSONObject
import java.util.Observable
import java.util.Observer

class MusicMetadataFragment : Fragment(), Observer {

    private val log by logger()

    private lateinit var binding: FragmentMusicMetadataBinding
    private lateinit var musicMetadataModel: MusicMetadataModel

    /**
     * @see [Observer.update]
     */
    override fun update(o: Observable?, arg: Any?) {
        val recordingMetadataByName = musicMetadataModel.getRecordingMetadataByName()
        val recordingMetadata = musicMetadataModel.getRecordingMetadata()
        val albumMetadata = musicMetadataModel.getAlbumMetadata()
        val artistMetadata = musicMetadataModel.getArtistMetadata()

        binding.recordingMetadataByNameTextView.text = metadataToString(recordingMetadataByName)
        binding.recordingMetadataTextView.text = metadataToString(recordingMetadata)
        binding.albumMetadataTextView.text = metadataToString(albumMetadata)
        binding.artistMetadataTextView.text = metadataToString(artistMetadata)

        log.info(this::update.name) { "updated metadata" }
    }

    /**
     * @see [Fragment.onCreateView]
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMusicMetadataBinding.inflate(layoutInflater)

        musicMetadataModel = (activity as DemoActivity).musicMetadataModel

        return binding.root
    }

    /**
     * @see [Fragment.onResume]
     */
    override fun onResume() {
        super.onResume()

        musicMetadataModel.addObserver(this)

        binding.findRecordButton.setOnClickListener {

            val recordingName = binding.recordingNameEditText.text?.toString()
            val artistName = binding.artistNameEditText.text?.toString()
            if (!recordingName.isNullOrEmpty() && !artistName.isNullOrEmpty()) {
                musicMetadataModel.requestRecordingMetadataByName(recordingName, artistName)
            }

            log.info(this::onResume.name) {
                "request recording metadata: recordingName=$recordingName artistName=$artistName"
            }
        }

        binding.requestRecordButton.setOnClickListener {

            val recordingId = binding.recordingIdEditText.text?.toString()
            if (!recordingId.isNullOrEmpty()) {
                musicMetadataModel.requestRecordingMetadata(recordingId)
            }

            log.info(this::onResume.name) { "request recording metadata: recordingId=$recordingId" }
        }

        binding.requestAlbumButton.setOnClickListener {

            val albumId = binding.albumIdEditText.text?.toString()
            if (!albumId.isNullOrEmpty()) {
                musicMetadataModel.requestAlbumMetadata(albumId)
            }

            log.info(this::onResume.name) { "request album metadata: albumId=$albumId" }
        }

        binding.requestArtistButton.setOnClickListener {

            val artistId = binding.artistIdEditText.text?.toString()
            if (!artistId.isNullOrEmpty()) {
                musicMetadataModel.requestArtistMetadata(artistId)
            }

            log.info(this::onResume.name) { "request artist metadata: artistId=$artistId" }
        }
    }

    /**
     * @see [Fragment.onPause]
     */
    override fun onPause() {
        super.onPause()

        musicMetadataModel.disposeRequests()
        musicMetadataModel.deleteObserver(this)
    }

    private fun metadataToString(metadata: MusicMetadata?): String {
        return if (metadata == null) {
            ""
        } else if (!metadata.content.isNullOrEmpty()) {
            JSONObject(metadata.content ?: "{}").toString(4)
        } else {
            "Error: ${metadata.error}"
        }
    }
}

