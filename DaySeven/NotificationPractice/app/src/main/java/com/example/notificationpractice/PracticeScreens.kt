package com.example.notificationpractice


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// UI chỉ nhận state/callback; việc gửi notification thuộc Activity và helper.
data class MainUiState(
    val notificationsEnabled: Boolean = false,
    val canRequestPermission: Boolean = true,
    val isARead: Boolean = false,
    val isBRead: Boolean = false,
)

@Composable
fun PracticeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
        Surface(modifier = Modifier.fillMaxSize(), content = content)
    }
}

@Composable
private fun ScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding()
            .verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

@Composable
fun MessageListScreen(
    state: MainUiState,
    brokenMode: Boolean,
    onBrokenModeChange: (Boolean) -> Unit,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onPostMessage: (DemoMessage) -> Unit,
    onReset: () -> Unit,
) {
    ScreenColumn {
        Text(stringResource(R.string.screen_title), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.screen_intro))
        Text(stringResource(if (state.notificationsEnabled) R.string.permission_enabled else R.string.permission_disabled))
        Button(
            onClick = onRequestPermission,
            enabled = state.canRequestPermission && !state.notificationsEnabled,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.permission_request)) }
        OutlinedButton(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.permission_settings))
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.broken_mode), modifier = Modifier.weight(1f))
            Switch(checked = brokenMode, onCheckedChange = onBrokenModeChange, modifier = Modifier.testTag("brokenMode"))
        }
        Text(stringResource(if (brokenMode) R.string.mode_broken else R.string.mode_safe))
        MessageCard(DemoMessages.a, state.isARead, R.string.notify_a, onPostMessage)
        MessageCard(DemoMessages.b, state.isBRead, R.string.notify_b, onPostMessage)
        OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.reset))
        }
    }
}

@Composable
private fun MessageCard(message: DemoMessage, isRead: Boolean, buttonRes: Int, onPost: (DemoMessage) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(message.titleRes), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(message.bodyRes))
            Text(
                stringResource(R.string.message_status, message.id, stringResource(if (isRead) R.string.read else R.string.unread)),
                modifier = Modifier.testTag("status${message.id}"),
            )
            Button(onClick = { onPost(message) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(buttonRes))
            }
        }
    }
}

@Composable
fun MessageDetailScreen(messageId: String?, onBackToList: () -> Unit) {
    val message = DemoMessages.find(messageId)
    ScreenColumn {
        Text(stringResource(R.string.detail_title), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(R.string.detail_id, messageId ?: stringResource(R.string.unknown_id)),
            modifier = Modifier.testTag("messageId"),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(stringResource(message?.titleRes ?: R.string.unknown_message), style = MaterialTheme.typography.titleLarge)
        if (message != null) Text(stringResource(message.bodyRes))
        Text(stringResource(R.string.detail_source))
        Button(onClick = onBackToList, modifier = Modifier.fillMaxWidth().testTag("backToList")) {
            Text(stringResource(R.string.back_to_list))
        }
    }
}

@Preview(showBackground = true, locale = "vi")
@Composable
private fun MessageListPreview() {
    PracticeTheme {
        MessageListScreen(MainUiState(notificationsEnabled = true), false, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true, locale = "vi")
@Composable
private fun MessageDetailPreview() {
    PracticeTheme { MessageDetailScreen("A", {}) }
}