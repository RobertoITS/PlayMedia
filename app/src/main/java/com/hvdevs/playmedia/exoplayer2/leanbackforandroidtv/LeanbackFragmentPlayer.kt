package com.hvdevs.playmedia.exoplayer2.leanbackforandroidtv

import android.net.Uri
import android.widget.Toast
import androidx.leanback.app.PlaybackSupportFragment
import com.google.android.exoplayer2.ui.PlayerView
import com.hvdevs.playmedia.R
import androidx.leanback.media.PlayerAdapter
import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter
import androidx.leanback.app.PlaybackSupportFragmentGlueHost
import androidx.leanback.widget.ArrayObjectAdapter
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.dash.DashChunkSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util

class LeanbackFragmentPlayer : PlaybackSupportFragment(), Player.Listener,
    VideoPlayerGlue.OnActionClickedListener {
    private var player: SimpleExoPlayer? = null
    private var playerGlue: VideoPlayerGlue? = null

    private fun preparePlayer(player: ExoPlayer) {

        //Obtenemos el shared preferences
        val sp = activity?.getSharedPreferences("videoData", 0)

        val uri = sp?.getString("uri", "")
        val licenceUrl = sp?.getString("licence", "")

        val userAgent = "ExoPlayer-Drm"
        val drmSchemeUuid = C.WIDEVINE_UUID // DRM Type

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

        val dashMediaSource =
            DashMediaSource.Factory(dashChunkSourceFactory, manifestDataSourceFactory)
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

        // Prepare the player with the source.
        player.setMediaSource(dashMediaSource, true)
        player.prepare()
    }

    private fun initPlayer() {
        val playerView: PlayerView = requireActivity().findViewById(R.id.player)
        player = SimpleExoPlayer.Builder(requireContext()).build()
        playerView.player = player
        val playerAdapter: PlayerAdapter = LeanbackPlayerAdapter(requireContext(), player!!, 16)
        playerGlue = VideoPlayerGlue(context, playerAdapter, this)
        playerGlue!!.host = PlaybackSupportFragmentGlueHost(this)
        playerGlue!!.playWhenPrepared()
        adapter = ArrayObjectAdapter()
        preparePlayer(player!!)
    }

    private fun releasePlayer() {
        if (player != null) {
            player!!.release()
            player = null
        }
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initPlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT < 24 || player == null) {
            initPlayer()
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
        Toast.makeText(context, "Probando botones y su accion", Toast.LENGTH_SHORT).show()
    }
    override fun onNext() {
        Toast.makeText(context, "Probando botones y su accion", Toast.LENGTH_SHORT).show()
    }
}