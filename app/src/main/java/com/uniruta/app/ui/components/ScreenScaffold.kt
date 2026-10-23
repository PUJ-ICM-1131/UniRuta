package com.uniruta.app.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun UniRutaScreen(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    bottomBar: (@Composable ColumnScope.() -> Unit)? = null,
    floatingAction: @Composable () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    scrollState: ScrollState = rememberScrollState(),
    scrollEnabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 28.dp),
    verticalSpacing: Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { UniRutaTopBar(title = title, onBack = onBack) },
        bottomBar = { if (bottomBar != null) BottomActionBar(content = bottomBar) },
        floatingActionButton = floatingAction,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .verticalScroll(scrollState, enabled = scrollEnabled)
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
            content = content
        )
    }
}

@Composable
fun BottomActionBar(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}
