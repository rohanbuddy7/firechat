package com.tribeone.firechat.utils

import com.tribeone.firechat.FcMyApplication

internal class FcProfileUtils {

    companion object {
        var fcProfileUtils: FcProfileUtils? = null
        fun getInstance(): FcProfileUtils {
            if (fcProfileUtils == null) {
                fcProfileUtils = FcProfileUtils()
            }
            return fcProfileUtils!!
        }
    }

    fun getName(userId: String): String{
        val user = FcMyApplication.allUserDetails[userId]
        return if(user?.name!=null){
            user.name
        } else {
            userId
        }
    }

    fun getProfilePicture(userId: String): String?{
        val user = FcMyApplication.allUserDetails[userId]
        return if(user?.profilePicture!=null){
            user.profilePicture
        } else {
            "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2c/Default_pfp.svg/256px-Default_pfp.svg.png"
        }
    }

}