package com.ojhdtapp.parabox.data.remote.dto

import android.content.Context
import android.media.MediaMetadataRetriever
import com.ojhdtapp.messagedto.ReceiveMessageDto
import com.ojhdtapp.messagedto.SendMessageDto
import com.ojhdtapp.messagedto.message_content.getContentString
import com.ojhdtapp.parabox.core.util.FileUtil
import com.ojhdtapp.parabox.core.util.toDateAndTimeString
import com.ojhdtapp.parabox.data.local.entity.ContactEntity
import com.ojhdtapp.parabox.data.local.entity.MessageEntity
import com.ojhdtapp.parabox.domain.model.LatestMessage
import com.ojhdtapp.parabox.domain.model.PluginConnection
import com.ojhdtapp.parabox.domain.model.Profile
import com.ojhdtapp.parabox.domain.model.message_content.*

fun ReceiveMessageDto.toContactEntity(): ContactEntity {
    return ContactEntity(
        profile = subjectProfile.toProfile(),
        latestMessage = LatestMessage(
            sender = profile.name,
            content = contents.getContentString(),
            timestamp = timestamp,
            unreadMessagesNum = 0,
        ),
        contactId = pluginConnection.objectId,
        senderId = pluginConnection.objectId,
        isHidden = false,
        isPinned = false,
        isArchived = false,
        enableNotifications = true,
        tags = emptyList()
    )
}

fun ReceiveMessageDto.toMessageEntity(context: Context): MessageEntity {
    return MessageEntity(
        contents = contents.toMessageContentList(context),
        profile = profile.toProfile(),
        timestamp = timestamp,
        messageId = messageId ?: 0,
        sentByMe = false,
        verified = true
    )
}

//fun SendMessageDto.toMessageEntity(): MessageEntity {
//    return MessageEntity(
//        contents = contents.toMessageContentList(),
//        profile = Profile("", null, null),
//        timestamp = timestamp,
//        sentByMe = true,
//        verified = false
//    )
//}
//
//fun SendMessageDto.toContactEntity(senderName: String): ContactEntity {
//    return ContactEntity(
//        profile = Profile(pluginConnection.id.toString(), null, null),
//        latestMessage = LatestMessage(
//            sender = senderName,
//            content = contents.getContentString(),
//            timestamp = timestamp,
//            unreadMessagesNum = 0,
//        ),
//        contactId = pluginConnection.objectId,
//        senderId = pluginConnection.objectId,
//        isHidden = false,
//        isPinned = false,
//        isArchived = false,
//        enableNotifications = true,
//        tags = emptyList()
//    )
//}

fun com.ojhdtapp.messagedto.Profile.toProfile(): Profile {
    return Profile(this.name, this.avatar, null)
}

fun List<com.ojhdtapp.messagedto.message_content.MessageContent>.toMessageContentList(context: Context): List<MessageContent> {
    return this.map {
        it.toMessageContent(context = context)
    }
}

fun com.ojhdtapp.messagedto.message_content.MessageContent.toMessageContent(context: Context): MessageContent {
    return when (this) {
        is com.ojhdtapp.messagedto.message_content.PlainText -> PlainText(this.text)
        is com.ojhdtapp.messagedto.message_content.Image -> Image(
            url,
            width,
            height,
            uri?.toString()
        )
        is com.ojhdtapp.messagedto.message_content.At -> com.ojhdtapp.parabox.domain.model.message_content.At(
            target,
            name
        )
        is com.ojhdtapp.messagedto.message_content.AtAll -> com.ojhdtapp.parabox.domain.model.message_content.AtAll
        is com.ojhdtapp.messagedto.message_content.Audio -> {
            val path = context.getExternalFilesDir("chat")!!
            val copiedPath = uri?.let {
                FileUtil.copyFileToPath(
                    context,
                    path,
                    "${
                        System.currentTimeMillis().toDateAndTimeString()
                    }.${fileName.substringAfterLast('.')}",
                    it
                )
            }
            val copiedUri = copiedPath?.let { FileUtil.getUriOfFile(context, it) }
            Audio(
                url,
                if (length == 0L && copiedPath?.exists() == true) {
                    MediaMetadataRetriever().apply {
                        setDataSource(copiedPath.absolutePath)
                    }.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
                } else length,
                fileName,
                fileSize,
                copiedUri?.toString()
            )
        }
        is com.ojhdtapp.messagedto.message_content.QuoteReply -> {
            quoteMessageContent
            com.ojhdtapp.parabox.domain.model.message_content.QuoteReply(
                quoteMessageSenderName,
                quoteMessageTimestamp,
                quoteMessageId,
                quoteMessageContent?.toMessageContentList(context)
            )
        }
        is com.ojhdtapp.messagedto.message_content.File -> com.ojhdtapp.parabox.domain.model.message_content.File(
            url,
            name,
            extension,
            size,
            lastModifiedTime,
            expiryTime,
            uri
        )
        else -> PlainText(this.getContentString())
    }
}

fun com.ojhdtapp.messagedto.PluginConnection.toPluginConnection(): PluginConnection {
    return PluginConnection(this.connectionType, this.objectId, this.id)
}