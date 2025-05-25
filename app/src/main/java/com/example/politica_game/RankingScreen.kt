package com.example.politica_game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.* 
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class) 
@Composable
fun RankingScreen(
    politicaRepository: PoliticaRepository,
    onNavigateBack: () -> Unit 
) {
    val ranking = remember { mutableStateListOf<RankingEntry>() }

    
    LaunchedEffect(Unit) {
        ranking.clear()
        ranking.addAll(politicaRepository.getRanking())
    }

    Scaffold( 
        topBar = {
            TopAppBar(
                title = { Text("Ranking dos Candidatos") },
                
                
                
                
                
                
                
                actions = { 
                    Button(onClick = onNavigateBack, modifier = Modifier.padding(end = 8.dp)) {
                        Text("Voltar")
                    }
                }
            )
        }
    ) { paddingValues -> 
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) 
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (ranking.isEmpty()) {
                Text("O ranking ainda está vazio. Jogue algumas partidas!", fontSize = 18.sp)
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(ranking) { index, entry ->
                        RankingItem(position = index + 1, entry = entry)
                        Divider() 
                    }
                }
            }
        }
    }
}

@Composable
fun RankingItem(position: Int, entry: RankingEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$position.",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(40.dp) 
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = entry.nomeEngracado,
                fontSize = 18.sp
            )
        }
        Text(
            text = "${entry.pontuacao} Pts",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Blue
        )
    }
}