package com.example.notesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.notesapp.ui.theme.NotesAppTheme

class QuizPage : ComponentActivity() {
    // Override onCreate to set the content of the activity using a Composable function
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotesAppTheme{
                // This theme provides the overall styling for the app, which is also used for this quiz screen.
                val navController = rememberNavController()
                QuizScreen(navController)
            }
        }
    }
}

// This data class represents a single question in the quiz
data class Question(val text: String, val options: List<String>, val correctAnswer: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(navController: NavController) {
    // List of questions for the quiz
    val questions = listOf(
        Question("What is the largest land animal?", listOf("Elephant", "Giraffe", "Rhinoceros", "Hippopotamus"), 0),
        Question("Which animal is known as the King of the Jungle?", listOf("Tiger", "Lion", "Elephant", "Bear"), 1),
        Question("What is the fastest land animal?", listOf("Cheetah", "Lion", "Antelope", "Horse"), 0),
        Question("Which animal is known for its black and white stripes?", listOf("Zebra", "Tiger", "Penguin", "Panda"), 0),
        Question("What is the tallest animal in the world?", listOf("Elephant", "Giraffe", "Kangaroo", "Camel"), 1),
        Question("Which bird is often associated with delivering babies?", listOf("Stork", "Eagle", "Sparrow", "Owl"), 0),
        Question("What is the largest species of shark?", listOf("Great White Shark", "Hammerhead Shark", "Whale Shark", "Tiger Shark"), 2),
        Question("Which animal is known for its ability to change colors?", listOf("Chameleon", "Octopus", "Frog", "Snake"), 0),
        Question("What is the main diet of a Panda?", listOf("Fish", "Bamboo", "Insects", "Fruits"), 1),
        Question("Which mammal is known for having a pouch to carry its young?", listOf("Kangaroo", "Elephant", "Lion", "Wolf"), 0)
    )
    // State variables to track quiz progress
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableIntStateOf(-1) }
    var score by remember { mutableIntStateOf(0) }
    var showResult by remember { mutableStateOf(false) }
    // Scaffold provides a default layout with TopAppBar option
    Scaffold(
        modifier = Modifier.fillMaxSize(), // Utilize entire screen space
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth()) { // Horizontally center align the title
                        Text(
                            text = "Guess the Animal",
                            fontSize = 50.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)){ // Add padding to the content area
                // Show ResultScreen if showResult is true, otherwise show QuestionScreen
                if (showResult) {
                    ResultScreen(score, questions.size, navController) {
                        currentQuestionIndex = 0
                        selectedOptionIndex = -1
                        score = 0
                        showResult = false
                    }
                } else {
                    QuestionScreen(
                        question = questions[currentQuestionIndex],
                        selectedOptionIndex = selectedOptionIndex,
                        onOptionSelected = { selectedOptionIndex = it },
                        // Update score and navigate to next question or show results
                        onNextClicked = {
                            if (selectedOptionIndex == questions[currentQuestionIndex].correctAnswer) {
                                score++
                            }
                            if (currentQuestionIndex < questions.size - 1) {
                                currentQuestionIndex++
                                selectedOptionIndex = -1
                            } else {
                                showResult = true
                            }
                        }
                    )
                }
            }

        }
    )
}

@Composable
fun QuestionScreen(
    question: Question,
    selectedOptionIndex: Int,
    onOptionSelected: (Int) -> Unit,
    onNextClicked: () -> Unit
) {
    Column(
        // Modifiers for layout
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Display question text
        Text(text = question.text, style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        // Display radio buttons for each option with selection handling
        question.options.forEachIndexed { index, option ->
            Row(
                // Modifiers for layout
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedOptionIndex == index,
                    onClick = { onOptionSelected(index) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = option)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Button to navigate to next question, disabled if no option selected
        Button(
            onClick = onNextClicked,
            enabled = selectedOptionIndex != -1
        ) {
            Text("Next")
        }
    }
}

@Composable
fun ResultScreen(score: Int, totalQuestions: Int, navController: NavController, onRestartClicked: () -> Unit) {
    Column(
        // Modifiers for layout
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Display quiz completion message
        Text(text = "Quiz Finished!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        // Display score
        Text(text = "Your score: $score/$totalQuestions", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        // Buttons to restart quiz or return home
        Button(onClick = onRestartClicked) {
            Text("Restart Quiz")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { navController.navigate("home") }) {
            Text("Return Home")
        }
    }
}