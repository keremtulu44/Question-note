package com.keremtulu.questionnote.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.keremtulu.questionnote.data.ses.SesHizmeti

/**
 * Provided by MainActivity. Null (previews, tests, no-audio contexts) means silent everywhere.
 */
val LocalUygulamaSesleri = staticCompositionLocalOf<SesHizmeti?> { null }
