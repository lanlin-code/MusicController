// IPCPlayStateChangeListener.aidl
package com.lanlin.controller;

// 播放状态变化时的回调
interface IPCPlayStateChangeListener {
    void playStateChange(boolean state);
}