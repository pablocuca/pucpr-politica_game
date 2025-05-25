package com.example.politica_game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.politica_game.ui.theme.ThemeDefault


sealed class Screen {
    object Game : Screen()
    object Ranking : Screen()
    object Historico : Screen() 
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dbHelper = DatabaseHelper(this)
        val politicaRepository = PoliticaRepository(dbHelper)

        setContent {
            ThemeDefault {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Game) }

                when (currentScreen) {
                    Screen.Game -> GameScreen(
                        politicaRepository = politicaRepository,
                        onNavigateToRanking = { currentScreen = Screen.Ranking },
                        onNavigateToHistorico = { currentScreen = Screen.Historico } 
                    )
                    Screen.Ranking -> RankingScreen(
                        politicaRepository = politicaRepository,
                        onNavigateBack = { currentScreen = Screen.Game }
                    )
                    
                    Screen.Historico -> HistoricoScreen(
                        politicaRepository = politicaRepository,
                        onNavigateBack = { currentScreen = Screen.Game }
                    )
                }
            }
        }
    }
}