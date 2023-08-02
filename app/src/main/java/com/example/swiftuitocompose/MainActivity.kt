package com.example.swiftuitocompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

data class Task(
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val description: String
)

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp() {
    val tasks = remember {
        mutableStateListOf(
            Task(title = "Tarefa 1", description = "Descrição da Tarefa 1"),
            Task(title = "Tarefa 2", description = "Descrição da Tarefa 2"),
            Task(title = "Tarefa 3", description = "Descrição da Tarefa 3")
        )
    }

    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var newTask by remember { mutableStateOf<Task?>(null) }
    var expandedTask by remember { mutableStateOf<Task?>(null) }
    var deletedTaskId by remember { mutableStateOf<UUID?>(null) }

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "taskList") {
        composable("taskList") {
            Surface(color = MaterialTheme.colorScheme.background) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    TopAppBar(
                        title = {
                            Text("Lista de Tarefas")
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    selectedTask = null
                                    newTask = Task(title = "", description = "")
                                    // Navegue para a tela de adição usando o navController
                                    navController.navigate("addTask")
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                            }
                        },
                        colors = TopAppBarDefaults.smallTopAppBarColors(
                            containerColor = Color(0, 158, 229)
                        )
                    )

                    LazyColumn {
                        items(tasks) { task ->
                            TaskRow(
                                task = task,
                                expandedTask = expandedTask,
                                onClick = { clickedTask, showDescription ->
                                    selectedTask = if (showDescription) task else null
                                    expandedTask = if (showDescription) clickedTask else null
                                },
                                navController = navController,
                                onDelete = { taskToDelete ->
                                    tasks.remove(taskToDelete)
                                    expandedTask = null
                                    deletedTaskId = taskToDelete.id
                                }
                            )
                        }
                    }
                }
            }
        }
        composable(
            route = "taskDetail/{taskId}"
        ) { backStackEntry ->
            val taskId = UUID.fromString(backStackEntry.arguments?.getString("taskId") ?: "")
            val task = tasks.find { it.id == taskId }
            if (task != null) {
                TaskDetailScreen(
                    task = task,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        // Adicione a composable para exibir a tela de adição como uma folha modal
        composable("addTask") {
            newTask?.let { task ->
                AddTaskScreen(
                    newTask = task,
                    onTaskAdded = { addedTask ->
                        if (!addedTask.title.isEmpty()) {
                            tasks.add(addedTask)
                        }
                        // Feche a tela de adição usando o navController
                        navController.popBackStack()
                    },
                    onDismiss = {
                        // Feche a tela de adição usando o navController
                        navController.popBackStack()
                    },
                    isPresented = true
                )
            }
        }
    }
}

@Composable
fun TaskRow(
    task: Task,
    expandedTask: Task?,
    onClick: (Task, Boolean) -> Unit,
    navController: NavController,
    onDelete: (Task) -> Unit
) {
    val isExpanded = task == expandedTask
    var showDescription by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick(task, showDescription)
            }
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        showDescription = !showDescription
                    }
            )
        }

        if (showDescription) {
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Button(
                onClick = {
                    onClick(task, false)
                    navController.navigate("taskDetail/${task.id}")
                },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0,158,229),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(2.dp),

            ) {
                Text("Ver mais")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = {
                    // Navegação para a tela de detalhes ao clicar no botão "Ver mais"
                    onDelete(task)
                    if (isExpanded) {
                        onClick(task, false)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(229, 0, 31, 0),
                    contentColor = Color(150, 150, 150)
                ),
                shape = RoundedCornerShape(2.dp),
                border = BorderStroke(1.dp, Color(150, 150, 150))

                ) {
                Text("Excluir")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(task: Task, onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Text("Detalhes da Tarefa")
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.DarkGray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    newTask: Task?,
    isPresented: Boolean,
    onTaskAdded: (Task) -> Unit,
    onDismiss: () -> Unit
) {
    if (isPresented) {
        var title by remember { mutableStateOf(TextFieldValue(newTask?.title ?: "")) }
        var description by remember { mutableStateOf(TextFieldValue(newTask?.description ?: "")) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Adicionar Tarefa",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                },
                label = { Text("Título") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                },
                label = { Text("Descrição") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        // Fechar a tela de adição
                        onDismiss()
                    }
                ) {
                    Text("Cancelar")
                }

                Spacer(modifier = Modifier.width(8.dp))

                TextButton(
                    onClick = {
                        // Adicionar a nova tarefa à lista e fechar a tela de adição
                        onTaskAdded(Task(title = title.text, description = description.text))
                    }
                ) {
                    Text("Salvar")
                }
            }
        }
    }
}

