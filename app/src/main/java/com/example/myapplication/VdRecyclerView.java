package com.example.myapplication;


import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.RequestManager;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;

import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;

public class VdRecyclerView extends RecyclerView {
    int targetPosition;
    private SimpleExoPlayer videoPlayer;
    private static final String TAG = "VideoPlayerRecyclerView";
    BandwidthMeter bandwidthMeter;
    LoadControl loadControl;
    private enum VolumeState {ON, OFF};
    private ImageView thumbnail, volumeControl;
    private ProgressBar progressBar;
    private View viewHolderParent;
    private FrameLayout frameLayout;
    private long a=0,b=0,c=0,d=0,e=0,f=0;
    private PlayerView videoSurfaceView;
    // vars
    private ArrayList<MediaObject> mediaObjects = new ArrayList<>();
    private int videoSurfaceDefaultHeight = 0;
    private int screenDefaultHeight = 0;
    private Context context;
    private int playPosition = -1;
    private boolean isVideoViewAdded;
    private RequestManager requestManager;
    private VolumeState volumeState;
    private long m=0;
    public VdRecyclerView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public VdRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    private void init(Context context){
        this.context = context.getApplicationContext();
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        videoSurfaceDefaultHeight = point.x;
        screenDefaultHeight = point.y;

        videoSurfaceView = new PlayerView(this.context);
        videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
        bandwidthMeter=new DefaultBandwidthMeter();
       // BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. Create the player

        videoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        //videoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        // Bind the player to the view.
        videoSurfaceView.setUseController(false);
        videoSurfaceView.setPlayer(videoPlayer);
        setVolumeControl(VolumeState.ON);
        addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Log.d(TAG, "onScrollStateChanged: called.");
                    if(thumbnail != null){ // show the old thumbnail
                        thumbnail.setVisibility(VISIBLE);
                        if(m==0){
                        if(targetPosition==0){
                            a=videoPlayer.getCurrentPosition();
                        }
                        else if(targetPosition==1){
                            b=videoPlayer.getCurrentPosition();
                        }
                        else if(targetPosition==2){
                            c=videoPlayer.getCurrentPosition();
                        }
                        else if(targetPosition==3){
                            d=videoPlayer.getCurrentPosition();
                        }
                        else if(targetPosition==4){
                            e=videoPlayer.getCurrentPosition();
                        }
                        else{
                            f=videoPlayer.getCurrentPosition();
                        }}
                    }

                    // There's a special case when the end of the list has been reached.
                    // Need to handle that with this bit of logic
                    if(!recyclerView.canScrollVertically(1)){
                        playVideo(true);
                    }
                    else{
                        playVideo(false);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        addOnChildAttachStateChangeListener(new OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {

            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                if (viewHolderParent != null && viewHolderParent.equals(view)) {
                    resetVideoView();
                }

            }
        });

        videoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {

                    case Player.STATE_BUFFERING:
                        Log.e(TAG, "onPlayerStateChanged: Buffering video.");
                        if (progressBar != null) {
                            progressBar.setVisibility(VISIBLE);
                        }

                        break;
                    case Player.STATE_ENDED:
                        Log.d(TAG, "onPlayerStateChanged: Video ended.");
                        videoPlayer.seekTo(0);
                        break;
                    case Player.STATE_IDLE:

                        break;
                    case Player.STATE_READY:
                        Log.e(TAG, "onPlayerStateChanged: Ready to play.");
                        if (progressBar != null) {
                            progressBar.setVisibility(GONE);
                        }
                        if(!isVideoViewAdded){
                            addVideoView();
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        });
    }

    public void playVideo(boolean isEndOfList) {

        targetPosition=0;

        if(!isEndOfList){
            int startPosition = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
            int endPosition = ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition();

            // if there is more than 2 list-items on the screen, set the difference to be 1
            if (endPosition - startPosition > 1) {
                endPosition = startPosition + 1;
            }

            // something is wrong. return.
            if (startPosition < 0 || endPosition < 0) {
                return;
            }

            // if there is more than 1 list-item on the screen
            if (startPosition != endPosition) {
                int startPositionVideoHeight = getVisibleVideoSurfaceHeight(startPosition);
                int endPositionVideoHeight = getVisibleVideoSurfaceHeight(endPosition);

                targetPosition = startPositionVideoHeight > endPositionVideoHeight ? startPosition : endPosition;
            }
            else {
                targetPosition = startPosition;
            }
        }
        else{
            targetPosition = mediaObjects.size() - 1;
        }

        Log.d(TAG, "playVideo: target position: " + targetPosition);

        // video is already playing so return
        if (targetPosition == playPosition) {
            return;
        }

        // set the position of the list-item that is to be played
        playPosition = targetPosition;
        if (videoSurfaceView == null) {
            return;
        }

        // remove any old surface views from previously playing videos
        videoSurfaceView.setVisibility(INVISIBLE);
        removeVideoView(videoSurfaceView);

        int currentPosition = targetPosition - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();

        View child = getChildAt(currentPosition);
        if (child == null) {
            return;
        }

        PlayerViewHolder holder = (PlayerViewHolder) child.getTag();
        if (holder == null) {
            playPosition = -1;
            return;
        }
        thumbnail = holder.thumbnail;
        progressBar = holder.progressBar;
        volumeControl = holder.volumeControl;
        viewHolderParent = holder.itemView;
        requestManager = holder.requestManager;
        frameLayout = holder.itemView.findViewById(R.id.media_container);

        videoSurfaceView.setPlayer(videoPlayer);

        viewHolderParent.setOnClickListener(videoViewClickListener);
        Handler mainHandler = new Handler();
        String userAgent = Util.getUserAgent(context, "User Agent");
        DataSource.Factory dataSourceFactory = new DefaultHttpDataSourceFactory(
                userAgent, null,
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                1800000,
                true);


        String mediaUrl = mediaObjects.get(targetPosition).getMedia_url();
        if (mediaUrl != null) {
            HlsMediaSource videoSource = new HlsMediaSource(Uri.parse(mediaUrl), dataSourceFactory, 1800000,
                    mainHandler, null);
            Toast.makeText(viewHolderParent.getContext(), Integer.toString(targetPosition), Toast.LENGTH_SHORT).show();
            videoPlayer.prepare(videoSource);
            if(targetPosition==0){
               videoPlayer.seekTo(a);
            }
            else if(targetPosition==1){
                videoPlayer.seekTo(b);
            }
            else if(targetPosition==2){
                videoPlayer.seekTo(c);
            }
            else if(targetPosition==3){
                videoPlayer.seekTo(d);
            }
            else if(targetPosition==4){
                videoPlayer.seekTo(e);
            }
            else{
                videoPlayer.seekTo(f);
            }
            videoPlayer.setPlayWhenReady(true);
        }
    }

    private OnClickListener videoViewClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            toggleVolume();
        }
    };

    /**
     * Returns the visible region of the video surface on the screen.
     * if some is cut off, it will return less than the @videoSurfaceDefaultHeight
     * @param playPosition
     * @return
     */
    private int getVisibleVideoSurfaceHeight(int playPosition) {
        int at = playPosition - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
        Log.d(TAG, "getVisibleVideoSurfaceHeight: at: " + at);

        View child = getChildAt(at);
        if (child == null) {
            return 0;
        }

        int[] location = new int[2];
        child.getLocationInWindow(location);

        if (location[1] < 0) {
            return location[1] + videoSurfaceDefaultHeight;
        } else {
            return screenDefaultHeight - location[1];
        }
    }

    public void onPausePlayer() {
        if (videoSurfaceView != null) {
            removeVideoView(videoSurfaceView);
            videoPlayer.release();
            if(targetPosition==0){
                a=videoPlayer.getCurrentPosition();
            }
            else if(targetPosition==1){
                b=videoPlayer.getCurrentPosition();
            }
            else if(targetPosition==2){
                c=videoPlayer.getCurrentPosition();
            }
            else if(targetPosition==3){
                d=videoPlayer.getCurrentPosition();
            }
            else if(targetPosition==4){
                e=videoPlayer.getCurrentPosition();
            }
            else{
                f=videoPlayer.getCurrentPosition();
            }
            m=1;
            videoSurfaceView = null;
        }
    }

    public void onRestartPlayer() {
        if (videoSurfaceView == null) {
            playPosition = -1;
            init(context);
        }
    }
    // Remove the old player
    private void removeVideoView(PlayerView videoView) {
        ViewGroup parent = (ViewGroup) videoView.getParent();
        if (parent == null) {
            return;
        }

        int index = parent.indexOfChild(videoView);
        if (index >= 0) {
            parent.removeViewAt(index);
            isVideoViewAdded = false;
            viewHolderParent.setOnClickListener(null);
        }

    }

    private void addVideoView(){
        frameLayout.addView(videoSurfaceView);
        isVideoViewAdded = true;
        videoSurfaceView.requestFocus();
        videoSurfaceView.setVisibility(VISIBLE);
        videoSurfaceView.setAlpha(1);
        thumbnail.setVisibility(GONE);
    }

    private void resetVideoView(){
        if(isVideoViewAdded){
            removeVideoView(videoSurfaceView);
            playPosition = -1;
            videoSurfaceView.setVisibility(INVISIBLE);
            thumbnail.setVisibility(VISIBLE);
        }
    }

    public void releasePlayer() {

        if (videoPlayer != null) {
            videoPlayer.release();
            videoPlayer = null;
        }

        viewHolderParent = null;
    }

    private void toggleVolume() {
        if (videoPlayer != null) {
            if (volumeState == VolumeState.OFF) {
                Log.d(TAG, "togglePlaybackState: enabling volume.");
                setVolumeControl(VolumeState.ON);

            } else if(volumeState == VolumeState.ON) {
                setSystemUiVisibility(SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                Log.d(TAG, "togglePlaybackState: disabling volume.");
                setVolumeControl(VolumeState.OFF);

            }
        }
    }

    private void setVolumeControl(VolumeState state){
        volumeState = state;
        if(state == VolumeState.OFF){
            //mediaObjects.get(targetPosition).setThumbnail("https://drive.google.com/open?id=1B3z577pMDX7eKEY4EY4da8ZZA0aq-DPb");

            videoPlayer.setVolume(0f);
            animateVolumeControl();
        }
        else if(state == VolumeState.ON){
            videoPlayer.setVolume(1f);
            animateVolumeControl();
        }
    }


    private void animateVolumeControl(){
        if(volumeControl != null){
            volumeControl.bringToFront();
            if(volumeState == VolumeState.OFF){
                requestManager.load(R.drawable.ic_volume_off_grey_24dp)
                        .into(volumeControl);
            }
            else if(volumeState == VolumeState.ON){
                requestManager.load(R.drawable.ic_volume_up_grey_24dp)
                        .into(volumeControl);
            }
            volumeControl.animate().cancel();

            volumeControl.setAlpha(1f);

            volumeControl.animate()
                    .alpha(0f)
                    .setDuration(600).setStartDelay(1000);
        }
    }

    public void setMediaObjects(ArrayList<MediaObject> mediaObjects){
        this.mediaObjects = mediaObjects;
    }
}