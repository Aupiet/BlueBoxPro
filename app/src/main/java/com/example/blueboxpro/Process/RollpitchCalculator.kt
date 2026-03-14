package com.example.blueboxpro.Process

import android.hardware.SensorManager
import kotlin.math.roundToInt
/**
 * Utilitaire dédié au calcul de l'attitude (inclinaison) du périphérique.
 */
object RollpitchCalculator {
    private val RAD_TO_DEG = 57.2957795131 // (180 / PI)
    /**
     * Calcule le Pitch et le Roll à partir des données brutes des capteurs.
     * * @param gravity Données de l'accéléromètre (3 floats)
     * @param geomagnetic Données du magnétomètre (3 floats)
     * @return Une paire d'entiers (Pitch, Roll) ou null si le calcul échoue
     */
    fun calculatePitchRoll(gravity: FloatArray, geomagnetic: FloatArray): Pair<Int, Int>? {
        val r = FloatArray(9)
        val i = FloatArray(9)

        // Tente de générer la matrice de rotation
        return if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(r, orientation)

            // orientation[1] est le pitch, orientation[2] est le roll
            val pitch = (orientation[1] * RAD_TO_DEG).roundToInt()
            val roll = (orientation[2] * RAD_TO_DEG).roundToInt()

            Pair(pitch, roll)
        } else {
            null
        }
    }
}