package com.cst2335.teamprojectapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // we would need a helper
        val dbHelper = DatabaseHelper(this)

        setContent {
            val viewModel: ItemViewModel = viewModel(factory = ItemViewModelFactory(dbHelper))
            AppScreen(viewModel)
        }
    }
}

//let's do a composable
@Composable
fun AppScreen(viewModel: ItemViewModel) {
    val items by viewModel.items.collectAsState()
    val isDialogVisible by remember { mutableStateOf(viewModel.isDialogVisible.value) }
    val isRefreshing = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val progress = remember { mutableStateOf(0f) }
    val isProgressBarVisible = remember { mutableStateOf(false) }

    // We need app bar at top with a refresh button to refresh the item list
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Item List") },
                actions = {
                    IconButton(onClick = {
                        isRefreshing.value = true
                        viewModel.refreshItems {
                            isRefreshing.value = false
                        }
                    }) {
                        if (isRefreshing.value) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddItemDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { padding ->

        //we need items we can delete

        Box(Modifier.padding(padding)) {
            Column {
                // List of items
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(items, key = { it.id }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable { viewModel.deleteItem(item.id) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.name,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Tap to delete",
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // One of requirements - progress bar
                if (isProgressBarVisible.value) {
                    LinearProgressIndicator(progress = progress.value, modifier = Modifier.fillMaxWidth())
                }

                //And button, of course
                Button(
                    onClick = {
                        isProgressBarVisible.value = true
                        progress.value = 0f
                        coroutineScope.launch {
                            repeat(10) {
                                delay(300)
                                progress.value += 0.1f
                            }
                            isProgressBarVisible.value = false
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
                ) {
                    Text("Start Progress")
                }
            }

            // If we would need a dialogue of Add Item
            if (isDialogVisible) {
                AddItemDialog(
                    onAdd = { name ->
                        viewModel.addItem(name)
                        viewModel.hideAddItemDialog()
                    },
                    onDismiss = { viewModel.hideAddItemDialog() }
                )
            }
        }
    }
}
