package com.keisardev.moviesandbeyond.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.Role

/** Clickable modifier that suppresses the default ripple indication but preserves accessibility. */
fun Modifier.noRippleClickable(role: Role = Role.Button, onClick: () -> Unit) = composed {
    Modifier.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        role = role,
        onClick = onClick,
    )
}
