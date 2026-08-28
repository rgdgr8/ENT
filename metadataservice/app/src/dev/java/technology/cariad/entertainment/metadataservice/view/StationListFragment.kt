package technology.cariad.entertainment.metadataservice.view

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import technology.cariad.entertainment.metadataservice.R
import technology.cariad.entertainment.metadataservice.model.StationModel
import technology.cariad.entertainment.metadataservice.view.adapter.StationListAdapter
import java.util.Observable
import java.util.Observer

/**
 * Fragment displaying a list of stations together with controls to select
 * new bearers to request the station data of from a fixed set of bearers.
 */
class StationListFragment : Fragment(), Observer {

    private lateinit var stationLogoRecyclerView: RecyclerView
    private lateinit var stationLogoViewAdapter: RecyclerView.Adapter<StationListAdapter.StationListViewHolder>
    private lateinit var stationLogoViewManager: RecyclerView.LayoutManager

    private lateinit var bearerSpinner: Spinner
    private val bearerList = listOf(
        "dab:1245.d32b.0.262.e0 (94,3 rs2)",
        "dab:1245.1024.0.262.e0 (98.8 KISS FM BERLIN)",
        "dab:1162.dc1a.0.262.e0 (Absolut HOT)",
        "dab:10bc.17fa.0.262.e0 (Absolut relax)",
        "dab:10a3.d519.0.262.e0 (Arabella Kult)",
        "dab:10a5.d315.0.262.e0 (B5 aktuell)",
        "dab:10a5.df15.0.262.e0 (B5 plus)",
        "dab:1162.d511.0.262.e0 (BAYERN 1 Franken)",
        "dab:1162.d711.0.262.e0 (BAYERN 1 Main)",
        "dab:10a5.d811.0.262.e0 (BAYERN 1 Nby/Opf)",
        "dab:1162.d411.0.262.e0 (BAYERN 1 Obb)",
        "dab:1162.d911.0.262.e0 (BAYERN 1 Schw)",
        "dab:1162.df12.0.262.e0 (Bayern 2 Nord)",
        "dab:10a5.d412.0.262.e0 (Bayern 2 Sued)",
        "dab:10a5.d313.0.262.e0 (BAYERN 3)",
        "dab:10a5.d316.0.262.e0 (Bayern+)",
        "dab:1245.d32c.0.262.e0 (Berliner Rundfunk 91.4)",
        "dab:10a5.df16.0.262.e0 (BR Heimat)",
        "dab:10a5.d314.0.262.e0 (BR-KLASSIK)",
        "dab:10bc.d220.0.262.e0 (Deutschlandfunk Kultur)",
        "dab:10bc.d230.0.262.e0 (Deutschlandfunk Nova)",
        "dab:10bc.d210.0.262.e0 (Deutschlandfunk)",
        "dab:10bc.d240.0.262.e0 (Deutschlandradio Dokumente und Debatten)",
        "dab:1162.1014.0.262.e0 (egoFM)",
        "dab:10bc.1a45.0.262.e0 (ENERGY - HIT MUSIC ONLY !)",
        "dab:10bc.1a64.0.262.e0 (ERF Plus)",
        "dab:10a3.d58c.0.262.e0 (HITRADIO RT1 Neuburg Schrobenhausen)",
        "dab:1245.10b2.0.262.e0 (JAM FM Live)",
        "dab:10bc.d75b.0.262.e0 (Klassik Radio Live)",
        "dab:1245.1a2b.0.262.e0 (lulu.fm)",
        "dab:10a3.1613.0.262.e0 (Oldie Welle Ingolstadt)",
        "dab:10a5.d317.0.262.e0 (PULS)",
        "dab:10bc.15dd.0.262.e0 (RADIO BOB!)",
        "dab:10a3.1a14.0.262.e0 (Radio Galaxy Ingolstadt)",
        "dab:10a3.d71b.0.262.e0 (RADIO IN)",
        "dab:10bc.10c3.0.262.e0 (Radio Schlagerparadies)",
        "dab:1162.1b2e.0.262.e0 (Radio TEDDY LIVE - BAYERN)",
        "dab:1245.da3b.0.262.e0 (Schlager Radio B2+ SchlagerMIXX)",
        "dab:10bc.100d.0.262.e0 (Schwarzwaldradio – Classic Hits & Super Oldies)",
        "dab:c18e.cde9.0.262.e1 (Smooth Country)",
        "dab:10bc.15dc.0.262.e0 (sunshine live)",
        "fm:16d1.10370.310.1a0 (80s+ 103.7)",
        "fm:d318.10190.262 (ANTENNE BAYERN)",
        "fm:d431.09970.262 (Antenne Brandenburg)",
        "fm:d78a.10400.262 (Arabella München)",
        "fm:d315.10670.262 (B5 aktuell)",
        "fm:d312.08790.262 (Bayern 2 Sued)",
        "fm:d313.09470.262 (BAYERN 3)",
        "fm:d314.09310.262 (BR-KLASSIK)",
        "fm:d220.10130.262 (Deutschlandfunk Kultur)",
        "fm:d333.09190.262 (Fritz)",
        "fm:1364.09570.262 (harmony.fm)",
        "fm:d368.08810.262 (HIT RADIO FFH)",
        "fm:d361.09430.262 (hr1)",
        "fm:d362.09370.262 (hr2-kultur)",
        "fm:d363.09590.262 (hr3)",
        "fm:d3c2.09150.262 (MDR JUMP)",
        "fm:d3d4.10070.262 (MDR SPUTNIK)",
        "fm:d451.09030.262 (NDR 90,3)",
        "fm:d369.09010.262 (planet radio)",
        "fm:1a14.10790.262 (Radio Galaxy Ingolstadt)",
        "fm:d71b.09910.262 (RADIO IN)",
        "fm:1735.10650.262 (Schlager Radio B2 Mecklenburg-Vorpommern)",
        "fm:d301.08910.262 (SWR1 Baden-Württemberg)",
        "fm:d3a3.09450.262 (SWR3 Elchradio)",
        "fm:d392.09330.262 (WDR 2 - Rhein und Ruhr)",
        "fm:d393.09270.262 (WDR 3)",
        "hd:0ffcc.292.1 (80s+ 103.7)",
        "dab:10a3.131a.0.262.e0 (n/a)",
        "dab:10a3.1612.0.262.e0 (n/a)",
        "dab:11f7.1218.0.262.e0 (n/a)",
        "dab:d390.d392.0.262.e0 (n/a)",
        "dab:d390.d393.0.262.e0 (n/a)",
        "fm:a201.09090.262 (n/a)",
        "fm:d341.09510.262 (n/a)",
        "fm:d382.09110.262 (n/a)",
        "fm:d388.10150.262 (n/a)",
        "fm:d3a5.09290.262 (n/a)",
        "hd:07426.292.2 (n/a)"
    )

    private lateinit var addBearerButton: ImageButton
    private lateinit var batchTestButton: Button
    private lateinit var expireCacheButton: Button

    // Batch test bearers extracted from real radio app log (2026-05-19)
    // Format: internal bearer format dab:<eid>.<sid>.<scids>.<mcc>.<ecc>
    private val batchTestBearers = listOf(
        // German DAB (ecc=e0) — known stations
        "dab:10a5.d313.0.262.e0",  // BAYERN 3
        "dab:10a5.d315.0.262.e0",  // B5 aktuell
        "dab:10a5.d314.0.262.e0",  // BR-KLASSIK
        "dab:10a5.d316.0.262.e0",  // Bayern+
        "dab:10a5.d312.0.262.e0",  // Bayern 2
        "dab:10a5.df16.0.262.e0",  // BR Heimat
        "dab:10bc.d210.0.262.e0",  // Deutschlandfunk
        "dab:10bc.d220.0.262.e0",  // Deutschlandfunk Kultur
        "dab:10bc.d230.0.262.e0",  // Deutschlandfunk Nova
        "dab:11f7.1057.0.262.e0",  // 80s80s
        "dab:11f7.121d.0.262.e0",  // Oldie Antenne
        "dab:11f7.12e9.0.262.e0",  // 90s90s
        "dab:10bc.15dc.0.262.e0",  // sunshine live
        "dab:10bc.15dd.0.262.e0",  // RADIO BOB!
        "dab:10bc.d75b.0.262.e0",  // Klassik Radio
        "dab:1321.1001.0.262.e0",  // Abs Radio
        "dab:1321.1002.0.262.e0",  // Abs Radio 2
        // Austrian DAB (ecc=e3)
        "dab:807c.8721.0.262.e3",
        "dab:807c.841d.0.262.e3",
        "dab:807c.84c8.0.262.e3",
        // UK DAB (ecc=e1)
        "dab:ce15.c221.0.262.e1",
        "dab:ce15.c225.0.262.e1",
        // FM bearers
        "fm:d313.09470.262",       // BAYERN 3
        "fm:d315.09010.262",       // B5 aktuell
        "fm:d3a3.09970.262",       // SWR3
        "fm:f213.09270.262",       // WDR 3
        "fm:d409.09240.262",       // MDR
    )

    private lateinit var stationModel: StationModel

    /**
     * @see [Fragment.onCreateView]]
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_station_list, container, false)

        //hacky, not reusable
        stationModel = (activity as DemoActivity).stationModel

        stationLogoRecyclerView = view.findViewById(R.id.stationRecyclerView)

        stationLogoViewManager = LinearLayoutManager(requireContext())
        stationLogoViewAdapter =
            StationListAdapter(stationModel.getStations(), activity as StationSelectionListener)

        stationLogoRecyclerView.apply {
            layoutManager = stationLogoViewManager
            adapter = stationLogoViewAdapter
        }

        stationLogoRecyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        )

        addBearerButton = view.findViewById(R.id.addBearerButton)
        batchTestButton = view.findViewById(R.id.batchTestButton)
        expireCacheButton = view.findViewById(R.id.expireCacheButton)

        bearerSpinner = view.findViewById(R.id.bearerSpinner)
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bearerList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bearerSpinner.adapter = adapter

        return view
    }

    /**
     * @see [Fragment.onResume]
     */
    override fun onResume() {
        super.onResume()

        stationModel.addObserver(this)
        addBearerButton.setOnClickListener {
            stationModel.addStation(
                bearerList[bearerSpinner.selectedItemPosition].split(' ').first()
            )
        }
        batchTestButton.setOnClickListener {
            var count = 0
            for (bearer in batchTestBearers) {
                stationModel.addStation(bearer)
                count++
            }
            Toast.makeText(requireContext(), "Batch test: $count bearers queued", Toast.LENGTH_SHORT).show()
        }
        expireCacheButton.setOnClickListener {
            try {
                val dbPath = requireContext().filesDir.resolve("database/metadata_cache.db").absolutePath
                val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE)
                val now = System.currentTimeMillis() / 1000
                db.execSQL("UPDATE life_cycle_data SET valid_until = ?", arrayOf(now))
                val cursor = db.rawQuery("SELECT changes()", null)
                val affected = if (cursor.moveToFirst()) cursor.getInt(0) else 0
                cursor.close()
                db.close()
                Toast.makeText(requireContext(), "Cache expired: $affected entries set to now ($now)", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Expire failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        requireActivity().runOnUiThread {
            stationLogoViewAdapter.notifyDataSetChanged()
        }
    }

    /**
     * @see [Fragment.onPause]
     */
    override fun onPause() {
        super.onPause()

        addBearerButton.setOnClickListener(null)
        stationModel.deleteObserver(this)
    }

    /**
     * @see [Observer.update]
     */
    override fun update(o: Observable, bearer: Any?) {
        activity?.runOnUiThread {
            stationLogoViewAdapter.notifyDataSetChanged()
        }
    }
}

