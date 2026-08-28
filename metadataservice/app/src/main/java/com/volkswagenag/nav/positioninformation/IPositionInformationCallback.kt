// LOCAL BUILD STUB: IPositionInformationCallback
package com.volkswagenag.nav.positioninformation

/**
 * Stub for IPositionInformationCallback from navigation AIDL.
 * This is a placeholder for local builds without navigation AIDL files.
 */
interface IPositionInformationCallback {
    fun onGeneralPositionInformation(generalPositionInformation: GeneralPositionInformationType?)
    fun onRoadInformation(isOnRoad: Boolean, roadInformation: RoadInformationType?)
    fun onError(error: Int)

    /**
     * Stub base class providing default implementations
     */
    abstract class Stub : IPositionInformationCallback {
        override fun onGeneralPositionInformation(generalPositionInformation: GeneralPositionInformationType?) {}
        override fun onRoadInformation(isOnRoad: Boolean, roadInformation: RoadInformationType?) {}
        override fun onError(error: Int) {}
    }
}
