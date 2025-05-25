package com.example.politica_game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricoScreen(
    politicaRepository: PoliticaRepository,
    onNavigateBack: () -> Unit 
) {
    val historico = remember { mutableStateListOf<PartidaDetalhada>() }

    
    LaunchedEffect(Unit) {
        historico.clear()
        historico.addAll(politicaRepository.getHistoricoPartidas())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico de Partidas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp), 
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (historico.isEmpty()) {
                Text("Nenhuma partida foi jogada ainda.", fontSize = 18.sp)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp) 
                ) {
                    items(historico) { partida ->
                        PartidaItem(partida = partida)
                    }
                }
            }
        }
    }
}

@Composable
fun PartidaItem(partida: PartidaDetalhada) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${partida.nomeJogador} vs ${partida.nomeOponente}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f) 
                )
                Text(
                    text = "${partida.pontosJogador} x ${partida.pontosOponente}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary 
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            
            Text(
                text = "Vencedor: ${partida.nomeVencedor ?: "Empate"}",
                fontSize = 16.sp,
                color = if (partida.nomeVencedor != null) Color(0xFF2E7D32) else Color.Gray, 
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            
            Text("Atributos Disputados:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text("- ${partida.nomeAtributo1}", fontSize = 13.sp, color = Color.DarkGray)
            Text("- ${partida.nomeAtributo2}", fontSize = 13.sp, color = Color.DarkGray)
            Text("- ${partida.nomeAtributo3}", fontSize = 13.sp, color = Color.DarkGray)

            Spacer(modifier = Modifier.height(8.dp))

            
            Text(
                text = formatarData(partida.dataPartida),
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@Composable
private fun formatarData(dataString: String): String {
    return try {
        val ldt = LocalDateTime.parse(dataString)
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        ldt.format(formatter)
    } catch (e: Exception) {
        dataString 
    }
}