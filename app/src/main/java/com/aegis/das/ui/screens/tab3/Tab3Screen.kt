package com.aegis.das.ui.screens.tab3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.aegis.das.domain.state.AppState
import com.aegis.das.domain.state.LogEntry
import com.aegis.das.domain.state.LogEntryType
import com.aegis.das.ui.components.GlassCard
import kotlinx.coroutines.delay

@Composable
fun Tab3Screen(state: AppState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Thinking Log", style = MaterialTheme.typography.headlineSmall)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Debug: ${state.debugMode}", style = MaterialTheme.typography.labelMedium)
            Text("Processor: ${state.processor}", style = MaterialTheme.typography.labelMedium)
            Text("Severity: ${state.lastInference.severity}", style = MaterialTheme.typography.labelMedium)
        }

        ThoughtBubble(
            entries = state.logEntries,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        JsonTimeline(
            entries = state.logEntries,
            showSystem = state.debugMode,
            modifier = Modifier
                .fillMaxSize()
        )
    }
}

@Composable
private fun ThoughtBubble(
    entries: List<LogEntry>,
    modifier: Modifier = Modifier
) {
    val thoughts = remember(entries) {
        entries.filter { it.type == LogEntryType.THOUGHT && !it.message.isNullOrBlank() }
    }

    val latest = thoughts.lastOrNull()?.message.orEmpty()
    val previous = if (thoughts.size > 1) thoughts.dropLast(1).takeLast(3) else emptyList()

    var visibleChars by remember(latest) { mutableIntStateOf(0) }
    LaunchedEffect(latest) {
        visibleChars = 0
        while (visibleChars < latest.length) {
            visibleChars += 1
            delay(18)
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Thought", style = MaterialTheme.typography.titleMedium)

        if (latest.isBlank()) {
            Text("No thought yet.", style = MaterialTheme.typography.bodyMedium)
        } else {
            if (previous.isNotEmpty()) {
                previous.asReversed().forEach { entry ->
                    Text(
                        text = entry.message.orEmpty(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = latest.take(visibleChars.coerceAtMost(latest.length)),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun JsonTimeline(
    entries: List<LogEntry>,
    showSystem: Boolean,
    modifier: Modifier = Modifier
) {
    val visibleEntries = remember(entries, showSystem) {
        entries.filter {
            when (it.type) {
                LogEntryType.SYSTEM -> showSystem
                else -> it.type != LogEntryType.THOUGHT
            }
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Timeline", style = MaterialTheme.typography.titleMedium)

        if (visibleEntries.isEmpty()) {
            Text("No logs yet.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(visibleEntries, key = { it.id }) { entry ->
                    TimelineItem(entry)
                }
            }
        }
    }
}

@Composable
private fun TimelineItem(entry: LogEntry) {
    val header = buildString {
        append(entry.type.name)
        append(" • ")
        append(entry.timestamp)
        if (!entry.message.isNullOrBlank()) {
            append(" • ")
            append(entry.message)
        }
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = Modifier.padding(12.dp)
    ) {
        Text(header, style = MaterialTheme.typography.labelMedium)
        if (entry.payload != null) {
            Text(
                text = JsonFormatter.format(entry.payload),
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
            )
        }
    }
}
