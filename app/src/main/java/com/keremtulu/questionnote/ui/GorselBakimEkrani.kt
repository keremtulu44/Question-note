package com.keremtulu.questionnote.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.images.GorselBakimRaporu
import com.keremtulu.questionnote.data.images.GorselTutarliligiHizmeti
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import com.keremtulu.questionnote.ui.premium.PremiumTextButton
import com.keremtulu.questionnote.ui.premium.PremiumButton

@Composable
fun GorselBakimEkrani(
    service: GorselTutarliligiHizmeti,
    onGeri: () -> Unit,
) {
    var report by remember { mutableStateOf<GorselBakimRaporu?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    fun runAudit() {
        loading = true
        error = false
        scope.launch {
            try {
                report = service.denetle()
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                error = true
            } finally {
                loading = false
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets.safeDrawing,
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding),
        ) {
            Column(Modifier.fillMaxWidth().padding(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PremiumTextButton(onClick = onGeri) { Text(stringResource(R.string.common_back)) }
                com.keremtulu.questionnote.ui.bilesenler.DuzBaslik(
                    baslik = stringResource(R.string.image_maintenance_title),
                    alt = stringResource(R.string.image_maintenance_description),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PremiumButton(onClick = ::runAudit, enabled = !loading) { Text(stringResource(R.string.image_audit_action)) }
            }
            if (loading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) { CircularProgressIndicator() }
            } else if (error) {
                Text(
                    stringResource(R.string.image_audit_error),
                    modifier = Modifier.padding(24.dp),
                    color = MaterialTheme.colorScheme.error,
                )
            } else {
                report?.let { current ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            Text(
                                stringResource(
                                    R.string.image_audit_summary,
                                    current.eksikDosyalar.size,
                                    current.bozukDosyalar.size,
                                    current.onayBekleyenYetimler.size,
                                ),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        if (current.eksikDosyalar.isNotEmpty()) {
                            item { Text(stringResource(R.string.image_missing_files), color = MaterialTheme.colorScheme.error) }
                            items(current.eksikDosyalar) { name -> Text(name, style = MaterialTheme.typography.bodySmall) }
                        }
                        if (current.bozukDosyalar.isNotEmpty()) {
                            item { Text(stringResource(R.string.image_corrupt_files), color = MaterialTheme.colorScheme.error) }
                            items(current.bozukDosyalar) { name -> Text(name, style = MaterialTheme.typography.bodySmall) }
                        }
                        if (current.onayBekleyenYetimler.isNotEmpty()) {
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(stringResource(R.string.image_orphan_files), style = MaterialTheme.typography.titleSmall)
                                    Text(stringResource(R.string.image_orphan_warning), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            items(current.onayBekleyenYetimler, key = { it.dosyaAdi }) { file ->
                                Text(
                                    stringResource(R.string.image_orphan_item, file.dosyaAdi, file.boyut),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            item {
                                PremiumButton(onClick = { confirmDelete = true }) {
                                    Text(stringResource(R.string.image_orphan_delete_action, current.onayBekleyenYetimler.size))
                                }
                            }
                        }
                        if (current.eksikDosyalar.isEmpty() && current.bozukDosyalar.isEmpty() && current.onayBekleyenYetimler.isEmpty()) {
                            item { Text(stringResource(R.string.image_audit_clean)) }
                        }
                    }
                }
            }
        }
    }

    if (confirmDelete && report != null) {
        val selectedNames = report!!.onayBekleyenYetimler.map { it.dosyaAdi }.toSet()
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.image_orphan_confirm_title)) },
            text = { Text(stringResource(R.string.image_orphan_confirm, selectedNames.size)) },
            confirmButton = {
                PremiumTextButton(onClick = {
                    confirmDelete = false
                    scope.launch {
                        try {
                            val deleted = service.onaylananYetimleriSil(selectedNames)
                            snackbar.showSnackbar(context.getString(R.string.image_orphan_deleted, deleted))
                            report = service.denetle()
                        } catch (cancel: CancellationException) {
                            throw cancel
                        } catch (_: Exception) {
                            snackbar.showSnackbar(context.getString(R.string.image_audit_error))
                        }
                    }
                }) { Text(stringResource(R.string.image_orphan_delete_confirm)) }
            },
            dismissButton = { PremiumTextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.topic_cancel)) } },
        )
    }
}
