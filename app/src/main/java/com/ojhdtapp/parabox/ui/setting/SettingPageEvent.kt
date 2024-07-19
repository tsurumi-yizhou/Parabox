package com.ojhdtapp.parabox.ui.setting

import com.ojhdtapp.parabox.core.util.LoadState
import com.ojhdtapp.parabox.domain.model.Chat
import com.ojhdtapp.parabox.domain.model.ExtensionInfo
import com.ojhdtapp.parabox.domain.model.Connection
import com.ojhdtapp.parabox.domain.model.Extension
import com.ojhdtapp.parabox.domain.model.filter.ChatFilter
import com.ojhdtapp.parabox.domain.service.extension.ExtensionInitActionWrapper
import com.ojhdtapp.parabox.ui.base.UiEvent

sealed interface SettingPageEvent : UiEvent{
    @Deprecated("using decompose")
    data class SelectSetting(val setting: Setting) : SettingPageEvent
    data class UpdateConnection(val list: List<Connection>): SettingPageEvent
    data class UpdateExtension(val list: List<Extension>): SettingPageEvent
    data class DeleteExtensionInfo(val extensionId: Long): SettingPageEvent
    data class UpdateExtensionInitActionState(val initActionWrapper: ExtensionInitActionWrapper): SettingPageEvent
    data class InitNewConnection(val extension: Extension.Success): SettingPageEvent
    data class SubmitExtensionInitActionResult(val result: Any): SettingPageEvent
    object RevertExtensionInitAction: SettingPageEvent
    data class InitNewExtensionConnectionDone(val isDone: Boolean) : SettingPageEvent
    data class RestartExtensionConnection(val extensionId: Long): SettingPageEvent
    data class UpdateSelectedTagLabel(val tagLabel: ChatFilter.Tag): SettingPageEvent
    data class TagLabelChatsLoadDone(val chats: List<Chat>, val loadState: LoadState) : SettingPageEvent
    data class UpdateChatTags(val chatId: Long, val tags: List<String>): SettingPageEvent
    data class NotificationDisabledChatLoadDone(val chats: List<Chat>, val loadState: LoadState) : SettingPageEvent
    data class UpdateChatNotificationEnabled(val chatId: Long, val value: Boolean): SettingPageEvent
    data object ReloadExtension: SettingPageEvent
}