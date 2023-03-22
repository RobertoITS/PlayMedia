package com.hvdevs.playmedia.exoplayer2.leanbackforandroidtv

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.leanback.app.PlaybackSupportFragment
import com.google.android.exoplayer2.ui.PlayerView
import com.hvdevs.playmedia.R
import androidx.leanback.media.PlayerAdapter
import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter
import androidx.leanback.app.PlaybackSupportFragmentGlueHost
import androidx.leanback.widget.ArrayObjectAdapter
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.dash.DashChunkSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.DefaultTrackNameProvider
import com.google.android.exoplayer2.ui.TrackNameProvider
import com.google.android.exoplayer2.ui.TrackSelectionDialogBuilder
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hvdevs.playmedia.mainlist.constructor.ChildModel
import com.hvdevs.playmedia.mainlist.constructor.ParentModel
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList

class LeanbackFragmentPlayer : PlaybackSupportFragment(), Player.Listener,
    VideoPlayerGlue.OnActionClickedListener {
    private var player: SimpleExoPlayer? = null
    private var playerGlue: VideoPlayerGlue? = null
    private var list: ArrayList<ParentModel> = arrayListOf() //La lista principal
    private var childList: ArrayList<ChildModel> = arrayListOf() //La lista con los canales
    private var parentPosition = 0 //La posicion del parent
    private var childCurrentPosition = 0 //La posicion actual del hijo
    private var uri: String? = null
    private var licenceUrl: String? = null
    private val userAgent = "ExoPlayer-Drm"
    private val drmSchemeUuid = C.WIDEVINE_UUID // DRM Type
    private var trackSelector: DefaultTrackSelector? = null //Este es el selector de la calidad del video
    private val formatList: java.util.ArrayList<String> = java.util.ArrayList()
    private val bitrateList: java.util.ArrayList<String> = java.util.ArrayList()

    private fun preparePlayer(
        player: ExoPlayer,
        uri: String?,
        licenceUrl: String?,
        userAgent: String,
        drmSchemeUuid: UUID
    ) {

        val dashMediaSource = if ("m3u8" in uri!!){
            m3u8()
        } else {
            nom3u8()
        }

        // Prepare the player with the source.
        player.setMediaSource(dashMediaSource, true)
        player.prepare()
    }

    private fun m3u8(): MediaSource {
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(requireContext(), "exoplayer-codelab")

        return HlsMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri!!))
    }

    private fun nom3u8(): DashMediaSource {
        val defaultHttpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(userAgent)
            .setTransferListener(
                DefaultBandwidthMeter.Builder(requireContext())
                    .setResetOnNetworkTypeChange(false)
                    .build()
            )

        val dashChunkSourceFactory: DashChunkSource.Factory = DefaultDashChunkSource.Factory(
            defaultHttpDataSourceFactory
        )

        val manifestDataSourceFactory = DefaultHttpDataSource.Factory().setUserAgent(userAgent)

        return DashMediaSource.Factory(dashChunkSourceFactory, manifestDataSourceFactory)
            .createMediaSource(
                MediaItem.Builder()
                    .setUri(Uri.parse(uri))
                    // DRM Configuration
                    .setDrmConfiguration(
                        MediaItem.DrmConfiguration.Builder(drmSchemeUuid)
                            .setLicenseUri(licenceUrl).build()
                    )
                    .setMimeType(MimeTypes.APPLICATION_MPD)
                    .setTag(null)
                    .build()
            )
    }

    private fun initPlayer(
        uri: String?,
        licenceUrl: String?,
        userAgent: String,
        drmSchemeUuid: UUID
    ) {
        //Le pasamos el trackSelector adaptativo...
        trackSelector = DefaultTrackSelector(requireContext()).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }
        val playerView: PlayerView = requireActivity().findViewById(R.id.player)
        player = SimpleExoPlayer.Builder(requireContext()).setTrackSelector(trackSelector!!).build()
        playerView.player = player
        val playerAdapter: PlayerAdapter = LeanbackPlayerAdapter(requireContext(), player!!, 16)
        playerGlue = VideoPlayerGlue(context, playerAdapter, this)
        playerGlue!!.host = PlaybackSupportFragmentGlueHost(this)
        playerGlue!!.playWhenPrepared()
        adapter = ArrayObjectAdapter()
        preparePlayer(player!!, uri, licenceUrl, userAgent, drmSchemeUuid)
    }

    private fun releasePlayer() {
        player?.playWhenReady = false
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            val sp = activity?.getSharedPreferences("channel", Context.MODE_PRIVATE) //Obtenemos las SP
            with(sp!!){
                uri = getString("uri", null)
                licenceUrl = getString("licence", null)
                parentPosition = getInt("parentPosition", 0)
                childCurrentPosition = getInt("childPosition", 0)
            }

            list = getListSP()

            val parentInfo = list[parentPosition]
            childList = parentInfo.itemList //Obtenemos la lista con los canales

            initPlayer(uri, licenceUrl, userAgent, drmSchemeUuid) //Inicializamos el player
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT < 24 || player == null) {
            initPlayer(uri, licenceUrl, userAgent, drmSchemeUuid)
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    //Estas son las fuciones de la interface implementada en el VideoPlayerGlue
    override fun onPrevious() {
        if (childCurrentPosition == 0){ //La unica condicion mas extraña es que el contador llegue a cero
            childCurrentPosition = childList.size - 1
            changeChannel(childCurrentPosition)
        } else { //Caso contrario, le resta 1 a la posicion
            childCurrentPosition -= 1
            changeChannel(childCurrentPosition)
        }
    }

    override fun onNext() {
        if (childCurrentPosition == childList.size - 1){
            childCurrentPosition = 0
            changeChannel(childCurrentPosition)
        } else {
            childCurrentPosition += 1
            changeChannel(childCurrentPosition)
        }
    }

    override fun onAudio() {
        audioTrack()
    }

    override fun onSubtitle() {
        subTrack()
    }

    private fun getListSP(): ArrayList<ParentModel> { //Obtenemos las SP con la lista convertida, para reconstruir
        val sp = activity?.getSharedPreferences("channel", Context.MODE_PRIVATE) //Obtenemos las SP
        val json = sp?.getString("list", null)
        val type: Type = object : TypeToken<ArrayList<ParentModel>>() {}.type
        return Gson().fromJson(json, type)
    }

    private fun changeChannel(childCurrentPosition: Int) { //Cambiamos el canal
        releasePlayer()
        uri = childList[childCurrentPosition].uri
        licenceUrl = childList[childCurrentPosition].drm_license_url
        initPlayer(uri, licenceUrl, userAgent, drmSchemeUuid)
    }


    /** Selector de audio */
    private fun audioTrack(){
        val audioTrack = java.util.ArrayList<String>() //Lista con los audios rescatados de la pista
        val audioList = java.util.ArrayList<String>() //Lista con los NOMBRES de los audios
        for(group in player?.currentTracksInfo?.trackGroupInfos!!){ //Ingresamos a la informacion de la pista
            if(group.trackType == C.TRACK_TYPE_AUDIO){ //Nos fijamos si son pistas de audio
                val groupInfo = group.trackGroup
                for (i in 0 until groupInfo.length){ //Las guardamos en una lista
                    audioTrack.add(groupInfo.getFormat(i).language.toString())
                    //Aqui colocamos los nombres para mostrarlos en el dialog
                    audioList.add("${audioList.size + 1}. " + Locale(groupInfo.getFormat(i).language.toString()).displayLanguage)
                }
            }
        }
        val tempTracks = audioList.toArray(arrayOfNulls<CharSequence>(audioList.size))
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Seleccionar lenguaje: ")
            .setPositiveButton("Sin audio"){ it, _ ->
                trackSelector
                    ?.setParameters(
                        trackSelector!!
                            .buildUponParameters()
                            .setRendererDisabled(
                                C.TRACK_TYPE_AUDIO, true)
                    )
                it.dismiss()
            }
            .setItems(tempTracks) { _, position ->
                Toast.makeText(context, "${audioList[position]} seleccionado", Toast.LENGTH_SHORT)
                    .show()
                trackSelector?.setParameters(
                    trackSelector!!
                        .buildUponParameters()
                        .setRendererDisabled(C.TRACK_TYPE_AUDIO, false)
                        .setPreferredAudioLanguage(audioTrack[position])
                )
            }
            .create()
        builder.show()
    }

    //----------------- Cuadro de dialogo general -----------------//
    private fun audioSubQualityTrackSelector(){
        val items = arrayListOf(
            "Audio",
            "Subtitulos",
            "Calidad"
        )
        val setItems = items.toArray(arrayOfNulls<CharSequence>(items.size))
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Seleccionar: ")
            .setPositiveButton("Salir"){ it, _ ->
                it.dismiss()
            }
            .setItems(setItems) { it, position ->
                when (position){
                    0 -> {
                        audioTrack()
                        it.dismiss()
                    }
                    1 -> {
                        subTrack()
                        it.dismiss()
                    }
                    2 -> {
                        qualityTrack()
                        it.dismiss()
                    }
                    else -> { it.dismiss() }
                }
            }
            .create()
        builder.show()
    }

    //----------------- Cuadro de dialogo general -----------------//

    //Aca se maneja el estado de carga de los videos
    override fun onPlaybackStateChanged(playbackState: @Player.State Int) {
        when (playbackState) {
            Player.STATE_IDLE -> {
                println("IDLE")
            }
            Player.STATE_BUFFERING -> {
                println("BUFFERING")
            }
            Player.STATE_READY -> {
                println("READY")
                trackListDetails() //Al pasar a estar listo, se sacan las calidades de video
            }
            Player.STATE_ENDED -> {
                println("ENDED")
            }
        }
    }

    //Obtenemos las pistas de calidad del video
    private fun trackListDetails() {
        formatList.clear()
        bitrateList.clear()

        if (trackSelector?.currentMappedTrackInfo != null) {
            val mappedTrackInfo = trackSelector!!.currentMappedTrackInfo
            for (rendererIndex in 0 until mappedTrackInfo!!.rendererCount) {

                val trackGroupArray = mappedTrackInfo.getTrackGroups(rendererIndex)

                for (groupIndex in 0 until trackGroupArray.length) {

                    for (trackIndex in 0 until trackGroupArray[groupIndex].length) {
                        val trackNameProvider: TrackNameProvider = DefaultTrackNameProvider(resources)
                        val trackName = trackNameProvider.getTrackName(
                            trackGroupArray[groupIndex].getFormat(trackIndex))
                        val widthHeight = trackName.split(",").toTypedArray()
                        val height = widthHeight[0].split("×").toTypedArray()

                        if (rendererIndex == 0) {
                            formatList.add(height[1] + "p")
                            bitrateList.add(widthHeight[1].replace("Mbps","").trim())
                        }
                    }
                }
            }
        }
    }

    /** Selecto de subtitulos */
    private fun subTrack(){
        val subTrack = java.util.ArrayList<String>() //Lista con los audios rescatados de la pista
        val subList = java.util.ArrayList<String>() //Lista con los NOMBRES de los audios
        for(group in player?.currentTracksInfo?.trackGroupInfos!!){ //Ingresamos a la informacion de la pista
            if(group.trackType == C.TRACK_TYPE_TEXT){ //Nos fijamos si son pistas de audio
                val groupInfo = group.trackGroup
                for (i in 0 until groupInfo.length){ //Las guardamos en una lista
                    subTrack.add(groupInfo.getFormat(i).language.toString())
                    //Aqui colocamos los nombres para mostrarlos en el dialog
                    subList.add("${subList.size + 1}. " + Locale(groupInfo.getFormat(i).language.toString()).displayLanguage)
                }
            }
        }

        val tempTracks = subList.toArray(arrayOfNulls<CharSequence>(subList.size))

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Seleccionar lenguaje: ")
            .setPositiveButton("Sin subtitulo"){ it, _ ->
                trackSelector
                    ?.setParameters(
                        trackSelector!!
                            .buildUponParameters()
                            .setRendererDisabled(
                                C.TRACK_TYPE_TEXT, true)
                    )
                Toast.makeText(context, "Subtitulos desactivados.", Toast.LENGTH_SHORT).show()
                it.dismiss()
            }
            .setItems(tempTracks) { _, position ->
                Toast.makeText(context, "${subList[position]} seleccionado", Toast.LENGTH_SHORT)
                    .show()
                trackSelector?.setParameters(
                    trackSelector!!
                        .buildUponParameters()
                        .setRendererDisabled(C.TRACK_TYPE_TEXT, false)
                        .setPreferredTextLanguage(subTrack[position])
                )
            }
            .create()
        builder.show()
    }

    private fun qualityTrack() { //El problema que teniamos, es que si abriamos el dialogo del
        try{ //selector de pistas, salia un nullPointerException.
            val trackSelector = trackSelector?.let {
                TrackSelectionDialogBuilder( //Con este try, catch, lo manejamos
                    requireContext(),
                    "Seleccione calidad",
                    it,
                    0
                ).build()
            }
            trackSelector?.show()
        } catch (e:NullPointerException){
            Toast.makeText(context, "Cargando...", Toast.LENGTH_SHORT).show()
        }
    }
}