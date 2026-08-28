package technology.cariad.entertainment.metadataservice.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.databinding.FragmentSongLyricsBinding
import technology.cariad.entertainment.metadataservice.model.SongLyricsModel
import technology.cariad.entertainment.metadataserviceclient.api.SongLyrics
import org.json.JSONObject
import java.util.Observable
import java.util.Observer

/**
 * Fragment displaying the content of a [SongLyrics] object.
 */
class SongLyricsFragment : Fragment(), Observer {

    private val log by logger()

    private lateinit var binding: FragmentSongLyricsBinding
    private lateinit var songLyricsModel: SongLyricsModel

    private lateinit var lyricsTextView: TextView
    private lateinit var playPositionTextView: TextView

    private lateinit var lyricsTypeSpinner: Spinner
    private lateinit var familyFriendlySwitch: Switch

    /**
     * @see [Observer.update]
     */
    override fun update(o: Observable?, arg: Any?) {

        val songLyrics = songLyricsModel.getSongLyrics()
        val lyricsJson = songLyrics?.lyricsJson

        if (lyricsJson.isNullOrEmpty()) {
            lyricsTextView.text = "no lyrics available"
        } else {
            lyricsTextView.text = JSONObject(lyricsJson).toString(4)
        }

        playPositionTextView.text = "${songLyrics?.playPositionMs}"

        log.info(this::update.name) { "updated lyrics" }

        if (songLyrics?.lyricsJson == null) {
            binding.startStopButton.isChecked = false

            log.info(this::update.name) { "lyrics request completed" }
        }
    }

    /*
    * @see [Fragment.onCreateView]
    */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSongLyricsBinding.inflate(layoutInflater)

        log.info(this::onCreateView.name) { "add song lyrics fragment to the view" }

        songLyricsModel = (activity as DemoActivity).songLyricsModel

        lyricsTypeSpinner = binding.lyricsTypeSpinner
        familyFriendlySwitch = binding.familyFriendlySwitch

        lyricsTextView = binding.lyricsTextView
        playPositionTextView = binding.playPositionTextView

        return binding.root
    }

    /**
     * @see [Fragment.onResume]
     */
    override fun onResume() {
        super.onResume()

        songLyricsModel.addObserver(this)
        binding.startStopButton.setOnClickListener {
            if (binding.startStopButton.isChecked) {
                log.info("onClick") { "start lyrics request" }

                val familyFriendly = familyFriendlySwitch.isChecked
                val lyricsType = lyricsTypeSpinner.selectedItemPosition

                songLyricsModel.startSongLyrics(lyricsType, familyFriendly)
            } else {
                log.info("onClick") { "stop lyrics request" }

                songLyricsModel.stopSongLyrics()
            }
        }
    }

    /**
     * @see [Fragment.onPause]
     */
    override fun onPause() {
        super.onPause()

        if (binding.startStopButton.isChecked) {
            songLyricsModel.stopSongLyrics()
        }

        songLyricsModel.deleteObserver(this)
    }
}
