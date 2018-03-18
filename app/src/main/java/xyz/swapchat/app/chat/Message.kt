package xyz.teamcatalyst.breedr.chat

import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.commons.models.IUser
import java.util.*

/**
 * Created by aar on 15/12/2017.
 */
data class Message(
        val _id: String,
        val _createdAt: Date,
        val _user: ChatUser,
        val _text: String
) : IMessage {
    override fun getId() = _id
    override fun getCreatedAt() = _createdAt
    override fun getUser() = _user
    override fun getText() = _text
}


data class ChatUser(
        val _avatar: String,
        val _name: String,
        val _id: String
) : IUser {
    override fun getAvatar() = _avatar
    override fun getName() = _name
    override fun getId() = _id
}