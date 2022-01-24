// IPCListenerController.aidl
package com.lanlin.controller;

import com.lanlin.controller.IPCDataSetChangeListener;
import com.lanlin.controller.IPCItemChangeListener;
import com.lanlin.controller.IPCModeChangeListener;
import com.lanlin.controller.IPCPlayStateChangeListener;

// IPC回调管理接口
interface IPCListenerController {
    void addItemChangeListener(in IPCItemChangeListener listener);
    void addModeChangeListener(in IPCModeChangeListener listener);
    void addPlayStateChangeListener(in IPCPlayStateChangeListener listener);
    void removeItemChangeListener(in IPCItemChangeListener listener);
    void removeModeChangeListener(in IPCModeChangeListener listener);
    void removePlayStateChangeListener(in IPCPlayStateChangeListener listener);
    void addDataChangeListener(in IPCDataSetChangeListener listener);
    void removeDataChangeListener(in IPCDataSetChangeListener listener);
}