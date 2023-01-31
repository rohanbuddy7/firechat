package com.tribeone.firechat

import android.app.Application
import com.google.firebase.FirebaseApp
import com.tribeone.firechat.di.component.ApplicationComponent
import com.tribeone.firechat.di.component.DaggerApplicationComponent
import com.tribeone.firechat.di.module.ApplicationModule
import com.tribeone.firechat.model.Users
import dagger.Component

internal class MyApplication: Application() {

    lateinit var applicationComponent: ApplicationComponent

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
        applicationComponent = DaggerApplicationComponent
            .builder()
            .build()
        applicationComponent.inject(this)

    }

}