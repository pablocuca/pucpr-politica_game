package com.example.politica_game

import android.content.ContentValues
import android.database.Cursor

class PoliticaRepository(private val dbHelper: DatabaseHelper) {

    fun getAllAtributos(): List<Atributo> {
        val atributos = mutableListOf<Atributo>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_ATRIBUTOS,
            arrayOf(DatabaseHelper.COL_ATR_ID, DatabaseHelper.COL_ATR_NOME, DatabaseHelper.COL_ATR_TIPO),
            null, null, null, null, null
        )
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.COL_ATR_ID))
                val nome = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COL_ATR_NOME))
                val tipo = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COL_ATR_TIPO))
                atributos.add(Atributo(id, nome, tipo))
            }
        }
        return atributos
    }

    fun getAllCandidatos(): List<Candidato> {
        val candidatos = mutableListOf<Candidato>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_CANDIDATOS,
            arrayOf(DatabaseHelper.COL_CAND_ID, DatabaseHelper.COL_CAND_NOME_ORIGINAL, DatabaseHelper.COL_CAND_NOME_ENGRACADO, DatabaseHelper.COL_CAND_DESCRICAO),
            null, null, null, null, null
        )
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.COL_CAND_ID))
                val nomeOriginal = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COL_CAND_NOME_ORIGINAL))
                val nomeEngracado = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COL_CAND_NOME_ENGRACADO))
                val descricao = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COL_CAND_DESCRICAO))
                candidatos.add(Candidato(id, nomeOriginal, nomeEngracado, descricao))
            }
        }
        return candidatos
    }

    fun getRandomCandidato(): Candidato? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_CANDIDATOS} ORDER BY RANDOM() LIMIT 1", null)
        var candidato: Candidato? = null
        cursor.use {
            if (it.moveToFirst()) {
                val id = it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.COL_CAND_ID))
                val nomeOriginal = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COL_CAND_NOME_ORIGINAL))
                val nomeEngracado = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COL_CAND_NOME_ENGRACADO))
                val descricao = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COL_CAND_DESCRICAO))
                candidato = Candidato(id, nomeOriginal, nomeEngracado, descricao)
            }
        }
        return candidato
    }

    fun getRanking(): List<RankingEntry> {
        val db = dbHelper.readableDatabase
        val rankingList = mutableListOf<RankingEntry>()
        val query = """
            SELECT r.id_candidato, c.nome_engracado, r.pontuacao_total
            FROM ${DatabaseHelper.TABLE_RANKING} r
            JOIN ${DatabaseHelper.TABLE_CANDIDATOS} c ON r.${DatabaseHelper.COL_RANK_CAND_ID} = c.${DatabaseHelper.COL_CAND_ID}
            ORDER BY r.${DatabaseHelper.COL_RANK_PONTUACAO} DESC
        """.trimIndent()
        val cursor = db.rawQuery(query, null)
        cursor.use {
            while (it.moveToNext()) {
                val idCandidatoIndex = it.getColumnIndexOrThrow("id_candidato")
                val nomeEngracadoIndex = it.getColumnIndexOrThrow("nome_engracado")
                val pontuacaoTotalIndex = it.getColumnIndexOrThrow("pontuacao_total")
                val entry = RankingEntry(
                    idCandidato = it.getInt(idCandidatoIndex), 
                    nomeEngracado = it.getString(nomeEngracadoIndex),
                    pontuacao = it.getInt(pontuacaoTotalIndex)
                )
                rankingList.add(entry)
            }
        }
        return rankingList
    }

    fun getCandidatoAttributeValue(candidatoId: Int, atributoId: Int): Int { 
        val db = dbHelper.readableDatabase
        var value = 0 
        val cursor = db.query(
            DatabaseHelper.TABLE_CANDIDATOS_ATRIBUTOS,
            arrayOf(DatabaseHelper.COL_CA_VALOR),
            "${DatabaseHelper.COL_CA_CAND_ID} = ? AND ${DatabaseHelper.COL_CA_ATR_ID} = ?",
            arrayOf(candidatoId.toString(), atributoId.toString()),
            null, null, null
        )
        cursor.use {
            if (it.moveToFirst()) {
                value = it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.COL_CA_VALOR))
            }
        }
        return value
    }

    fun insertPartida(partida: Partida): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_PART_DATA, partida.dataPartida)
            put(DatabaseHelper.COL_PART_CAND_JOGADOR_ID, partida.candidatoJogadorId)
            put(DatabaseHelper.COL_PART_CAND_OPONENTE_ID, partida.candidatoOponenteId)
            put(DatabaseHelper.COL_PART_ATR1_ID, partida.atributo1Id)
            put(DatabaseHelper.COL_PART_ATR2_ID, partida.atributo2Id)
            put(DatabaseHelper.COL_PART_ATR3_ID, partida.atributo3Id)
            put(DatabaseHelper.COL_PART_PONTOS_JOGADOR, partida.pontosJogador)
            put(DatabaseHelper.COL_PART_PONTOS_OPONENTE, partida.pontosOponente)
            put(DatabaseHelper.COL_PART_VENCEDOR_ID, partida.vencedorId) 
        }
        return db.insert(DatabaseHelper.TABLE_PARTIDAS, null, values)
    }

    fun updateRanking(candidatoId: Int, pontosGanhos: Int) { 
        val db = dbHelper.writableDatabase
        val query = "UPDATE ${DatabaseHelper.TABLE_RANKING} SET ${DatabaseHelper.COL_RANK_PONTUACAO} = ${DatabaseHelper.COL_RANK_PONTUACAO} + ? WHERE ${DatabaseHelper.COL_RANK_CAND_ID} = ?"
        db.execSQL(query, arrayOf(pontosGanhos, candidatoId))
    }

    fun getHistoricoPartidas(): List<PartidaDetalhada> {
        val db = dbHelper.readableDatabase
        val historicoList = mutableListOf<PartidaDetalhada>()

        
        
        val query = """
            SELECT
                p.${DatabaseHelper.COL_PART_ID} AS id_partida,
                p.${DatabaseHelper.COL_PART_DATA} AS data_partida,
                cj.${DatabaseHelper.COL_CAND_NOME_ENGRACADO} AS nome_jogador,
                co.${DatabaseHelper.COL_CAND_NOME_ENGRACADO} AS nome_oponente,
                a1.${DatabaseHelper.COL_ATR_NOME} AS nome_atributo_1,
                a2.${DatabaseHelper.COL_ATR_NOME} AS nome_atributo_2,
                a3.${DatabaseHelper.COL_ATR_NOME} AS nome_atributo_3,
                p.${DatabaseHelper.COL_PART_PONTOS_JOGADOR} AS pontos_jogador,
                p.${DatabaseHelper.COL_PART_PONTOS_OPONENTE} AS pontos_oponente,
                cv.${DatabaseHelper.COL_CAND_NOME_ENGRACADO} AS nome_vencedor
            FROM
                ${DatabaseHelper.TABLE_PARTIDAS} p
            LEFT JOIN
                ${DatabaseHelper.TABLE_CANDIDATOS} cj ON p.${DatabaseHelper.COL_PART_CAND_JOGADOR_ID} = cj.${DatabaseHelper.COL_CAND_ID}
            LEFT JOIN
                ${DatabaseHelper.TABLE_CANDIDATOS} co ON p.${DatabaseHelper.COL_PART_CAND_OPONENTE_ID} = co.${DatabaseHelper.COL_CAND_ID}
            LEFT JOIN
                ${DatabaseHelper.TABLE_ATRIBUTOS} a1 ON p.${DatabaseHelper.COL_PART_ATR1_ID} = a1.${DatabaseHelper.COL_ATR_ID}
            LEFT JOIN
                ${DatabaseHelper.TABLE_ATRIBUTOS} a2 ON p.${DatabaseHelper.COL_PART_ATR2_ID} = a2.${DatabaseHelper.COL_ATR_ID}
            LEFT JOIN
                ${DatabaseHelper.TABLE_ATRIBUTOS} a3 ON p.${DatabaseHelper.COL_PART_ATR3_ID} = a3.${DatabaseHelper.COL_ATR_ID}
            LEFT JOIN
                ${DatabaseHelper.TABLE_CANDIDATOS} cv ON p.${DatabaseHelper.COL_PART_VENCEDOR_ID} = cv.${DatabaseHelper.COL_CAND_ID}
            ORDER BY
                p.${DatabaseHelper.COL_PART_DATA} DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)

        cursor.use {
            
            
            val idPartidaIdx = it.getColumnIndex("id_partida")
            val dataPartidaIdx = it.getColumnIndex("data_partida")
            val nomeJogadorIdx = it.getColumnIndex("nome_jogador")
            val nomeOponenteIdx = it.getColumnIndex("nome_oponente")
            val nomeAtr1Idx = it.getColumnIndex("nome_atributo_1")
            val nomeAtr2Idx = it.getColumnIndex("nome_atributo_2")
            val nomeAtr3Idx = it.getColumnIndex("nome_atributo_3")
            val pontosJogadorIdx = it.getColumnIndex("pontos_jogador")
            val pontosOponenteIdx = it.getColumnIndex("pontos_oponente")
            val nomeVencedorIdx = it.getColumnIndex("nome_vencedor")

            while (it.moveToNext()) {
                
                fun getStringOrNull(idx: Int): String? = if (idx != -1 && !it.isNull(idx)) it.getString(idx) else null
                fun getStringOrDefault(idx: Int): String = getStringOrNull(idx) ?: "Desconhecido"
                fun getIntOrDefault(idx: Int): Int = if (idx != -1 && !it.isNull(idx)) it.getInt(idx) else 0

                val partida = PartidaDetalhada(
                    idPartida = getIntOrDefault(idPartidaIdx),
                    dataPartida = getStringOrDefault(dataPartidaIdx),
                    nomeJogador = getStringOrDefault(nomeJogadorIdx),
                    nomeOponente = getStringOrDefault(nomeOponenteIdx),
                    nomeAtributo1 = getStringOrDefault(nomeAtr1Idx),
                    nomeAtributo2 = getStringOrDefault(nomeAtr2Idx),
                    nomeAtributo3 = getStringOrDefault(nomeAtr3Idx),
                    pontosJogador = getIntOrDefault(pontosJogadorIdx),
                    pontosOponente = getIntOrDefault(pontosOponenteIdx),
                    nomeVencedor = getStringOrNull(nomeVencedorIdx) 
                )
                historicoList.add(partida)
            }
        }
        return historicoList
    }
}