package technology.cariad.entertainment.metadataservice.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.databinding.FragmentPermissionInfoBinding
import technology.cariad.entertainment.metadataservice.model.PermissionDataModel
import technology.cariad.entertainment.metadataserviceclient.api.PermissionData
import java.util.Observable
import java.util.Observer

class PermissionDataFragment : Fragment(), Observer {

    private val log by logger()

    private lateinit var permissionDataModel: PermissionDataModel

    private lateinit var refreshButton: Button

    private lateinit var binding: FragmentPermissionInfoBinding

    /**
     * @see [Observer.update]
     */
    override fun update(o: Observable?, arg: Any?) {

        val permissionData = permissionDataModel.getValue()
        val missingPermissionList = permissionData?.missingPermissions

        log.info(this::update.name) {
            "missing permissions: ${missingPermissionList?.joinToString { "$it" }}"
        }

        binding.missingPermissionTextView.text = if (missingPermissionList.isNullOrEmpty()) {
            "no permissions missing"
        } else {
            missingPermissionList.joinToString(separator = "\n") {
                when (it) {
                    PermissionData.PERMISSION_RECORD_AUDIO ->
                        "PERMISSION_RECORD_AUDIO ($it)"
                    PermissionData.PERMISSION_FINE_BACKGROUND_LOCATION ->
                        "PERMISSION_FINE_BACKGROUND_LOCATION ($it)"
                    else -> "unknown permission ($it)"
                }
            }
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
        binding = FragmentPermissionInfoBinding.inflate(layoutInflater)

        log.info(this::onCreateView.name) { "permission information fragment is added to the view" }

        permissionDataModel = (activity as DemoActivity).permissionDataModel

        refreshButton = binding.refreshButton

        return binding.root
    }

    /**
     * @see [Fragment.onResume]
     */
    override fun onResume() {
        super.onResume()

        permissionDataModel.addObserver(this)
        refreshButton.setOnClickListener {
            log.info(this::update.name) { "Refresh permission information.." }
            permissionDataModel.getPermissionInformation()
        }
    }

    /**
     * @see [Fragment.onPause]
     */
    override fun onPause() {
        super.onPause()

        permissionDataModel.disposePermissionDataRequest()
        permissionDataModel.deleteObserver(this)
    }
}
