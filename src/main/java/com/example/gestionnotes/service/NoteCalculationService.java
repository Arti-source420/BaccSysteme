package com.example.gestionnotes.service;

import com.example.gestionnotes.entity.*;
import com.example.gestionnotes.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class NoteCalculationService {
    
    @Autowired
    private NoteRepository noteRepository;
    
    @Autowired
    private ParametreRepository parametreRepository;
    
    @Autowired
    private CandidatRepository candidatRepository;
    
    @Autowired
    private MatiereRepository matiereRepository;
    
    @Autowired
    private CorrecteurRepository correcteurRepository;
    
    /**
     * Calcule la différence entre plusieurs notes
     * Pour 2 notes: |note1 - note2|
     * Pour plus de 2 notes: somme de toutes les différences entre chaque paire
     */
    public Double calculerDifference(List<Double> notes) {
        if (notes == null || notes.size() < 2) {
            return 0.0;
        }
        
        Double sommeDifferences = 0.0;
        
        // Calculer toutes les paires possibles
        for (int i = 0; i < notes.size(); i++) {
            for (int j = i + 1; j < notes.size(); j++) {
                sommeDifferences += Math.abs(notes.get(i) - notes.get(j));
            }
        }
        
        return sommeDifferences;
    }
    
    /**
     * Détermine la note finale selon les règles paramétrées
     */
    public Double determinerNoteFinale(Long idCandidat, Long idMatiere) {
        // Récupérer toutes les notes du candidat pour cette matière
        List<Note> notes = noteRepository.findByCandidatIdAndMatiereId(idCandidat, idMatiere);
        
        if (notes.isEmpty()) {
            return null;
        }
        
        // Extraire les valeurs des notes
        List<Double> valeursNotes = new ArrayList<>();
        for (Note note : notes) {
            valeursNotes.add(note.getValeur());
        }
        
        // Calculer la différence
        Double difference = calculerDifference(valeursNotes);
        
        // Récupérer les paramètres pour cette matière
        List<Parametre> parametres = parametreRepository.findByMatiereIdOrderByDifference(idMatiere);
        
        // Trouver la règle applicable
        Parametre parametreApplicable = trouverParametreApplicable(parametres, difference);
        
        if (parametreApplicable == null) {
            // Si aucune règle trouvée, retourner la moyenne par défaut
            double somme = 0.0;
            for (Double note : valeursNotes) {
                somme += note;
            }
            return somme / valeursNotes.size();
        }
        
        // Appliquer la résolution
        return appliquerResolution(valeursNotes, parametreApplicable.getResolution().getNom());
    }
    
    /**
     * Trouve le paramètre applicable selon la différence calculée
     */
    private Parametre trouverParametreApplicable(List<Parametre> parametres, Double difference) {
        for (Parametre p : parametres) {
            if (comparerAvecOperateur(difference, p.getDifference(), p.getOperateur().getSymbole())) {
                return p;
            }
        }
        return null;
    }
    
    /**
     * Compare deux valeurs selon un opérateur
     */
    private boolean comparerAvecOperateur(Double valeur1, Double valeur2, String operateur) {
        if (operateur.equals("<")) {
            return valeur1 < valeur2;
        } else if (operateur.equals(">")) {
            return valeur1 > valeur2;
        } else if (operateur.equals("<=")) {
            return valeur1 <= valeur2;
        } else if (operateur.equals(">=")) {
            return valeur1 >= valeur2;
        } else {
            return false;
        }
    }
    
    /**
     * Applique la résolution (plus petit, plus grand, moyenne)
     */
    private Double appliquerResolution(List<Double> notes, String resolution) {
        String resolutionLower = resolution.toLowerCase();
        
        if (resolutionLower.contains("plus petit")) {
            return Collections.min(notes);
        } else if (resolutionLower.contains("plus grand")) {
            return Collections.max(notes);
        } else { // moyenne ou autre
            double somme = 0.0;
            for (Double note : notes) {
                somme += note;
            }
            return somme / notes.size();
        }
    }
    
    /**
     * Traite une chaîne de notes séparées par des points-virgules avec IDs correcteurs
     */
    @Transactional
    public Double traiterSaisieNotes(String saisieNotes, Long idCandidat, Long idMatiere, List<Long> idsCorrecteurs) {
        // Diviser la chaîne et parser les notes
        String[] notesArray = saisieNotes.split(";");
        List<Double> notes = new ArrayList<>();
        
        for (String noteStr : notesArray) {
            try {
                notes.add(Double.parseDouble(noteStr.trim()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Format de note invalide: " + noteStr);
            }
        }
        
        if (notes.size() != idsCorrecteurs.size()) {
            throw new IllegalArgumentException("Le nombre de notes (" + notes.size() + 
                                             ") ne correspond pas au nombre de correcteurs (" + 
                                             idsCorrecteurs.size() + ")");
        }
        
        // Récupérer le candidat et la matière
        Candidat candidat = candidatRepository.findById(idCandidat)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé avec l'ID: " + idCandidat));
        
        Matiere matiere = matiereRepository.findById(idMatiere)
                .orElseThrow(() -> new RuntimeException("Matière non trouvée avec l'ID: " + idMatiere));
        
        // Supprimer les anciennes notes pour ce candidat et cette matière
        List<Note> anciennesNotes = noteRepository.findByCandidatIdAndMatiereId(idCandidat, idMatiere);
        if (!anciennesNotes.isEmpty()) {
            noteRepository.deleteAll(anciennesNotes);
        }
        
        // Créer les nouvelles notes - Utilisation d'une boucle for classique au lieu de stream
        for (int i = 0; i < notes.size(); i++) {
            Long correcteurId = idsCorrecteurs.get(i);
            Double noteValeur = notes.get(i);
            
            Correcteur correcteur = correcteurRepository.findById(correcteurId)
                    .orElseThrow(() -> new RuntimeException("Correcteur non trouvé avec l'ID: " + correcteurId));
            
            Note note = new Note();
            note.setCandidat(candidat);
            note.setMatiere(matiere);
            note.setCorrecteur(correcteur);
            note.setValeur(noteValeur);
            
            noteRepository.save(note);
        }
        
        // Calculer et retourner la note finale
        return determinerNoteFinale(idCandidat, idMatiere);
    }
    
    /**
     * Version simplifiée de traiterSaisieNotes sans spécifier les correcteurs
     * Utilise des correcteurs par défaut
     */
    @Transactional
    public Double traiterSaisieNotes(String saisieNotes, Long idCandidat, Long idMatiere) {
        // Diviser la chaîne et parser les notes
        String[] notesArray = saisieNotes.split(";");
        List<Double> notes = new ArrayList<>();
        
        for (String noteStr : notesArray) {
            try {
                notes.add(Double.parseDouble(noteStr.trim()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Format de note invalide: " + noteStr);
            }
        }
        
        // Récupérer le candidat et la matière
        Candidat candidat = candidatRepository.findById(idCandidat)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé avec l'ID: " + idCandidat));
        
        Matiere matiere = matiereRepository.findById(idMatiere)
                .orElseThrow(() -> new RuntimeException("Matière non trouvée avec l'ID: " + idMatiere));
        
        // Récupérer des correcteurs par défaut (les premiers disponibles)
        List<Correcteur> correcteursDisponibles = correcteurRepository.findAll();
        if (correcteursDisponibles.size() < notes.size()) {
            throw new IllegalArgumentException("Pas assez de correcteurs disponibles. " +
                                             "Besoin: " + notes.size() + 
                                             ", Disponibles: " + correcteursDisponibles.size());
        }
        
        // Supprimer les anciennes notes pour ce candidat et cette matière
        List<Note> anciennesNotes = noteRepository.findByCandidatIdAndMatiereId(idCandidat, idMatiere);
        if (!anciennesNotes.isEmpty()) {
            noteRepository.deleteAll(anciennesNotes);
        }
        
        // Créer les nouvelles notes - Utilisation d'une boucle for classique
        for (int i = 0; i < notes.size(); i++) {
            Correcteur correcteur = correcteursDisponibles.get(i);
            Double noteValeur = notes.get(i);
            
            Note note = new Note();
            note.setCandidat(candidat);
            note.setMatiere(matiere);
            note.setCorrecteur(correcteur);
            note.setValeur(noteValeur);
            
            noteRepository.save(note);
        }
        
        // Calculer et retourner la note finale
        return determinerNoteFinale(idCandidat, idMatiere);
    }
    
    /**
     * Méthode utilitaire pour parser les notes depuis une chaîne
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