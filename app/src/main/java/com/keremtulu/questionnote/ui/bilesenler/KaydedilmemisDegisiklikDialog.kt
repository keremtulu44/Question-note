package com.keremtulu.questionnote.ui.bilesenler

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.ui.premium.PremiumTextButton

/** A deliberate exit is required before discarding changes in a student form. */
@Composable
fun KaydedilmemisDegisiklikDialog(onCik: () -> Unit, onDevam: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDevam,
        title = { Text(stringResource(R.string.unsaved_changes_title)) },
        text = { Text(stringResource(R.string.unsaved_changes_message)) },
        confirmButton = {
            PremiumTextButton(onClick = onCik) {
                Text(stringResource(R.string.unsaved_changes_discard), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            PremiumTextButton(onClick = onDevam) { Text(stringResource(R.string.unsaved_changes_continue)) }
        },
    )
}
