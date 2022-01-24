// IPCDataSetChangeListener.aidl
package com.lanlin.controller;
import com.lanlin.controller.data.Wrapper;

// 操作数据源的回调
interface IPCDataSetChangeListener {
    // 回调添加、清除的数据，当清除所有数据时，参数为空列表
    void onChange(in List<Wrapper> source);
}