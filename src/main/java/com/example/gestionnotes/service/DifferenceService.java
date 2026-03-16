package com.example.gestionnotes.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsable du calcul des écarts entre les notes des correcteurs.
 *
 * Pour 2 notes  : écart = |note1 - note2|
 * Pour N notes  : somme de toutes les différences absolues entre chaque paire
 */
@Service
public class DifferenceService {

    /**
     * Calcule la différence totale entre toutes les paires de notes.
     *
     * @param notes liste des valeurs de notes
     * @return somme des différences absolues par paire, 0 si moins de 2 notes
     */
    public Double calculerDifference(List<Double> notes) {
        if (notes == null || notes.size() < 2) {
            return 0.0;
        }

        double sommeDifferences = 0.0;
        for (int i = 0; i < notes.size(); i++) {
            for (int j = i + 1; j < notes.size(); j++) {
                sommeDifferences += Math.abs(notes.get(i) - notes.get(j));
            }
        }
        return sommeDifferences;
    }

    /**
     * Retourne le détail de chaque paire sous forme de chaîne lisible.
     * Exemple : ["10.0 - 17.0 = 7.0"]
     *
     * @param notes liste des valeurs de notes
     * @return liste de chaînes décrivant chaque paire
     */
    public List<String> detaillerPaires(List<Double> notes) {
        List<String> details = new ArrayList<>();
        if (notes == null || notes.size() < 2) return details;

        for (int i = 0; i < notes.size(); i++) {
            for (int j = i + 1; j < notes.size(); j++) {
                double diff = Math.abs(notes.get(i) - notes.get(j));
                details.add(String.format("%.1f - %.1f = %.1f",
                        notes.get(i), notes.get(j), diff));
            }
        }
        return details;
    }

    /**
     * Parse une chaîne de notes séparées par ";" et retourne la liste des valeurs.
     *
     * @param saisieNotes chaîne brute, ex: "10;17"
     * @return liste de Double
     * @throws IllegalArgumentException si une note est mal formatée
     */
    public List<Double> parserNotes(String saisieNotes) {
        String[] notesArray = saisieNotes.split(";");
        List<Double> notes = new ArrayList<>();

        for (String noteStr : notesArray) {
            try {
                notes.add(Double.parseDouble(noteStr.trim()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Format de note invalide: " + noteStr);
            }
        }
        return notes;
    }
}
