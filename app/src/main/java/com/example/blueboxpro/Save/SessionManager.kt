package com.example.blueboxpro.Save

import androidx.compose.runtime.mutableStateListOf

// TODO ajouter dans la structure un tableau qui sauvegrades tous les points enregistrés

data class Session(
    val id: Int,
    val date: String,
    val duration: String,
    val distance: String
)

object SessionManager {
    // Liste réactive des sessions
    val sessions = mutableStateListOf<Session>()
    fun getSessions(): List<Session> { return sessions }
    fun saveSessionInfile() { /* TODO : Sauvegarde dans un fichier */ }
    fun setSessions(newSessions: List<Session>) { sessions.clear(); sessions.addAll(newSessions) }
    fun clearSessions() { sessions.clear() }
    // Fonction pour ajouter une session (pour tester plus tard)
    fun addSession(date: String, duration: String, distance: String) {
        val newId = if (sessions.isEmpty()) 1 else sessions.maxOf { it.id } + 1
        sessions.add(Session(newId, date, duration, distance))
    }
}
