package com.hvdevs.playmedia.exoplayer2.leanbackforandroidtv

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.leanback.media.PlayerAdapter
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.widget.Action
import androidx.leanback.widget.PlaybackControlsRow.RepeatAction
import androidx.leanback.widget.PlaybackControlsRow.ThumbsUpAction
import androidx.leanback.widget.PlaybackControlsRow.ThumbsDownAction
import androidx.leanback.widget.PlaybackControlsRow.SkipPreviousAction
import androidx.leanback.widget.PlaybackControlsRow.SkipNextAction
import androidx.leanback.widget.PlaybackControlsRow.FastForwardAction
import androidx.leanback.widget.PlaybackControlsRow.RewindAction
import androidx.leanback.widget.PlaybackControlsRow.ClosedCaptioningAction
import androidx.leanback.widget.PlaybackControlsRow.ShuffleAction
import androidx.leanback.widget.PlaybackControlsRow.PictureInPictureAction
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.PlaybackControlsRow
import com.hvdevs.playmedia.R
import java.util.concurrent.TimeUnit

/**
 * Manages customizing the actions in the [PlaybackControlsRow]. Adds and manages the
 * following actions to the primary and secondary controls:
 *
 *
 *  * [androidx.leanback.widget.PlaybackControlsRow.RepeatAction]
 *  * [androidx.leanback.widget.PlaybackControlsRow.ThumbsDownAction]
 *  * [androidx.leanback.widget.PlaybackControlsRow.ThumbsUpAction]
 *  * [androidx.leanback.widget.PlaybackControlsRow.SkipPreviousAction]
 *  * [androidx.leanback.widget.PlaybackControlsRow.SkipNextAction]
 *  * [androidx.leanback.widget.PlaybackControlsRow.FastForwardAction]
 *  * [androidx.leanback.widget.PlaybackControlsRow.RewindAction]
 *
 *
 * Note that the superclass, [PlaybackTransportControlGlue], manages the playback controls
 * row.
 */
class VideoPlayerGlue(
    context: Context?,
    playerAdapter: PlayerAdapter?,
    private val mActionListener: OnActionClickedListener
) : PlaybackTransportControlGlue<PlayerAdapter?>(context, playerAdapter) {
    /** Listens for when skip to next and previous actions have been dispatched.  */
    interface OnActionClickedListener {
        /** Skip to the previous item in the queue.  */
        fun onPrevious()

        /** Skip to the next item in the queue.  */
        fun onNext()

        fun onSettings()
    }

    private val mRepeatAction: RepeatAction
    private val mThumbsUpAction: ThumbsUpAction = ThumbsUpAction(context)
    private val mThumbsDownAction: ThumbsDownAction
    private val mSkipPreviousAction: SkipPreviousAction = SkipPreviousAction(context)
    private val mSkipNextAction: SkipNextAction = SkipNextAction(context)
//    private val mFastForwardAction: FastForwardAction = FastForwardAction(context)
//    private val mRewindAction: RewindAction = RewindAction(context)
    private val mClosedCaptionAction: ClosedCaptioningAction
    private val mShuffleAction: ShuffleAction
    private val mPictureAction: PictureInPictureAction
    override fun onCreatePrimaryActions(adapter: ArrayObjectAdapter) {
        super.onCreatePrimaryActions(adapter)
//        adapter.add(mRewindAction)
        adapter.add(mSkipPreviousAction)
        adapter.add(mSkipNextAction)
//        adapter.add(mFastForwardAction)
    }

    override fun onCreateSecondaryActions(adapter: ArrayObjectAdapter) {
        super.onCreateSecondaryActions(adapter)

//        adapter.add(mThumbsDownAction);
//        adapter.add(mThumbsUpAction);
//        adapter.add(mShuffleAction);
//        adapter.add(mClosedCaptionAction)
//        adapter.add(mPictureAction)
        //Botones secundarios, le agregamos los iconos
//        mPictureAction.icon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_settings_24)
    }

    //Aca se manejan las acciones aparentemente
    override fun onActionClicked(action: Action) {
        if (shouldDispatchAction(action)) {
//            if (action === mFastForwardAction) {
//                playerAdapter!!.seekTo(playerAdapter!!.currentPosition + 60000)
//            }

            if (action === mPictureAction){
                mActionListener.onSettings()
            }
            return
        }
        // Super class handles play/pause and delegates to abstract methods next()/previous().
        super.onActionClicked(action)
    }

    // Should dispatch actions that the super class does not supply callbacks for.
    private fun shouldDispatchAction(action: Action): Boolean {
        return /*action === mRewindAction || action === mFastForwardAction || */action === mThumbsDownAction || action === mThumbsUpAction || action === mRepeatAction || action === mShuffleAction || action === mClosedCaptionAction || action === mPictureAction
    }

    override fun next() {
        mActionListener.onNext()
    }

    override fun previous() {
        mActionListener.onPrevious()
    }

    companion object {
        private val TEN_SECONDS = TimeUnit.SECONDS.toMillis(10)
    }

    init {
        //El panel de botones, aqui poner accion
        mThumbsUpAction.index = ThumbsUpAction.INDEX_OUTLINE
        mThumbsDownAction = ThumbsDownAction(context)
        mThumbsDownAction.index = ThumbsDownAction.INDEX_OUTLINE
        mRepeatAction = RepeatAction(context)
        mClosedCaptionAction = ClosedCaptioningAction(context)
        mShuffleAction = ShuffleAction(context)
        mPictureAction = PictureInPictureAction(context)
    }
}