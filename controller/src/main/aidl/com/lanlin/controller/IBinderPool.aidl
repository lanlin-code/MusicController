// IBinderPool.aidl
package com.lanlin.controller;

// Declare any non-default types here with import statements

// Binder池，根据code查询相应的Binder
interface IBinderPool {
    IBinder queryBinder(int code);
}