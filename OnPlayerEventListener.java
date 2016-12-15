package com.soundai.smartboxlite.Service;

import com.soundai.smartboxlite.Model.Music;

/**
 * Created by fez on 2016/12/2.
 */

public interface OnPlayerEventListener {

    //切换歌曲
    void onChange(Music music);
    //暂停播放
    void onPlayerPause();
    //继续播放
    void onPlayerResume();

}
