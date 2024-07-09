package com.example.notesapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.notesapp.ui.theme.NotesAppTheme

class MainActivity : ComponentActivity() {
    private lateinit var dbHelper: NotesDatabaseHelper
    // Override onCreate to set the content of the activity using a Composable function
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enable edge-to-edge mode
        dbHelper = NotesDatabaseHelper(this) // Initialize the database helper
        setContent {
            // This theme provides the overall styling for the app
            NotesAppTheme {
                MyApp(dbHelper) // The main application composable
            }
        }
    }
}
// Composable function for the main app structure
@Composable
fun MyApp(dbHelper: NotesDatabaseHelper){
    // Create a navigation controller to handle app navigation
    val navController = rememberNavController()
    // Navigation host to handle navigation between screens
    NavHost(navController, startDestination = "home"){
        composable("home"){ HomeScreen(navController, dbHelper)} // Home screen route
        composable("add_note"){ AddNoteScreen(navController, dbHelper)} // Add note screen route
        composable("take_quiz"){ QuizScreen(navController)} // Quiz screen route
        composable("edit_note/{noteId}/{title}/{content}",
            arguments = listOf(
                navArgument("noteId") { type = NavType.LongType }, // noteId argument with Long type
                navArgument("title") { type = NavType.StringType }, // title argument with String type
                navArgument("content") { type = NavType.StringType } // content argument with String type
            )
        ) { backStackEntry -> // Access the back stack entry to retrieve arguments
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L // Retrieve noteId from arguments, default to 0 if not found
            val title = backStackEntry.arguments?.getString("title") ?: "" // Retrieve title from arguments, default to empty string if not found
            val content = backStackEntry.arguments?.getString("content") ?: "" // Retrieve content from arguments, default to empty string if not found
            EditNoteScreen(navController, dbHelper, noteId, title, content) // Call the EditNoteScreen composable with retrieved arguments
        } // Edit note screen route with arguments
    }
}
// Composable function for the Home screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController,dbHelper: NotesDatabaseHelper) {
    val context = LocalContext.current // Get the current context for displaying Toast messages
    val (notes, setNotes) = remember { mutableStateOf(emptyList<Note>()) } // State to hold the list of notes
    // Load the notes from the database when the composable is first launched
    LaunchedEffect(Unit) {
        val cursor = dbHelper.getAllNotes()
        val notesList = mutableListOf<Note>()
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(NotesDatabaseHelper.COLUMN_ID))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(NotesDatabaseHelper.COLUMN_TITLE))
            val content = cursor.getString(cursor.getColumnIndexOrThrow(NotesDatabaseHelper.COLUMN_CONTENT))
            notesList.add(Note(id, title, content))
        }
        cursor.close()
        setNotes(notesList)
    }
    // Scaffold provides a default layout with TopAppBar and FloatingActionButton options
    Scaffold(
        modifier = Modifier.fillMaxSize(), // Utilize entire screen space
        topBar = {
            TopAppBar(
                title = { Text(text= "Notes", fontSize = 50.sp, fontWeight = FontWeight.Bold)}, // Display "Notes" title
                actions = {
                    Text(
                        text = "Quiz",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable{ navController.navigate("take_quiz") } // Navigate to quiz screen on click
                    )
                }
            )
        },
        // Button to add new note
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_note") }, // Navigate to add note screen on click
                modifier = Modifier.padding(10.dp)
            ) {
                Icon(Icons.Filled.AddCircle, contentDescription = "Add Note") // Display add note icon
            }
        },
        // Main content of the screen
        content = { innerPadding ->
            if(notes.isEmpty()){
                // Display Empty Notes message when there are no notes
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ){
                    Text(
                        text = "Empty Notes",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else{
                // Display the list of notes using LazyColumn
                LazyColumn(
                    modifier = Modifier.padding(innerPadding)
                ) {
                    items(notes) { note ->
                        NoteItem(
                            title = note.title,
                            content = note.content,
                            // Navigate to the edit note screen on click
                            onClick = {
                                val route = "edit_note/${note.id}/${note.title}/${note.content}"
                                navController.navigate(route)
                            },
                            // Handle delete note action
                            onDeleteClick = {
                                dbHelper.deleteNote(note.id)
                                // Update the list of notes after deletion
                                val cursor = dbHelper.getAllNotes()
                                val updatedNotesList = mutableListOf<Note>()
                                while (cursor.moveToNext()) {
                                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(NotesDatabaseHelper.COLUMN_ID))
                                    val title = cursor.getString(cursor.getColumnIndexOrThrow(NotesDatabaseHelper.COLUMN_TITLE))
                                    val content = cursor.getString(cursor.getColumnIndexOrThrow(NotesDatabaseHelper.COLUMN_CONTENT))
                                    updatedNotesList.add(Note(id, title, content))
                                }
                                cursor.close()
                                setNotes(updatedNotesList)
                                // Show a Toast message indicating note deletion
                                Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    )
}
// Composable function to display a single note item in a Card UI
@Composable
fun NoteItem(title: String, content: String, onClick: () -> Unit, onDeleteClick: () -> Unit) {
    // Card composable to display the note item with elevation and clickable behavior
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick), // Make the card clickable
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box { // Box composable to hold content and delete icon
            Column( // Column to arrange text content vertically
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold) // Display title text with bold font
                Spacer(modifier = Modifier.height(4.dp)) // Spacer to create space between title and content
                Text(text = content, fontSize = 16.sp) // Display content text with smaller font size
            }
            Icon( // Icon button to delete the note, positioned at the top end corner
                Icons.Filled.Delete,
                contentDescription = "Delete",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clickable(onClick = onDeleteClick) // Make the icon clickable to delete the note
            )
        }
    }
}
// Data class to represent a Note with id, title, and content
data class Note(
    val id: Long, // Unique identifier for the note
    val title: String, // Title of the note
    val content: String // Content or description of the note
)
// Composable function for the AddNote screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(navController: NavController, dbHelper: NotesDatabaseHelper) {
    // State variables to store note title and content entered by the user
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val context = LocalContext.current // Get the current context for displaying Toast messages
    // Scaffold provides a default layout with TopAppBar and FloatingActionButton options
    Scaffold(
        modifier = Modifier.fillMaxSize(), // Utilize entire screen space
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Note",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ) // Display "Add Note" title in the app bar
                }
            )
        },
        // Button to save note
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Check if the note title and content is provided
                    if(title.isNotBlank() && content.isNotBlank()){
                        dbHelper.addNote(title, content) // Insert the note into the database
                        Toast.makeText(context, "Note created", Toast.LENGTH_SHORT).show() // Show a success message
                        navController.popBackStack() // Navigate back to the previous screen
                    } else{
                        // Show a message if title or content is empty
                        Toast.makeText(context, "Title and content cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.padding(10.dp) // Add padding to the button
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = "Add Note") // Display checkmark icon
            }
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding) // Add padding around the content
                    .verticalScroll(rememberScrollState()) // Enable vertical scrolling
                    .fillMaxSize() // Utilize entire screen space for the Column
            ) {
                // Text field for entering note title
                TextField(
                    value = title,
                    onValueChange = { title = it }, // Set the value to what the user entered
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth() // Occupy full width available
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Text field for entering note content
                TextField(
                    value = content,
                    onValueChange = { content = it }, // Set the value to what the user entered
                    label = { Text("Take a note...") },
                    modifier = Modifier
                        .fillMaxWidth() // Occupy full width available
                )
                Spacer(modifier = Modifier.height(16.dp)) // Add spacing after content field
            }
        }
    )
}
// Composable function for the EditNote screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(navController: NavController, dbHelper: NotesDatabaseHelper, noteId: Long, initialTitle: String, initialContent: String){
    // Mutable state variables to hold the current title and content of the note
    var title by remember { mutableStateOf(initialTitle) }
    var content by remember { mutableStateOf(initialContent) }
    val context = LocalContext.current // Get the current context for displaying Toast messages
    // Scaffold provides a default layout with TopAppBar and FloatingActionButton options
    Scaffold(
        // Modifiers for layout
        modifier = Modifier.fillMaxSize(), // Utilize entire screen space
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "View/Edit Note",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ) // Display "Edit Note" title in the app bar
                }
            )
        },
        // Button to save note
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Check if the note title and content is provided
                    if(title.isNotBlank() && content.isNotBlank()){
                        dbHelper.editNote(noteId, title, content) // Update the note
                        Toast.makeText(context, "Note updated", Toast.LENGTH_SHORT).show()
                        navController.popBackStack() // Navigate back to the previous screen
                    } else{
                        // Show a message if title or content is empty
                        Toast.makeText(context, "Title and content cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.padding(10.dp) // Add padding to the button
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = "Save Note") // Display the checkmark icon
            }
        },
        // Main content of the screen
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding) // Add padding around the content
                    .verticalScroll(rememberScrollState()) // Enable vertical scrolling
                    .fillMaxSize() // Utilize entire screen space for the Column
            ) {
                // Text field for entering note title
                TextField(
                    value = title, // Show the note title
                    onValueChange = { title = it }, // Update the title when text changes
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth() // Occupy full width available
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Text field for entering note content
                TextField(
                    value = content, // Show the note content
                    onValueChange = { content = it }, // Update the content when text changes
                    label = { Text("Take a note...") },
                    modifier = Modifier
                        .fillMaxWidth() // Occupy full width available
                )
                Spacer(modifier = Modifier.height(16.dp)) // Add spacing after content field
            }
        }
    )
}
