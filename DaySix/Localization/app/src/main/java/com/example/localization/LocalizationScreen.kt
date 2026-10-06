package com.example.localization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.localization.ui.theme.LocalizationTheme

@Composable
fun LocalizationScreen(
    name: String,
    onNameChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = Modifier.fillMaxSize()) {innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.title),
                style = MaterialTheme.typography.headlineMedium
            )
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = {Text(stringResource(R.string.name_hint))},
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("name_input")
            )
            Text(
                text = stringResource(R.string.greeting, name),
                style = MaterialTheme.typography.titleMedium
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0, 1, 2, 5).forEach { count ->
                    Text(
                        text = if (count == 0) stringResource(R.string.zero)
                        else pluralStringResource(R.plurals.messages, count, count)
                    )
                }
            }
            listOf(
                "English" to "en",
                "Tiếng Việt" to "vi",
                "日本語" to "ja",
                stringResource(R.string.system) to ""
            ).forEach { (label, languageTag) ->
                Button(
                    onClick = { onLanguageChange(languageTag) },
                    modifier = Modifier.fillMaxWidth().testTag("language_$languageTag")
                ) {
                    Text(label)
                }
            }
            Text(
                text = stringResource(R.string.fallback),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(name = "English", showBackground = true, locale = "en")
@Preview(name = "Tiếng Việt", showBackground = true, locale = "vi")
@Preview(name = "日本語", showBackground = true, locale = "ja")
@Composable
private fun LocalizationScreenPreview() {
    var name by remember { mutableStateOf("Android") }

    LocalizationTheme(dynamicColor = false) {
        LocalizationScreen(
            name = name,
            onNameChange = { name = it },
            onLanguageChange = {}
        )
    }
}
