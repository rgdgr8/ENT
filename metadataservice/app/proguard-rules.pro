-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-dontobfuscate

-keep class de.esolutions.coreservices.csm.service.systemservice.** { *; }
-keep class de.esolutions.magicadapter.stapi.** { *; }

-keep class com.squareup.moshi.kotlin.reflect.** { *; }
-keep class de.esolutions.connectedradioclient.data.** { *; }

-keep class technology.cariad.entertainment.metadataservice.data.provider.GnServiceRequestNr { *; }

-keep class technology.cariad.entertainment.metadataservice.data.provider.gracenote.IGnCoverArtCallback { *; }
-keep class technology.cariad.entertainment.metadataservice.data.provider.gracenote.IGnLyricsCallback { *; }

-keep class technology.cariad.entertainment.metadataservice.data.provider.gracenote.RadioStationInfo { *; }
-keep class technology.cariad.entertainment.metadataservice.data.provider.gracenote.ExternalId { *; }
-keep class technology.cariad.entertainment.metadataservice.data.provider.gracenote.RadioStationInfo { *; }
-keep class technology.cariad.entertainment.metadataservice.data.provider.gracenote.TrackInfo { *; }
-keep class technology.cariad.entertainment.metadataservice.data.provider.gracenote.AlbumInfo { *; }

