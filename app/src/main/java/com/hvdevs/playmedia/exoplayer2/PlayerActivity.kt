package com.hvdevs.playmedia.exoplayer2

import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.google.ads.interactivemedia.v3.api.*
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.decoder.DecoderReuseEvaluation
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.dash.DashChunkSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.DefaultTrackNameProvider
import com.google.android.exoplayer2.ui.TrackNameProvider
import com.google.android.exoplayer2.ui.TrackSelectionDialogBuilder
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.video.VideoSize
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hvdevs.playmedia.R.id
import com.hvdevs.playmedia.databinding.ActivityPlayerBinding
import com.hvdevs.playmedia.mainlist.constructor.ChildModel
import com.hvdevs.playmedia.mainlist.constructor.ParentModel
import com.hvdevs.playmedia.utilities.WindowSystemUtilities
import com.hvdevs.playmedia.utilities.WindowSystemUtilities.hideSystemUI
import java.lang.reflect.Type
import java.util.*
import kotlin.properties.Delegates


class PlayerActivity : Activity(), Player.Listener, AnalyticsListener, AdEvent.AdEventListener,
    AdErrorEvent.AdErrorListener, AdsLoader.AdsLoadedListener {

    private lateinit var binding : ActivityPlayerBinding

    private var adsLoader: ImaAdsLoader? = null
    private var adsManager: AdsManager? = null

    private val formatList: ArrayList<String> = ArrayList()
    private val bitrateList: ArrayList<String> = ArrayList()

    private lateinit var drm: String //Variable que obtiene el drm que se pasa de la actividad anterior
    private lateinit var uri: String //Variable que obtiene la uri que se pasa de la actividad anterior
    private var time by Delegates.notNull<Long>() //Variable que obtiene el tiempo que se pasa de la actividad anterior
    private var testContent by Delegates.notNull<Boolean>() //Variable que obtiene el tipo de contenido que se pasa de la actividad anterior
    private lateinit var uid: String //Variable que obtiene la uid que se pasa de la actividad anterior
    private lateinit var streamUrl: String //Variable para pasar los datos al reproductor
    private lateinit var licenceUrl: String //Variable para pasar los datos al reproductor
    private var list: ArrayList<ParentModel> = arrayListOf()
    private var childList: ArrayList<ChildModel> = arrayListOf()
    private var childCurrentPosition = 0
    private var parentPosition = 0

    //---------------------------------------------------------------------------------//
    private lateinit var playerView: ExoPlayer
    private lateinit var trackSelector: DefaultTrackSelector
    //---------------------------------------------------------------------------------//

    private lateinit var countDownTimer: CountDownTimer //El contador del reproductor, en caso de modo de prueba
    var total: Long = 0

    private val database = Firebase.database //Instancias a la base de datos
    val myRef = database.getReference("users")

    private val waitUI = Handler() //Instanciamos el handler para el tiempo de espera

    private var isLandScape = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sp = getSharedPreferences("channel", 0)
        val bundle: Bundle? = intent.extras
        with(sp!!){
            uri = getString("uri", null).toString()
            drm = getString("licence", null).toString()
            parentPosition = getInt("parentPosition", 0)
            childCurrentPosition = getInt("childPosition", 0)
        }

        uid = bundle?.getString("uid").toString()
        time = bundle!!.getLong("time")
        Log.d("TIMETEST", time.toString())
        testContent = bundle.getBoolean("testContent")

        list = getListSP()
        val parentInfo = list[parentPosition]
        childList = parentInfo.itemList //Conseguimos la lista de los child


        streamUrl = uri
        Log.d("DRM", drm)
        Log.d("URL", uri)
        licenceUrl = drm


        adsLoader = ImaAdsLoader.Builder(this)
            .setAdEventListener(this)
            .setAdErrorListener(this)
            .build()

        binding.detailBtn.setOnClickListener { showDialog() }

        //Instanciamos las vistas que estan llamadas desde el archivo layout donde se
        //encuentra el exoPlayerView: app:controller_layout_id="@layout/custom_controls"

        //Boton adelantar
        val forwardBtn = binding.exoPlayerView.findViewById<ImageView>(id.fwd)
        forwardBtn.setOnClickListener {
//            simpleExoPlayer!!.seekTo(
//                simpleExoPlayer!!.currentPosition + 10000
//            )
            if (childCurrentPosition == childList.size - 1){
                childCurrentPosition = 0
                changeChannel(childCurrentPosition)
            } else {
                childCurrentPosition += 1
                changeChannel(childCurrentPosition)
            }
        }

        //Boton rebobinar
        val rewBtn = binding.exoPlayerView.findViewById<ImageView>(id.rew)
        rewBtn.setOnClickListener {
//            val num = simpleExoPlayer!!.currentPosition - 10000
//            if (num < 0) {
//                simpleExoPlayer!!.seekTo(0)
//            } else {
//                simpleExoPlayer!!.seekTo(simpleExoPlayer!!.currentPosition - 10000)
//            }
            if (childCurrentPosition == 0){ //La unica condicion mas extraña es que el contador llegue a cero
                childCurrentPosition = childList.size - 1
                changeChannel(childCurrentPosition)
            } else { //Caso contrario, le resta 1 a la posicion
                childCurrentPosition -= 1
                changeChannel(childCurrentPosition)
            }
        }

        //Fullscreen
        val fullscreenButton = binding.exoPlayerView.findViewById<ImageView>(id.fullscreen)
        fullscreenButton.setOnClickListener {
            val orientation: Int =
                this.resources.configuration.orientation
            requestedOrientation = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                // code for portrait mode
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                // code for landscape mode
                Toast.makeText(this, "Land", Toast.LENGTH_SHORT).show()
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }

        //Settings button
        val setting = binding.exoPlayerView.findViewById<ImageView>(id.exo_track_selection_view)
        setting.setOnClickListener {
            showDialog()
        }

        //Audio selector
        val audio = binding.exoPlayerView.findViewById<ImageView>(id.exo_audio_selection_view)
        audio.setOnClickListener {
            audioTrack()
        }

        //Back button
        val back = binding.exoPlayerView.findViewById<ImageView>(id.exo_back)
        back.setOnClickListener {
            playerView.release()
            finish()
        }
        //---------------------------------------------------------------------------------//

        //Captamos el cambio de visibilidad del UI del sistema
        //Le pasamos un handler para que se oculte a los 3 segundos de espera
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0 && isLandScape){
                waitUI.postDelayed(wait, 3000)
            }
        }
    }

    //Vemos el estado de configuracion en la rotacion de la pantalla
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val root = binding.exoPlayerView.findViewById<ConstraintLayout>(id.root)
        // Tomamos la variable boolean para saber si esta o no en landscape
        isLandScape = WindowSystemUtilities.checkOrientation(newConfig, window, root)
    }

    //Creamos la variable de tipo runnable para parar el tiempo,
    //con la funcionalidad a realizarse
    private val wait = Runnable {
        hideSystemUI(window) //Escondemos la UI del sistema
    }

    //Escondemos la barra de estado y de navegacion
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        //Si esta en landscape
        if (hasFocus && isLandScape) hideSystemUI(window) //Escondemos la UI del sistema
    }

    //Inicializamos es reproductor
    //Las demas funciones viene predeterminadas en el repositorio clonado
    private fun initializePlayer() {
        val userAgent = "ExoPlayer-Drm"
        val drmSchemeUuid: UUID = C.WIDEVINE_UUID // DRM Type

        val dashMediaSource = if ("m3u8" in uri){
            m3u8()
        } else {
            nom3u8(userAgent, drmSchemeUuid)
        }

        val handler = Handler()
        val adaptiveTrackSelection = AdaptiveTrackSelection.Factory()
        trackSelector = DefaultTrackSelector(this, adaptiveTrackSelection)
        //trackSelector.buildUponParameters().setRendererDisabled(C.TRACK_TYPE_TEXT, false).build()

        val bandwidthMeter = DefaultBandwidthMeter.Builder(this).build()
        bandwidthMeter.addEventListener(handler) { elapsedMs, bytesTransferred, _ ->
            binding.textView.text = (((bytesTransferred * 8).toDouble() / (elapsedMs / 1000)) / 1000).toString()
        }

        //Usamos el exoplayer
        playerView = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .setBandwidthMeter(bandwidthMeter)
            .setSeekForwardIncrementMs(10000)
            .setSeekBackIncrementMs(10000)
            .build()

        val mEventLogger = EventLogger(trackSelector)
        playerView.addAnalyticsListener(mEventLogger)

        playerView.playWhenReady = true
        binding.exoPlayerView.player = playerView
        playerView.setMediaSource(dashMediaSource, true)
        playerView.prepare()
    }

    private fun showDialog() { //El problema que teniamos, es que si abriamos el dialogo del
        try{ //selector de pistas, salia un nullPointerException.
            val trackSelector = TrackSelectionDialogBuilder( //Con este try, catch, lo manejamos
                this,
                "Seleccione calidad",
                trackSelector,
                0
            ).build()
            trackSelector.show()
        } catch (e:NullPointerException){
            Toast.makeText(this, "Cargando...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun trackListDetails() {
        formatList.clear()
        bitrateList.clear()

        if (trackSelector.currentMappedTrackInfo != null) {
            val mappedTrackInfo = trackSelector.currentMappedTrackInfo
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

    override fun onAdEvent(p0: AdEvent?) {
        Log.e("AD_POD_POS", p0?.ad?.adPodInfo?.adPosition.toString())
        Log.e("AD_POD_TOTAL", p0?.ad?.adPodInfo?.totalAds.toString())
        Log.e("AD_POD_INDEX", p0?.ad?.adPodInfo?.podIndex.toString())

    }

    override fun onAdError(p0: AdErrorEvent?) {
        Log.e("AD_ERROR", Gson().toJson(p0))
    }

    override fun onAdsManagerLoaded(p0: AdsManagerLoadedEvent) {
        adsManager = p0.adsManager
        Log.e("CUE_POINTS", adsManager?.adCuePoints.toString())
    }

    override fun onPlaybackStateChanged(playbackState: @Player.State Int) {
        when (playbackState) {
            Player.STATE_IDLE -> {
                println("IDLE")
            }
            Player.STATE_BUFFERING -> {
                println("BUFFERING")
                binding.progressBar.isVisible = true
            }
            Player.STATE_READY -> {
                println("READY")
                binding.progressBar.isGone = true
                //getTrackDetails()
                trackListDetails()
                //audioTrack()
            }
            Player.STATE_ENDED -> {
                println("ENDED")
            }
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        Log.e("PLAYBACK_ERROR_MESSAGE", error.toString())
        Log.e("PLAYBACK_ERROR_CAUSE", error.cause.toString())
        Log.e("PLAYBACK_ERROR_CNAME", error.errorCodeName)
        Log.e("PLAYBACK_ERROR_CODE", error.errorCode.toString())
        Log.e("ERROR_STACKTRACE", error.stackTraceToString())
        Log.e("ERROR_SUPP_EXCEPTIONS", error.suppressedExceptions.toString())
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        if (timeline.isEmpty) {
            return
        }
        val period = timeline.getPeriod(playerView.currentPeriodIndex, Timeline.Period())
        val adGroupTimesUs = LongArray(period.adGroupCount)
        for (i in adGroupTimesUs.indices) {
            adGroupTimesUs[i] = period.getAdGroupTimeUs(i)
        }
        Log.e("AD_GROUPS", Gson().toJson(adGroupTimesUs))
    }

    override fun onVideoInputFormatChanged(
        eventTime: AnalyticsListener.EventTime,
        format: Format,
        decoderReuseEvaluation: DecoderReuseEvaluation?) {
        super.onVideoInputFormatChanged(eventTime, format, decoderReuseEvaluation)

        val trackNameProvider: TrackNameProvider = DefaultTrackNameProvider(resources)
        Log.e("VIDEO_BITRATE", trackNameProvider.getTrackName(format))
        Log.e("TRACK_INDEX", format.id.toString())
        println(Gson().toJson(eventTime))
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        Log.e("Video Height: ", videoSize.height.toString())
        Log.e("Video Width: ", videoSize.width.toString())
    }

    private fun releasePlayer() {
        /***/
        playerView.playWhenReady = false
    }

    //Cuando se construye la vista, se iniciliza el exoPlayer
    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23)
        //Pasamos el comprobador de tiempo de prueba, si es true,
        //comienza el contador a ir hacia atras
            if (testContent){
                Log.d("TIMETEST", time.toString())
                countDownTimer = object : CountDownTimer(time, 1000) {
                    @SuppressLint("SetTextI18n")
                    override fun onTick(millisUntilFinished: Long) {
                        //Lo pasamos a formato hora
                        val seconds = (millisUntilFinished / 1000).toInt() % 60
                        val minutes = (millisUntilFinished / (1000 * 60) % 60).toInt()
                        val hours = (millisUntilFinished / (1000 * 60 * 60) % 24).toInt()
                        val newtime = "$hours:$minutes:$seconds"
                        total = millisUntilFinished / 1000
                        Log.d("TIMETEST", millisUntilFinished.toString())
                        binding.testContent.visibility = View.VISIBLE
                        binding.testContent.text = "seconds remaining: $newtime"
                        //Vamos refrescando la hora en la base de datos en tiempo real
                        myRef.child("$uid/time").setValue(millisUntilFinished)
                    }

                    @SuppressLint("SetTextI18n")
                    override fun onFinish() {
                        Toast.makeText(this@PlayerActivity, "Su tiempo de prueba expiro", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }.start()
                initializePlayer()
            } else {
                initializePlayer()
            }
    }

    //Cuando se resume la vista, se reproduce el exoPlayer
    public override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23)
            playerView.playWhenReady = true
    }

    //En estado de pausa de la vista, el exoPlayer queda en estado de pausa
    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) releasePlayer()
    }

    //En estado de stop de la vista, el exoPlayer queda en estado de pausa
    public override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) releasePlayer()
    }

    //Cuando se destruye la vista, se destruye el reproductor exoPlayer
    override fun onDestroy() {
        super.onDestroy()
        adsLoader?.release()
        //Se libera el reproductor
        playerView.release()
        Log.e("ADS_DESTROYED", "X")
        if (testContent) countDownTimer.cancel()
    }

    override fun onBackPressed() {
        if (testContent) countDownTimer.cancel()
        super.onBackPressed()
    }

    companion object {
        const val SUBS = "https://bitdash-a.akamaihd.net/content/sintel/hls/subtitles_en.vtt"
    }

    private fun m3u8(): MediaSource {
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(this, "exoplayer-codelab")

        return HlsMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri))
    }

    private fun nom3u8(userAgent: String, drmSchemeUuid: UUID): DashMediaSource {
        val defaultHttpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(userAgent)
            .setTransferListener(
                DefaultBandwidthMeter.Builder(this)
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

    private fun getListSP(): ArrayList<ParentModel> { //Obtenemos las SP con la lista convertida, para reconstruir
        val sp = getSharedPreferences("channel", Context.MODE_PRIVATE) //Obtenemos las SP
        val json = sp?.getString("list", null)
        val type: Type = object : TypeToken<ArrayList<ParentModel>>() {}.type
        return Gson().fromJson(json, type)
    }

    private fun changeChannel(childCurrentPosition: Int) { //Cambiamos el canal
        releasePlayer()
        uri = childList[childCurrentPosition].uri
        licenceUrl = childList[childCurrentPosition].drm_license_url
        initializePlayer()
    }

    private fun audioTrack(){
        val audioTrack = ArrayList<String>() //Lista con los audios rescatados de la pista
        val audioList = ArrayList<String>() //Lista con los NOMBRES de los audios
        for(group in playerView.currentTracksInfo.trackGroupInfos){ //Ingresamos a la informacion de la pista
            if(group.trackType == C.TRACK_TYPE_AUDIO){ //Nos fijamos si son pistas de audio
                val groupInfo = group.trackGroup
                for (i in 0 until groupInfo.length){ //Las guardamos en una lista
                    audioTrack.add(groupInfo.getFormat(i).language.toString())
                    //Aqui colocamos los nombres para mostrarlos en el dialog
                    audioList.add("${audioList.size + 1}. " + Locale(groupInfo.getFormat(i).language.toString()).displayLanguage)
                }
            }
        }
        //if(audioList[0].contains("null")) audioList[0] = "1. Default Track"
        val tempTracks = audioList.toArray(arrayOfNulls<CharSequence>(audioList.size))
        //val context: Context = ContextThemeWrapper(this, com.hvdevs.playmedia.R.style.AppTheme2)
//        val audioDialog = MaterialAlertDialogBuilder(context).setTitle("Lenguaje")
//            .setPositiveButton("Off audio"){ self, _ ->
//                trackSelector.setParameters(trackSelector.buildUponParameters().setRendererDisabled(
//                    C.TRACK_TYPE_AUDIO, true
//                ))
//                self.dismiss()
//            }
//            .setItems(tempTracks){_,position ->
//                Toast.makeText(this, audioList[position] + "Selected", Toast.LENGTH_SHORT).show()
//                trackSelector.setParameters(trackSelector.buildUponParameters()
//                    .setRendererDisabled(C.TRACK_TYPE_AUDIO, false)
//                    .setPreferredAudioLanguage(audioTrack[position])
//                )
//            }
//            .create()
//        audioDialog.show()
//        audioDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
//        audioDialog.window?.setBackgroundDrawable(ColorDrawable(0x99000000.toInt()))
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Seleccionar lenguaje: ")
            .setPositiveButton("Sin audio"){ it, _ ->
                trackSelector
                    .setParameters(
                        trackSelector
                            .buildUponParameters()
                            .setRendererDisabled(
                                C.TRACK_TYPE_AUDIO, true)
                    )
                it.dismiss()
            }
            .setItems(tempTracks) { _, position ->
                Toast.makeText(this, "${audioList[position]} seleccionado", Toast.LENGTH_SHORT)
                    .show()
                trackSelector.setParameters(
                    trackSelector
                        .buildUponParameters()
                        .setRendererDisabled(C.TRACK_TYPE_AUDIO, false)
                        .setPreferredAudioLanguage(audioTrack[position])
                )
            }
            .create()
        builder.show()
    }
}