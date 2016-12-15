package com.soundai.smartboxlite.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.soundai.smartboxlite.Model.Music;
import com.soundai.smartboxlite.Utils.Actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fez on 2016/12/2.
 */

public class MusicPlayerService extends Service implements MediaPlayer.OnCompletionListener {

    private static final String TAG = "SoundAi";

    public static List<Music> mMusicList = new ArrayList<>();
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private OnPlayerEventListener mOnPlayerEventListener;
    private Music mPlayingMusic;    //正在播放的本地歌曲
    private int mPlayingPosition;   //正在播放的歌曲序号
    private boolean isPause;    //是否暂停

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: 音乐服务启动:" + intent.getAction());
        if (intent == null || intent.getAction() == null){
            return START_NOT_STICKY;
        }
        switch (intent.getAction()){
            case Actions.ACTION_MEDIA_PLAY_PAUSE:
                playAndPause();
                break;
            case Actions.ACTION_MEDIA_PLAY:
                play(getPlayingPosition());
                break;
            case Actions.ACTION_MEDIA_PAUSE:
                if (isPlaying()){
                    pause();
                }
                break;
            case Actions.ACTION_MEDIA_RESUME:
                if (isPause()){
                    resume();
                }
                break;
            case Actions.ACTION_MEDIA_NEXT:
                next();
                break;
            case Actions.ACTION_MEDIA_PREVIOUS:
                prev();
                break;
            case Actions.ACTION_MEDIA_STOP:
                stop();
                break;
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new PlayerBinder();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        next();
    }

    public void setOnPlayerEventListener(OnPlayerEventListener listener){
        this.mOnPlayerEventListener = listener;
    }

    public void playAndPause(){
        if (isPlaying()){
            pause();
        } else if (isPause()){
            resume();
        } else {
            play(getPlayingPosition());
        }
    }

    public int play(int position){
        if (getMusicList().isEmpty()){
            return -1;
        }
        if (position < 0){
            position = getMusicList().size() - 1;
        } else if (position >= getMusicList().size()){
            position = 0;
        }
        mPlayingPosition = position;
        mPlayingMusic = getMusicList().get(mPlayingPosition);
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(mPlayingMusic.getDownloadUrl());
            Log.d(TAG, "play: 音频数据设置成功");
            mMediaPlayer.prepare();
            start();
            if (mOnPlayerEventListener != null){
                mOnPlayerEventListener.onChange(mPlayingMusic);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mPlayingPosition;
    }

    public void play(Music music){
        mPlayingMusic = music;
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(mPlayingMusic.getDownloadUrl());
            mMediaPlayer.prepare();
            start();
            if (mOnPlayerEventListener != null){
                mOnPlayerEventListener.onChange(mPlayingMusic);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int pause(){
        if (!isPlaying()){
            return -1;
        }
        mMediaPlayer.pause();
        isPause = true;
        if (mOnPlayerEventListener != null){
            mOnPlayerEventListener.onPlayerPause();
        }
        return mPlayingPosition;
    }

    public int next(){
        return play(mPlayingPosition + 1);
    }

    public int prev(){
        return play(mPlayingPosition - 1);
    }

    public void stop(){
        pause();
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;
        mMusicList.clear();
        stopSelf();
    }

    private void start(){
        mMediaPlayer.start();
        isPause = false;
    }

    private int resume(){
        if (isPlaying()){
            return -1;
        }
        start();
        if (mOnPlayerEventListener != null){
            mOnPlayerEventListener.onPlayerResume();
        }
        return mPlayingPosition;
    }

    public boolean isPlaying(){
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    public boolean isPause(){
        return mMediaPlayer != null && isPause;
    }

    public int getPlayingPosition(){
        return mPlayingPosition;
    }

    public Music getPlayingMusic(){
        return mPlayingMusic;
    }

    public static List<Music> getMusicList(){
        return mMusicList;
    }

    public class PlayerBinder extends Binder{
        public MusicPlayerService getService(){
            return MusicPlayerService.this;
        }
    }
}
