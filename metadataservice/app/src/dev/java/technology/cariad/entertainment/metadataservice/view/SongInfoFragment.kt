package technology.cariad.entertainment.metadataservice.view

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import technology.cariad.entertainment.metadataservice.R
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.databinding.FragmentSongInfoBinding
import technology.cariad.entertainment.metadataservice.model.SongInfoModel
import technology.cariad.entertainment.metadataserviceclient.api.ExternalIdentifier.Companion.ORIGIN_TYPE_AMAZON
import technology.cariad.entertainment.metadataserviceclient.api.ExternalIdentifier.Companion.ORIGIN_TYPE_APPLE
import technology.cariad.entertainment.metadataserviceclient.api.ExternalIdentifier.Companion.ORIGIN_TYPE_SPOTIFY
import technology.cariad.entertainment.metadataserviceclient.api.RecognizedSongInfo
import java.util.Observable
import java.util.Observer

/**
 * Fragment displaying the content of a [RecognizedSongInfo]
 */
class SongInfoFragment : Fragment(), Observer {

    private val log by logger()

    private lateinit var binding: FragmentSongInfoBinding

    private lateinit var songInfoModel: SongInfoModel

    private lateinit var artistTextView: TextView
    private lateinit var albumTextView: TextView
    private lateinit var genreTextView: TextView
    private lateinit var eraTextView: TextView
    private lateinit var yearTextView: TextView
    private lateinit var coverArtUriTextView: TextView
    private lateinit var titleTextView: TextView
    private lateinit var externalIdsTextView: TextView
    private lateinit var coverImageView: ImageView
    private lateinit var moodTextView: TextView
    private lateinit var tempoTextView: TextView
    private lateinit var gracenoteIdTextView: TextView
    private lateinit var gracenoteUniqueIdTextView: TextView
    private lateinit var externalAlbumIdsTextView: TextView
    private var pendingStartAfterPermission = false

    companion object {
        private const val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 1001
    }

    /**
     * @see [Observer.update]
     */
    override fun update(o: Observable?, arg: Any?) {

        val songInfo = songInfoModel.getSongInfo()

        albumTextView.text = songInfo?.album ?: ""
        artistTextView.text = songInfo?.artist ?: ""
        coverArtUriTextView.text = songInfo?.coverartUri ?: ""
        yearTextView.text = songInfo?.year ?: ""
        eraTextView.text = songInfo?.era ?: ""
        genreTextView.text = songInfo?.genre ?: ""
        titleTextView.text = songInfo?.title ?: ""
        moodTextView.text = songInfo?.mood ?: ""
        tempoTextView.text = songInfo?.tempo ?: ""
        val externalIds = songInfo?.externalIds?.map {
            val origin = when (it.originType) {
                ORIGIN_TYPE_AMAZON ->  "amazon_music"
                ORIGIN_TYPE_APPLE ->   "apple"
                ORIGIN_TYPE_SPOTIFY -> "spotify"
                else -> "unknown_origin"
            }
            "$origin:${it.id}\n"
        }?.reduceOrNull { acc, extId ->
            acc + extId
        }
        externalIdsTextView.text = externalIds ?: ""
        val externalAlbumIds = songInfo?.externalAlbumIds?.map {
            val origin = when (it.originType) {
                ORIGIN_TYPE_AMAZON ->  "amazon_music"
                ORIGIN_TYPE_APPLE ->   "apple"
                ORIGIN_TYPE_SPOTIFY -> "spotify"
                else -> "unknown_origin"
            }
            "$origin:${it.id}\n"
        }?.reduceOrNull { acc, extId ->
            acc + extId
        }
        externalAlbumIdsTextView.text = externalAlbumIds ?: ""
        gracenoteIdTextView.text = songInfo?.gracenoteId ?: ""
        gracenoteUniqueIdTextView.text = songInfo?.gracenoteUniqueId ?: ""

        val uriString = songInfo?.coverartUri
        if (!uriString.isNullOrEmpty()) {
            val uri = Uri.parse(uriString)
            coverImageView.setImageURI(uri)
        } else {
            coverImageView.setImageResource(R.drawable.no_image_available)
        }

        if (arg != null) {
            log.info(this::update.name) { "song info is updated" }
        } else {
            binding.startStopButton.isChecked = false

            log.info(this::update.name) { "song info request completed" }
        }
    }

    /*
    * @see [Fragment.onCreateView]
    */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSongInfoBinding.inflate(layoutInflater)

        log.info(this::onCreateView.name) { "songInfo fragment is added to the view" }

        songInfoModel = (activity as DemoActivity).songInfoModel

        albumTextView = binding.albumTextView
        artistTextView = binding.artistTextView
        coverArtUriTextView = binding.coverartUriTextView
        yearTextView = binding.yearTextView
        eraTextView = binding.eraTextView
        genreTextView = binding.genreTextView
        externalIdsTextView = binding.externalIdsTextView
        titleTextView = binding.titleTextView
        coverImageView = binding.coverImageView
        moodTextView = binding.moodTextView
        tempoTextView = binding.tempoTextView
        gracenoteIdTextView = binding.gracenoteIdTextView
        gracenoteUniqueIdTextView = binding.gracenoteUniqueIdTextView
        externalAlbumIdsTextView = binding.externalAlbumIdsTextView

        return binding.root
    }

    /**
     * @see [Fragment.onResume]
     */
    override fun onResume() {
        super.onResume()

        songInfoModel.addObserver(this)
        binding.startStopButton.setOnClickListener {
            if (binding.startStopButton.isChecked) {
                log.info("onClick") { "toggle button is checked - startSongRecognition" }
                if (hasRecordAudioPermission()) {
                    songInfoModel.startSongRecognition()
                } else {
                    pendingStartAfterPermission = true
                    binding.startStopButton.isChecked = false
                    requestPermissions(
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        RECORD_AUDIO_PERMISSION_REQUEST_CODE
                    )
                }
            } else {
                log.info("onClick") { "toggle button is unchecked - stopSongRecognition" }

                songInfoModel.stopSongRecognition()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode != RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
            return
        }

        val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        if (granted && pendingStartAfterPermission) {
            pendingStartAfterPermission = false
            binding.startStopButton.isChecked = true
            songInfoModel.startSongRecognition()
            return
        }

        pendingStartAfterPermission = false
        binding.startStopButton.isChecked = false
    }

    /**
     * @see [Fragment.onPause]
     */
    override fun onPause() {
        super.onPause()

        if (binding.startStopButton.isChecked) {
            songInfoModel.stopSongRecognition()
        }

        songInfoModel.deleteObserver(this)
    }

    private fun hasRecordAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
}
