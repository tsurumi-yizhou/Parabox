package com.ojhdtapp.parabox.ui

import com.ojhdtapp.parabox.domain.model.Contact

sealed interface MainSharedUiEvent {
    data class ShowSnackBar(val message: String, val label: String? = null, val callback: (() -> Unit)? = null) : MainSharedUiEvent
    data class BottomSheetControl(val shouldOpen: Boolean) : MainSharedUiEvent
    data class NavigateToChat(val contact: Contact) : MainSharedUiEvent
}