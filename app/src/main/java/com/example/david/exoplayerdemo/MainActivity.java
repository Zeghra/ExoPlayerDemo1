package com.example.david.exoplayerdemo;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.example.david.exoplayerdemo.Utils.Valtozok;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class MainActivity extends AppCompatActivity {
    private SimpleExoPlayer exoPlayer;
    private SimpleExoPlayerView simpleExoPlayerView;
    private int videoId = 0;
    private Uri uri;
    private boolean loop = true;
    private MediaSource mediaSource;
    private DefaultDataSourceFactory dataSourceFactory;
    private ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
    private LoopingMediaSource loopingMediaSource;
    private BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
    private TrackSelector trackSelector;
    //private final Animation fadeOut = new AlphaAnimation(1, 0);
    private ImageView feketeView;
    private Bitmap bitmap = null;
    private String avaMem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // teljes képernyőre vált
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION); // navigációs gombok elrejtése
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide(); //actionbar-t leveszi

        simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.exoplayer_view);
        simpleExoPlayerView.setUseController(false);  // leveszi az állito sávot (lejátszási csik, eleje vége gomb
        feketeView = (ImageView) findViewById(R.id.feketeView);


        videoLejatszas();

    }

    private void getAvailableMemory() {  // próbáltam memóriát nézni
        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);

        Log.i("DEJO", " memoryInfo.availMem " + memoryInfo.availMem + "\n");  // szabad memória
        Log.i("DEJO", " memoryInfo.lowMemory " + memoryInfo.lowMemory + "\n"); //alacsony-e a memória
        Log.i("DEJO", " memoryInfo.threshold " + memoryInfo.threshold + "\n");

        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();

        Map<Integer, String> pidMap = new TreeMap<Integer, String>();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
            pidMap.put(runningAppProcessInfo.pid, runningAppProcessInfo.processName);
        }

        Collection<Integer> keys = pidMap.keySet();

        for (int key : keys) {
            int pids[] = new int[1];
            pids[0] = key;
            android.os.Debug.MemoryInfo[] memoryInfoArray = activityManager.getProcessMemoryInfo(pids);
            for (android.os.Debug.MemoryInfo pidMemoryInfo : memoryInfoArray) {
                Log.i("DEJO", String.format("** MEMINFO in pid %d [%s] **\n", pids[0], pidMap.get(pids[0])));
                Log.i("DEJO", " pidMemoryInfo.getTotalPrivateDirty(): " + pidMemoryInfo.getTotalPrivateDirty() + "\n");
                Log.i("DEJO", " pidMemoryInfo.getTotalPss(): " + pidMemoryInfo.getTotalPss() + "\n");
                Log.i("DEJO", " pidMemoryInfo.getTotalSharedDirty(): " + pidMemoryInfo.getTotalSharedDirty() + "\n");
            }
        }
    }

    public void getVideoFrame(Uri uri) {

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            //getAvailableMemory();
            retriever.setDataSource(String.valueOf(uri));
            bitmap = null;
            bitmap = retriever.getFrameAtTime();
            feketeView.setImageBitmap(bitmap);
            retriever.release();
            //feketeView.setBackground(new BitmapDrawable(bitmap));

            //Log.e("VIDEO", "retrieve.getFrame " + retriever.getFrameAtTime().toString());

        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        } finally {
            try {
                retriever.release();

            } catch (RuntimeException ex) {
            }
        }
        retriever.release();
    }

    public void fadeOut() {
        //áttűnési effekt

        Animation fadeOut = new AlphaAnimation(1, 0); //teljesen fedőről áttetszőre vált
        fadeOut.setInterpolator(new AccelerateInterpolator()); // animációs effekt
        fadeOut.setDuration(Valtozok.ATTUNESI_IDO); // áttűnési idő
        feketeView.setAnimation(fadeOut);   //beállitja az animációt a feketeView-nak
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {  // animáció kezdésekor
                feketeView.setVisibility(View.VISIBLE);  // látszódjon az ImageView feketeView
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                feketeView.setVisibility(View.INVISIBLE);  // animáció végén elrejti a feketeView-t
                //getAvailableMemory();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private void videoLejatszas() {

        try {
            // adaptiv lejátszáshoz kell
            trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
            exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

            if (videoId == 0) { // ha a nulla videot játsza
                // 0. video elérési útja
                uri = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() + Valtozok.VIDEOPATH + Valtozok.VIDEO_NULLA);
            }

            //videolejátszó paraméterezése
            dataSourceFactory = new DefaultDataSourceFactory(this, "exoplayer_video");
            mediaSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);
            simpleExoPlayerView.setPlayer(exoPlayer);

            // loopolás
            if (loop) {
                loopingMediaSource = new LoopingMediaSource(mediaSource);
                exoPlayer.prepare(loopingMediaSource);
            } else {
                exoPlayer.prepare(mediaSource);
            }

            exoPlayer.setPlayWhenReady(true); // inditja a videot
            exoPlayer.addListener(new ExoPlayer.EventListener() {
                @Override
                public void onTimelineChanged(Timeline timeline, Object manifest) {

                }

                @Override
                public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

                }

                @Override
                public void onLoadingChanged(boolean isLoading) {

                }

                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                    if (playbackState == exoPlayer.STATE_ENDED && videoId != 0) {  //ha a video lejátszásnak vége

                        videoId = 0;     // 0. video beállit
                        loop = true;     // loop igaz

                        videoLejatszas();  // videot indit
                    }
                    if (playbackState == exoPlayer.STATE_READY) {
                        Log.e("DEJO", "STATE_READY " + uri + " videoid " + videoId);

                    }
                    if (playbackState == exoPlayer.STATE_BUFFERING) {

                        Log.e("DEJO", "STATE_BUFFERING " + uri + " videoid " + videoId);
                    }
                    if (playbackState == exoPlayer.STATE_IDLE) {
                        Log.e("DEJO", "STATE_IDLE " + uri + " videoid " + videoId);

                    }
                    if (playbackState == exoPlayer.STATE_ENDED) {
                        Log.e("DEJO", "STATE_ENDED " + uri + " videoid " + videoId);
                    }
                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {
                    Log.e("DEJO", error.toString());
                }

                @Override
                public void onPositionDiscontinuity() {

                }

                @Override
                public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

                    Log.e("DEJO", "onPlaybackParamChanged " + uri.toString());
                }
            });
        } catch (Exception e) {
            Log.e("DEJO", e.toString());
        }
    }

    // gomb felengedése
    @Override
    public boolean onKeyUp(final int keyCode, final KeyEvent event) {
        switch (keyCode) {
            case Valtozok.VIDEO_EGY_GOMB:
                Log.e("DEJO", "1-est nyomtam");
                if (videoId != 1) { // ha már játsza az 1-es videot, akkor nem engedi újrainditani
                    bitmap = null;  // törli a bitmap képét
                    getVideoFrame(uri);  // képet készit a kijelzőről
                    exoPlayer.stop(); // leállitja az exoplayert, ha nincs itt ez, akkor out of memoryval elszáll
                    fadeOut(); // áttűnés
                    loop = false;   // ne loopolja az 1-es videot
                    videoId = 1;    // ez az 1-es video
                    uri = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() + Valtozok.VIDEOPATH + Valtozok.VIDEO_EGY);  // elérési út
                    videoLejatszas();
                }
                break;

            case Valtozok.VIDEO_KETTO_GOMB:
                Log.e("DEJO", "2-est nyomtam");
                if (videoId != 2) {
                    bitmap = null;
                    getVideoFrame(uri);
                    exoPlayer.stop();
                    fadeOut();
                    loop = false;   // ne loopolja a 2-es videot
                    videoId = 2;    // ez a 2-es video
                    uri = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() + Valtozok.VIDEOPATH + Valtozok.VIDEO_KETTO);  // elérési út
                    videoLejatszas();
                }
                break;

            case Valtozok.VIDEO_HAROM_GOMB:
                Log.e("DEJO", "3-ast nyomtam");
                if (videoId != 3) {
                    bitmap = null;
                    getVideoFrame(uri);
                    exoPlayer.stop();
                    fadeOut();
                    loop = false;   // ne loopolja a 3-es videot
                    videoId = 3;    // ez a 3-as video
                    uri = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() + Valtozok.VIDEOPATH + Valtozok.VIDEO_HAROM);  // elérési út
                    videoLejatszas();
                }
                break;

            default:
                return super.onKeyUp(keyCode, event);
        }
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        exoPlayer.release();
        Log.e("DEJO", "onStop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        exoPlayer.release();
        Log.e("DEJO", "onPause");
    }
}
