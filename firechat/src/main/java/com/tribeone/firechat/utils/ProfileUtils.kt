package com.tribeone.firechat.utils

import com.tribeone.firechat.MyApplication

internal class ProfileUtils {

    companion object {
        var profileUtils: ProfileUtils? = null
        fun getInstance(): ProfileUtils {
            if (profileUtils == null) {
                profileUtils = ProfileUtils()
            }
            return profileUtils!!
        }
    }

    fun getName(userId: String): String{
        val user = MyApplication.allUserDetails[userId]
        return if(user?.name!=null){
            user.name
        } else {
            userId
        }
    }

    fun getProfilePicture(userId: String): String?{
        val user = MyApplication.allUserDetails[userId]
        return if(user?.profilePicture!=null){
            user.profilePicture
        } else {
            "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2c/Default_pfp.svg/256px-Default_pfp.svg.png"
        }
    }

}