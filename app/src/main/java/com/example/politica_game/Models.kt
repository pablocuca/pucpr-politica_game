
package com.example.politica_game

data class Candidato(
    val id: Int,
    val nomeOriginal: String,
    val nomeEngracado: String,
    val descricao: String?
)

data class Atributo(
    val id: Int,
    val nome: String,
    val tipo: String 
)

data class CandidatoAtributo(
    val candidatoId: Int,
    val atributoId: Int,
    val valor: Int
)

data class RankingItem(
    val candidatoId: Int,
    val pontuacaoTotal: Int
)

data class Partida(
    val id: Int = 0, 
    val dataPartida: String, 
    val candidatoJogadorId: Int,
    val candidatoOponenteId: Int,
    val atributo1Id: Int,
    val atributo2Id: Int,
    val atributo3Id: Int,
    val pontosJogador: Int,
    val pontosOponente: Int,
    val vencedorId: Int? 
)