package net.invictusmanagement.invictuskiosk.data.repository

interface Repository {
    suspend fun sync()
}