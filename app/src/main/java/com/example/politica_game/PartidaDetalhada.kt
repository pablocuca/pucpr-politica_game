package com.example.politica_game

data class PartidaDetalhada(
    val idPartida: Int,
    val dataPartida: String, 
    val nomeJogador: String,
    val nomeOponente: String,
    val nomeAtributo1: String,
    val nomeAtributo2: String,
    val nomeAtributo3: String,
    val pontosJogador: Int,
    val pontosOponente: Int,
    val nomeVencedor: String? 
)