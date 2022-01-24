// IPCItemChangeListener.aidl
package com.lanlin.controller;
import com.lanlin.controller.data.Wrapper;

// 播放歌曲变化时的回调
interface IPCItemChangeListener {
    void onItemChange(in Wrapper wrapper);
}