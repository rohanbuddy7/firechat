package com.tribeone.firechat.fcm

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tribeone.firechat.MyApplication
import com.tribeone.firechat.MyApplication.Companion.userFCM
import com.tribeone.firechat.R
import com.tribeone.firechat.ui.main.MainActivity
import com.tribeone.firechat.utils.Constants
import com.tribeone.firechat.utils.Constants.FCM.CHANNEL_MESSAGE_RECEIVED
import com.tribeone.firechat.utils.FireChatErrors
import com.tribeone.firechat.utils.FireChatErrors.BUILD_VARIANT_NULL
import com.tribeone.firechat.utils.FireChatHelper
import org.json.JSONException
import org.json.JSONObject
import kotlin.random.Random


internal class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        userFCM = token
    }

    @SuppressLint("LogNotTimber")
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data
        val channelUrl: String?
        val messageTitle: String?
        val messageBody: String?

        if (data.isNotEmpty()) {

            if (data.containsKey(Constants.FCMParams.type)) {
                val requestUuid = data[Constants.FCMParams.request_uuid]
                val type = data[Constants.FCMParams.type]
                val dataContent = data[Constants.FCMParams.data]
                handleOnMessage(applicationContext, requestUuid, type, dataContent, false)
            }
        }
    }

    fun handleOnMessage(
        context: Context,
        fcmId: String?,
        type: String?,
        internalData: String?,
        isPolled: Boolean
    ) {
        when (type) {
            Constants.FCM.FIRECHAT_NEW_MESSAGE -> {
                val jsonObject = JSONObject(internalData)
                if (MyApplication.chatlistFragmentVisible == true || MyApplication.messageFragmentVisible == true) {
                    val intent = Intent(Constants.FCM.FIRECHAT_BROADCAST)
                    //intent.action = Constants.FCM.FIRECHAT_NEW_MESSAGE
                    intent.putExtra(Constants.FCMParams.title, jsonObject.getString(Constants.FCMParams.title))
                    intent.putExtra(Constants.FCMParams.message, jsonObject.getString(Constants.FCMParams.message))
                    intent.putExtra(Constants.FCMParams.action, jsonObject.getString(Constants.FCMParams.action))
                    intent.putExtra(Constants.FCMParams.chatId, jsonObject.getString(Constants.FCMParams.chatId))
                    intent.putExtra(Constants.FCMParams.seen, jsonObject.getString(Constants.FCMParams.seen))
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                } else {
                    try {
                        /*showNotification(
                            context,
                            jsonObject.getString(Constants.FCMParams.title),
                            jsonObject.getString(Constants.FCMParams.message),
                            jsonObject.getString(Constants.FCMParams.seen),
                        )*/
                        if(FireChatHelper.buildVariants == null){
                            FireChatErrors.crashIt(BUILD_VARIANT_NULL)
                        }
                        FireChatHelper.getInstance(FireChatHelper.buildVariants!!).onMessageReceivedListener?.onMessageReceived(
                            jsonObject.getString(Constants.FCMParams.title),
                            jsonObject.getString(Constants.FCMParams.message),
                            jsonObject.getString(Constants.FCMParams.seen),
                            jsonObject.getString(Constants.FCMParams.chatId),
                            jsonObject.getString(Constants.FCMParams.action)
                        )

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
            Constants.FCM.FIRECHAT_MESSAGE_SEEN -> {
                val jsonObject = JSONObject(internalData)
                if (MyApplication.chatlistFragmentVisible == true || MyApplication.messageFragmentVisible == true) {
                    val intent = Intent(Constants.FCM.FIRECHAT_BROADCAST)
                    //intent.action = Constants.FCM.FIRECHAT_MESSAGE_SEEN
                    intent.putExtra(Constants.FCMParams.action, jsonObject.getString(Constants.FCMParams.action))
                    intent.putExtra(Constants.FCMParams.chatId, jsonObject.getString(Constants.FCMParams.chatId))
                    intent.putExtra(Constants.FCMParams.userId, jsonObject.getString(Constants.FCMParams.userId))
                    intent.putExtra(Constants.FCMParams.seen, jsonObject.getString(Constants.FCMParams.seen))
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                }
            }
        }
    }


    /*private fun showNotification(
        context: Context,
        title: String,
        message: String,
        unseenCount: String
    ) {
        val notificationId = Random.nextInt()
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.action = Constants.Action.OPEN_CHATLIST
        intent.putExtra(Constants.EXTRA_NOTIFICATION_ID, notificationId)

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            else PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = NotificationCompat.Builder(context, resources.getString(R.string.new_message))
            .setSmallIcon(R.drawable.bg_blue_round)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(contentPendingIntent)
            .setColor(ContextCompat.getColor(context, R.color.cerulean_blue))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setAutoCancel(true)

        if (unseenCount != "0") {
            builder.setNumber(unseenCount.toInt())
        }

        createHomeLoanUpdateNotificationChannel(context, builder)

        val notificationmanager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationmanager.notify(1997, builder.build())
    }

    private fun createHomeLoanUpdateNotificationChannel(
        context: Context,
        builder: NotificationCompat.Builder
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = CHANNEL_MESSAGE_RECEIVED
            val serviceChannel = NotificationChannel(
                channelId,
                resources.getString(R.string.new_message),
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
            builder.setChannelId(channelId)
        }
    }*/

}