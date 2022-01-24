package com.lanlin.controller.player.client

import com.lanlin.controller.player.provider.ControllerProvider

class BinderPoolClientInstance {
    private val clientMap: MutableMap<Class<*>, BinderPoolClient> = mutableMapOf()


    fun getClient(clazz: Class<*>): BinderPoolClient {
        if (clientMap.containsKey(clazz)) {
            return clientMap[clazz]!!
        }
        val client = BinderPoolClient(ControllerProvider.getApplicationContext(), clazz)
        clientMap[clazz] = client
        return client
    }



    companion object {
        @Volatile
        private var instance: BinderPoolClientInstance? = null

        fun getInstance(): BinderPoolClientInstance {
            if (instance == null) {
                synchronized(BinderPoolClientInstance::class.java) {
                    if (instance == null) {
                        instance = BinderPoolClientInstance()
                    }
                }
            }

            return instance!!
        }
    }

}