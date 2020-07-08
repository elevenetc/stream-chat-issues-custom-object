package io.getstream.issues.extradata

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.socket.InitConnectionListener

class MainActivity : AppCompatActivity() {

    lateinit var client: ChatClient

    val userId = "stream-eugene"
    val apiKey = "d2q3juekvgsf"
    val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoic3RyZWFtLWV1Z2VuZSJ9.-WNauu6xV56sHM39ZrhxDeBiKjA972O5AYo-dVXva6I"

    val customObjectKey = "customObject"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        client = ChatClient.Builder(apiKey, applicationContext).build()

        client.setUser(User(userId), token, object : InitConnectionListener() {
            override fun onError(error: ChatError) {
                error.printStackTrace()
                showToast("Error setting user: " + error.message)
            }

            override fun onSuccess(data: ConnectionData) {
                getChannelAndSendMessage()
            }
        })
    }

    private fun getChannelAndSendMessage() {
        getChannel()
    }

    private fun getChannel() {
        val channelsRequest = QueryChannelsRequest(Filters.eq("type", "messaging"), 0, 1)
        client.queryChannels(channelsRequest).enqueue {

            when {
                it.isError -> {
                    it.error().printStackTrace()
                    showToast("Error getting channels: " + it.error().message)
                }
                it.data().isEmpty() -> {
                    showToast("No channels")
                }
                else -> {
                    val channel = it.data().first()
                    sendMessage(channel)
                }
            }
        }
    }

    private fun sendMessage(channel: Channel) {

        val message = Message(text = "hello")

        val customExtraObject = HashMap<String, Any>()
        customExtraObject["a"] = "a"
        customExtraObject["b"] = "b"

        message.extraData[customObjectKey] = customExtraObject
        client.sendMessage(channel.type, channel.id, message).enqueue {
            if (it.isError) {
                it.error().printStackTrace()
                showToast("Error messaging sending: " + it.error().message)
            } else {
                val sentMessage = it.data()
                val customObject = sentMessage.extraData[customObjectKey]

                if (customObject == null) {
                    showToast("extra data is not sent")
                } else {
                    val equalsTest = customObject == customExtraObject
                    showToast("extra data sent successfully. Equals test passed: $equalsTest")
                }
            }
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }
}