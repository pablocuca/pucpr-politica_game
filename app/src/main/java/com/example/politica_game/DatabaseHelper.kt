
package com.example.politica_game

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "politicos_game.db"
        private const val DATABASE_VERSION = 2 

        const val TABLE_CANDIDATOS = "Candidatos"
        const val TABLE_ATRIBUTOS = "Atributos"
        const val TABLE_CANDIDATOS_ATRIBUTOS = "Candidatos_Atributos"
        const val TABLE_RANKING = "Ranking"
        const val TABLE_PARTIDAS = "Partidas"

        const val COL_CAND_ID = "id_candidato"
        const val COL_CAND_NOME_ORIGINAL = "nome_original"
        const val COL_CAND_NOME_ENGRACADO = "nome_engracado"
        const val COL_CAND_DESCRICAO = "descricao"

        const val COL_ATR_ID = "id_atributo"
        const val COL_ATR_NOME = "nome_atributo"
        const val COL_ATR_TIPO = "tipo_atributo"

        const val COL_CA_CAND_ID = "id_candidato"
        const val COL_CA_ATR_ID = "id_atributo"
        const val COL_CA_VALOR = "valor"

        const val COL_RANK_CAND_ID = "id_candidato"
        const val COL_RANK_PONTUACAO = "pontuacao_total"

        const val COL_PART_ID = "id_partida"
        const val COL_PART_DATA = "data_partida"
        const val COL_PART_CAND_JOGADOR_ID = "id_candidato_jogador"
        const val COL_PART_CAND_OPONENTE_ID = "id_candidato_oponente"
        const val COL_PART_ATR1_ID = "id_atributo_1"
        const val COL_PART_ATR2_ID = "id_atributo_2"
        const val COL_PART_ATR3_ID = "id_atributo_3"
        const val COL_PART_PONTOS_JOGADOR = "pontos_jogador"
        const val COL_PART_PONTOS_OPONENTE = "pontos_oponente"
        const val COL_PART_VENCEDOR_ID = "id_vencedor"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_CANDIDATOS_TABLE = """
            CREATE TABLE $TABLE_CANDIDATOS (
                $COL_CAND_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CAND_NOME_ORIGINAL TEXT NOT NULL,
                $COL_CAND_NOME_ENGRACADO TEXT NOT NULL,
                $COL_CAND_DESCRICAO TEXT
            )
        """.trimIndent()
        db.execSQL(CREATE_CANDIDATOS_TABLE)

        val CREATE_ATRIBUTOS_TABLE = """
            CREATE TABLE $TABLE_ATRIBUTOS (
                $COL_ATR_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_ATR_NOME TEXT NOT NULL UNIQUE,
                $COL_ATR_TIPO TEXT NOT NULL CHECK ($COL_ATR_TIPO IN ('Positivo', 'Negativo'))
            )
        """.trimIndent()
        db.execSQL(CREATE_ATRIBUTOS_TABLE)

        val CREATE_CANDIDATOS_ATRIBUTOS_TABLE = """
            CREATE TABLE $TABLE_CANDIDATOS_ATRIBUTOS (
                $COL_CA_CAND_ID INTEGER NOT NULL,
                $COL_CA_ATR_ID INTEGER NOT NULL,
                $COL_CA_VALOR INTEGER NOT NULL CHECK ($COL_CA_VALOR >= 1 AND $COL_CA_VALOR <= 10),
                PRIMARY KEY ($COL_CA_CAND_ID, $COL_CA_ATR_ID),
                FOREIGN KEY ($COL_CA_CAND_ID) REFERENCES $TABLE_CANDIDATOS($COL_CAND_ID) ON DELETE CASCADE,
                FOREIGN KEY ($COL_CA_ATR_ID) REFERENCES $TABLE_ATRIBUTOS($COL_ATR_ID) ON DELETE CASCADE
            )
        """.trimIndent()
        db.execSQL(CREATE_CANDIDATOS_ATRIBUTOS_TABLE)

        val CREATE_RANKING_TABLE = """
            CREATE TABLE $TABLE_RANKING (
                $COL_RANK_CAND_ID INTEGER PRIMARY KEY,
                $COL_RANK_PONTUACAO INTEGER DEFAULT 0 NOT NULL,
                FOREIGN KEY ($COL_RANK_CAND_ID) REFERENCES $TABLE_CANDIDATOS($COL_CAND_ID) ON DELETE CASCADE
            )
        """.trimIndent()
        db.execSQL(CREATE_RANKING_TABLE)

        val CREATE_PARTIDAS_TABLE = """
            CREATE TABLE $TABLE_PARTIDAS (
                $COL_PART_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PART_DATA DATETIME DEFAULT CURRENT_TIMESTAMP,
                $COL_PART_CAND_JOGADOR_ID INTEGER NOT NULL,
                $COL_PART_CAND_OPONENTE_ID INTEGER NOT NULL,
                $COL_PART_ATR1_ID INTEGER NOT NULL,
                $COL_PART_ATR2_ID INTEGER NOT NULL,
                $COL_PART_ATR3_ID INTEGER NOT NULL,
                $COL_PART_PONTOS_JOGADOR INTEGER NOT NULL,
                $COL_PART_PONTOS_OPONENTE INTEGER NOT NULL,
                $COL_PART_VENCEDOR_ID INTEGER,
                FOREIGN KEY ($COL_PART_CAND_JOGADOR_ID) REFERENCES $TABLE_CANDIDATOS($COL_CAND_ID),
                FOREIGN KEY ($COL_PART_CAND_OPONENTE_ID) REFERENCES $TABLE_CANDIDATOS($COL_CAND_ID),
                FOREIGN KEY ($COL_PART_VENCEDOR_ID) REFERENCES $TABLE_CANDIDATOS($COL_CAND_ID),
                FOREIGN KEY ($COL_PART_ATR1_ID) REFERENCES $TABLE_ATRIBUTOS($COL_ATR_ID),
                FOREIGN KEY ($COL_PART_ATR2_ID) REFERENCES $TABLE_ATRIBUTOS($COL_ATR_ID),
                FOREIGN KEY ($COL_PART_ATR3_ID) REFERENCES $TABLE_ATRIBUTOS($COL_ATR_ID)
            )
        """.trimIndent()
        db.execSQL(CREATE_PARTIDAS_TABLE)

        populateInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PARTIDAS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RANKING")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CANDIDATOS_ATRIBUTOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ATRIBUTOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CANDIDATOS")
        onCreate(db)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    fun insertAttribute(db: SQLiteDatabase, nome: String, tipo: String): Long {
        val values = android.content.ContentValues().apply {
            put(COL_ATR_NOME, nome)
            put(COL_ATR_TIPO, tipo)
        }
        return db.insert(TABLE_ATRIBUTOS, null, values)
    }

    fun insertCandidate(db: SQLiteDatabase, nomeOriginal: String, nomeEngracado: String, descricao: String?): Long {
        val values = android.content.ContentValues().apply {
            put(COL_CAND_NOME_ORIGINAL, nomeOriginal)
            put(COL_CAND_NOME_ENGRACADO, nomeEngracado)
            put(COL_CAND_DESCRICAO, descricao)
        }
        return db.insert(TABLE_CANDIDATOS, null, values)
    }

    fun insertCandidateAttribute(db: SQLiteDatabase, candId: Int, atrId: Int, valor: Int): Long {
        val values = android.content.ContentValues().apply {
            put(COL_CA_CAND_ID, candId)
            put(COL_CA_ATR_ID, atrId)
            put(COL_CA_VALOR, valor)
        }
        return db.insert(TABLE_CANDIDATOS_ATRIBUTOS, null, values)
    }

    fun initializeRankingForCandidate(db: SQLiteDatabase, candId: Int): Long {
        val values = android.content.ContentValues().apply {
            put(COL_RANK_CAND_ID, candId)
            put(COL_RANK_PONTUACAO, 0)
        }
        return db.insert(TABLE_RANKING, null, values)
    }

    private fun populateInitialData(db: SQLiteDatabase) {
        val idLideranca = insertAttribute(db, "Liderança", "Positivo")
        val idHonestidade = insertAttribute(db, "Honestidade", "Positivo")
        val idEmpatia = insertAttribute(db, "Empatia", "Positivo")
        val idDialogo = insertAttribute(db, "Diálogo", "Positivo")
        val idVisao = insertAttribute(db, "Visão", "Positivo")
        val idCorrupcao = insertAttribute(db, "Corrupção", "Negativo")
        val idPopulismo = insertAttribute(db, "Populismo", "Negativo")
        val idAutoritarismo = insertAttribute(db, "Autoritarismo", "Negativo")
        val idIncompetencia = insertAttribute(db, "Incompetência", "Negativo")
        val idClientelismo = insertAttribute(db, "Clientelismo", "Negativo")

        val attributeIds = listOf(
            idLideranca.toInt(), idHonestidade.toInt(), idEmpatia.toInt(), idDialogo.toInt(), idVisao.toInt(),
            idCorrupcao.toInt(), idPopulismo.toInt(), idAutoritarismo.toInt(), idIncompetencia.toInt(), idClientelismo.toInt()
        )

        val candData = listOf(
            Triple("Carlos Silva dos Santos", "Carlão Silva", "O presidente que comanda a nação e tem uma história de altos e baixos na política brasileira."),
            Triple("Roberto Oliveira", "Beto Oliveiras", "O ex-presidente com forte influência nas redes sociais e uma base de apoio leal."),
            Triple("Eduardo Lima", "Dudu Lima-Lima", "O articulador principal da Câmara dos Deputados, peça-chave na aprovação de leis."),
            Triple("Marcos Pereira", "Marquinhos Pereira", "O líder do Senado Federal, responsável por conduzir os trabalhos da casa legislativa."),
            Triple("Paulo Costa", "Paulinho Costa", "O ministro da Economia, com a missão de gerenciar as finanças do país."),
            Triple("Ricardo Almeida", "Ricardão Almeida", "O governador de São Paulo, um nome em ascensão na direita e com foco em infraestrutura."),
            Triple("João Martins", "Joãozão Martins", "Um dos ministros mais influentes do STF, atuante em questões de segurança e democracia."),
            Triple("Antonio Souza", "Toninho Souza", "O vice-presidente e ministro, com grande experiência política e de gestão."),
            Triple("Helena Ribeiro", "Leninha Ribeiro", "A ministra do Planejamento, conhecida por sua moderação e capacidade de diálogo."),
            Triple("Fernanda Rocha", "Nandinha Rocha", "A ministra do Meio Ambiente, ícone da pauta ambiental no Brasil e no mundo."),
            Triple("Carla Mendes", "Carlinha Mendes", "A presidente do partido, com forte atuação na articulação política do partido."),
            Triple("José Ferreira", "Zé Ferreirinha", "O ministro da Educação, focado em políticas públicas para o ensino."),
            Triple("Ana Barbosa", "Aninha Barbosa", "A ex-primeira-dama, com crescente projeção política e forte apelo popular."),
            Triple("Pedro Araújo", "Pedrão Araújo", "O governador de Goiás, um nome forte no agronegócio e na direita brasileira."),
            Triple("Lucas Cardoso", "Luquinha Cardoso", "O deputado federal com grande influência e engajamento nas redes sociais.")
        )

        val candidateIds = mutableListOf<Int>()
        for (data in candData) {
            val id = insertCandidate(db, data.first, data.second, data.third).toInt()
            candidateIds.add(id)
            initializeRankingForCandidate(db, id)
        }

        val random = java.util.Random()
        for (candId in candidateIds) {
            for (atrId in attributeIds) {
                val valor = random.nextInt(10) + 1
                insertCandidateAttribute(db, candId, atrId, valor)
            }
        }
    }
}