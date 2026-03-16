package com.example.gestionnotes.service;

import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

/**
 * Service responsable d'appliquer une résolution sur une liste de notes.
 *
 * Les trois résolutions possibles sont :
 *   • "plus petit" → valeur minimale
 *   • "plus grand"  → valeur maximale
 *   • "moyenne"     → moyenne arithmétique (comportement par défaut)
 */
@Service
public class NoteResolutionService {

    /**
     * Applique la résolution indiquée sur la liste de notes.
     *
     * @param notes      liste de valeurs (non vide)
     * @param resolution nom de la résolution (insensible à la casse)
     * @return la note finale calculée
     * @throws IllegalArgumentException si la liste est nulle ou vide
     */
    public Double appliquer(List<Double> notes, String resolution) {
        if (notes == null || notes.isEmpty()) {
            throw new IllegalArgumentException("La liste de notes ne peut pas être vide.");
        }

        String res = resolution.toLowerCase().trim();

        if (res.contains("plus petit")) {
            return Collections.min(notes);
        } else if (res.contains("plus grand")) {
            return Collections.max(notes);
        } else {
            // "moyenne" et tout autre cas inconnu
            return calculerMoyenne(notes);
        }
    }

    /**
     * Calcule la moyenne arithmétique simple.
     */
    public Double calculerMoyenne(List<Double> notes) {
        if (notes == null || notes.isEmpty()) return 0.0;
        double somme = 0.0;
        for (Double n : notes) somme += n;
        return somme / notes.size();
    }

    /**
     * Indique quel type de résolution a été appliqué en comparant la note
     * finale aux valeurs min/max/moyenne de la liste.
     * Utile pour l'affichage dans la vue.
     *
     * @param noteFinale note résultante
     * @param notes      liste de notes originales
     * @return étiquette lisible : "plus petit", "plus grand" ou "moyenne"
     */
    public String identifierResolutionAppliquee(Double noteFinale, List<Double> notes) {
        if (notes == null || notes.isEmpty()) return "inconnue";

        double min     = Collections.min(notes);
        double max     = Collections.max(notes);
        double moyenne = calculerMoyenne(notes);

        final double epsilon = 1e-6;

        if (Math.abs(noteFinale - min) < epsilon)     return "plus petit";
        if (Math.abs(noteFinale - max) < epsilon)     return "plus grand";
        if (Math.abs(noteFinale - moyenne) < epsilon) return "moyenne";

        return "moyenne"; // par sécurité
    }
}
