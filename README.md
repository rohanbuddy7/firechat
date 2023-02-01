<h1> Firechat :green_apple:</h1>
Firechat is fast and easy to integrate messsaging framework which lets you chat with a user with easy to use interface


<h2>Prerequisite </h2>

- Create a project on android studio and firebase
- Add dependencies of firebase in android project Gradle file, add google-services.json
- Setup firestore, setup database server in test or production mode.
- Give all permissions to read and write on Firestore
- Enable Cloud Messaging API (Legacy) from firebase panel and copy the server token. Add it in your firestore like this:

        (key>cloud>serverKey=**your server token**)


<h2>Download</h2>

To get a Git project into your build:

Step 1. Add the JitPack repository to your build file

gradle
maven
sbt
leiningen
Add it in your root build.gradle at the end of repositories:
```ruby
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
Step 2. Add the dependency
```ruby
dependencies {
    implementation 'com.github.rohanbuddy7:firechat:1.0.2'
}
```
That's it! The first time you request a project JitPack checks out the code, builds it and serves the build artifacts (jar, aar).

<h2>How to use</h2>

```ruby

private var fire: FireChatHelper? = null
private var variant = FireChatHelper.production
private val otherUser = "**UNIQUE_USERID**"
private val myUser = Users(
        id = "**UNIQUE_USERID**",
        name = "Ramesh",
        profilePicture = "**IMAGE_URL**"
    )
    
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_launch)

    FirebaseApp.initializeApp(this)
    fire = FireChatHelper.getInstance(FireChatHelper.staging)

    setupNotification()

    # on notifcation click redirecting to specific chat 
    when(intent.action){
        "FIRECHAT_NEW_MESSAGE"->{
            val chatId = intent.getStringExtra("chatId")
            launchAndEnterChat(chatId)
        }
    }
}

private fun openChatlists() {
    fire?.openChats(myUser, this)
}

private fun startNewConversation() {
    fire?.startDistinctConversation(myUser, otherUser, this)
}

private fun launchAndEnterChat(chatId: String?) {
    fire?.openChats(myUser, this, chatId = chatId)
}

private fun setupNotification() {
    FireChatHelper.getInstance(variant)
        .setNotificationListener(object : FireChatHelper.OnMessageReceivedListener {
            override fun onMessageReceived(
                title: String,
                message: String,
                unseenCount: String,
                chatId: String,
                action: String
            ) {
                # handling notif
                Notification().showNotification(
                    context = this@LaunchActivity,
                    title = title,
                    message = message,
                    unseenCount = unseenCount,
                    chatId = chatId,
                    action = action
                )
            }
        })
}


```
