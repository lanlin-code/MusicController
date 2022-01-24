// IPCDataController.aidl
package com.lanlin.controller;
import com.lanlin.controller.data.Wrapper;

// 操作音乐数据源的接口
interface IPCDataController {
    void add(in Wrapper wrapper);
    void addAfter(in Wrapper wrapper, int index);
    void remove(in Wrapper wrapper);
    void addAll(in List<Wrapper> wrapper);
    void clear();
    List<Wrapper> allItems();
    Wrapper current();
}