package com.michael.features.genre

import com.michael.plugins.DatabaseSingleton.dbQuery
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll

class GenreDaoImpl : GenreDao {
    override suspend fun getAll(): List<Genre> = dbQuery {
        GenreTable.selectAll().map { it.toGenre() }
    }

    override suspend fun addGenre(genre: Genre): Genre? = dbQuery {
        TODO("Not yet implemented")
    }

    private fun ResultRow.toGenre(): Genre = Genre(
        this[GenreTable.genreId],
        this[GenreTable.name]
    )
}