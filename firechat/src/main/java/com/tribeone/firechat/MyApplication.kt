package com.tribeone.firechat

import android.app.Application
import com.tribeone.firechat.di.component.FcApplicationComponent
import com.tribeone.firechat.di.component.DaggerFcApplicationComponent
import com.tribeone.firechat.model.Users
import dagger.Component

internal class MyApplication: Application() {

    lateinit var fcApplicationComponent: FcApplicationComponent

    var component: Component? = null

    companion object{
        var user: Users? = null
        var userId: String? = ""
        var otherUserId: String? = ""
        var userFCM: String? = ""
        var otherUserFCM: String? = ""
        var chatlistFragmentVisible: Boolean? = false
        var messageFragmentVisible: Boolean? = false
        var allUserDetails: HashMap<String, Users> = hashMapOf()
    }

    override fun onCreate() {
        super.onCreate()
        fcApplicationComponent = DaggerFcApplicationComponent
            .builder()
            .build()
        fcApplicationComponent.inject(this)

    }

}