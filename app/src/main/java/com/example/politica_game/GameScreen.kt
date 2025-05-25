package com.example.politica_game

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@Composable
fun GameScreen(
    politicaRepository: PoliticaRepository,
    onNavigateToRanking: () -> Unit,
    onNavigateToHistorico: () -> Unit 
) {
    
    val atributos = remember { mutableStateListOf<Atributo>() }
    val selectedAtributos = remember { mutableStateListOf<Atributo>() }
    var candidatoJogador by remember { mutableStateOf<Candidato?>(null) }
    var candidatoOponente by remember { mutableStateOf<Candidato?>(null) }
    var showCandidatoRoleta by remember { mutableStateOf(false) }
    var showResultadosPartida by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var resultadoPartida by remember { mutableStateOf("") }
    var lastGamePontosJogador by remember { mutableStateOf(0) }
    var lastGamePontosOponente by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        atributos.clear()
        val loadedAtributos = politicaRepository.getAllAtributos()
        atributos.addAll(loadedAtributos)
        Log.d("GameScreen", "LaunchedEffect: Carregou ${atributos.size} atributos.")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(top = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Política Game",
                fontSize = 22.sp,
                modifier = Modifier.weight(1f)
            )
            
            Button(onClick = onNavigateToHistorico) { 
                Text("Histórico")
            }
            Spacer(modifier = Modifier.width(8.dp)) 
            Button(onClick = onNavigateToRanking) {
                Text("Ranking")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        
        

        Text(
            text = "Selecione 3 Atributos (1+ Positivo, 1+ Negativo):",
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(atributos) { atributo ->
                val isSelected = selectedAtributos.contains(atributo)
                AtributoSelectionItem(
                    atributo = atributo,
                    isSelected = isSelected,
                    onAttributeClick = {
                        if (isSelected) {
                            selectedAtributos.remove(atributo)
                        } else {
                            if (selectedAtributos.size < 3) {
                                selectedAtributos.add(atributo)
                            } else {
                                message = "Você já selecionou 3 atributos!"
                            }
                        }
                        message = ""
                    }
                )
            }
        }

        
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Atributos Selecionados:", fontSize = 16.sp, color = Color.Gray)
            Text(text = selectedAtributos.joinToString(", ") { it.nome }, fontSize = 14.sp)

            if (message.isNotEmpty()) {
                Text(text = message, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }

            
            if (showCandidatoRoleta && candidatoJogador != null && !showResultadosPartida) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Seu Candidato:", fontSize = 20.sp)
                CandidatoItem(candidato = candidatoJogador!!)
                Button(
                    onClick = {
                        coroutineScope.launch {
                            var oponente: Candidato? = null
                            val todosCandidatos = politicaRepository.getAllCandidatos()
                            if (todosCandidatos.size > 1) {
                                do {
                                    oponente = politicaRepository.getRandomCandidato()
                                } while (oponente?.id == candidatoJogador?.id)
                            } else {
                                message = "Não há oponentes suficientes para jogar!"
                                return@launch
                            }

                            candidatoOponente = oponente

                            if (candidatoOponente != null) {
                                var pontosJogador = 0
                                var pontosOponente = 0
                                val atrIds = selectedAtributos.map { it.id }
                                val jogadorId = candidatoJogador!!.id
                                val oponenteId = candidatoOponente!!.id

                                for (atr in selectedAtributos) {
                                    val valJogador = politicaRepository.getCandidatoAttributeValue(jogadorId, atr.id)
                                    val valOponente = politicaRepository.getCandidatoAttributeValue(oponenteId, atr.id)

                                    if (atr.tipo == "Positivo") {
                                        if (valJogador > valOponente) pontosJogador++
                                        else if (valOponente > valJogador) pontosOponente++
                                    } else { 
                                        if (valJogador < valOponente) pontosJogador++
                                        else if (valOponente < valJogador) pontosOponente++
                                    }
                                }

                                val vencedorId: Int? = when {
                                    pontosJogador >= 2 && pontosJogador > pontosOponente -> jogadorId
                                    pontosOponente >= 2 && pontosOponente > pontosJogador -> oponenteId
                                    else -> null
                                }

                                val partida = Partida(
                                    dataPartida = LocalDateTime.now().toString(),
                                    candidatoJogadorId = jogadorId,
                                    candidatoOponenteId = oponenteId,
                                    atributo1Id = atrIds[0],
                                    atributo2Id = atrIds[1],
                                    atributo3Id = atrIds[2],
                                    pontosJogador = pontosJogador,
                                    pontosOponente = pontosOponente,
                                    vencedorId = vencedorId
                                )
                                politicaRepository.insertPartida(partida)

                                if (vencedorId != null) {
                                    politicaRepository.updateRanking(vencedorId, 1)
                                    resultadoPartida = if (vencedorId == jogadorId) "Você Venceu!" else "Você Perdeu!"
                                } else {
                                    resultadoPartida = "Empate!"
                                }
                                message = ""
                                lastGamePontosJogador = pontosJogador
                                lastGamePontosOponente = pontosOponente
                                showCandidatoRoleta = false
                                showResultadosPartida = true
                            } else {
                                message = "Erro ao selecionar oponente."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("Jogar!")
                }
            }

            
            if (showResultadosPartida && candidatoJogador != null && candidatoOponente != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = resultadoPartida, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Blue)
                Text(text = "Seu Candidato: ${candidatoJogador!!.nomeEngracado}", fontSize = 16.sp)
                Text(text = "Oponente: ${candidatoOponente!!.nomeEngracado}", fontSize = 16.sp)
                Text(text = "Placar: $lastGamePontosJogador x $lastGamePontosOponente", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Button(
                    onClick = {
                        selectedAtributos.clear()
                        candidatoJogador = null
                        candidatoOponente = null
                        showCandidatoRoleta = false
                        showResultadosPartida = false
                        message = ""
                        resultadoPartida = ""
                        lastGamePontosJogador = 0
                        lastGamePontosOponente = 0
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Jogar Novamente")
                }
            }

            
            if (!showCandidatoRoleta && !showResultadosPartida) {
                Button(
                    onClick = {
                        val positivos = selectedAtributos.count { it.tipo == "Positivo" }
                        val negativos = selectedAtributos.count { it.tipo == "Negativo" }

                        if (selectedAtributos.size == 3 && positivos >= 1 && negativos >= 1) {
                            message = "Girando a roleta..."
                            showCandidatoRoleta = false
                            candidatoJogador = null
                            coroutineScope.launch {
                                delay(1500)
                                candidatoJogador = politicaRepository.getRandomCandidato()
                                showCandidatoRoleta = true
                                message = ""
                            }
                        } else {
                            message = "Selecione EXATAMENTE 3 atributos (1+ Positivo, 1+ Negativo)."
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    enabled = selectedAtributos.size == 3 &&
                            selectedAtributos.any { it.tipo == "Positivo" } &&
                            selectedAtributos.any { it.tipo == "Negativo" }
                ) {
                    Text("Girar Roleta")
                }
            }
        }

    }
}

@Composable
fun AtributoSelectionItem(
    atributo: Atributo,
    isSelected: Boolean,
    onAttributeClick: (Atributo) -> Unit
) {
    val positiveTextColor = Color(0xFF2E7D32)
    val negativeTextColor = Color(0xFFC62828)
    val selectedBackgroundColor = Color(0xFFDCEDC8)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAttributeClick(atributo) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> selectedBackgroundColor
                atributo.tipo == "Positivo" -> Color(0xFFE8F5E9)
                else -> Color(0xFFFFEBEE)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = atributo.nome,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (atributo.tipo == "Positivo") positiveTextColor else negativeTextColor
            )
            Text(
                text = "(${atributo.tipo})",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun CandidatoItem(candidato: Candidato) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = candidato.nomeEngracado, fontSize = 18.sp)
            Text(text = candidato.descricao ?: "Sem descrição.", fontSize = 12.sp, color = Color.DarkGray)
        }
    }
}