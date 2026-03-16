package com.example.gestionnotes.service;

import com.example.gestionnotes.entity.*;
import com.example.gestionnotes.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service responsable de la persistance des notes en base de données.
 *
 * Responsabilités :
 *   • Supprimer les anciennes notes d'un candidat/matière avant re-saisie
 *   • Créer et sauvegarder les nouvelles notes avec leurs correcteurs
 *   • Résoudre les correcteurs (par ID fourni ou par défaut)
 */
@Service
public class NotePersistenceService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private CandidatRepository candidatRepository;

    @Autowired
    private MatiereRepository matiereRepository;

    @Autowired
    private CorrecteurRepository correcteurRepository;

    /**
     * Sauvegarde les notes avec des correcteurs explicitement désignés.
     *
     * @param valeurs        liste ordonnée des valeurs de notes
     * @param idCandidat     identifiant du candidat
     * @param idMatiere      identifiant de la matière
     * @param idsCorrecteurs liste ordonnée des IDs de correcteurs (même taille que valeurs)
     * @throws IllegalArgumentException si les tailles ne concordent pas
     */
    @Transactional
    public void sauvegarderNotes(List<Double> valeurs, Long idCandidat,
                                 Long idMatiere, List<Long> idsCorrecteurs) {

        if (valeurs.size() != idsCorrecteurs.size()) {
            throw new IllegalArgumentException(
                    "Le nombre de notes (" + valeurs.size() +
                    ") ne correspond pas au nombre de correcteurs (" +
                    idsCorrecteurs.size() + ")");
        }

        Candidat candidat = trouverCandidat(idCandidat);
        Matiere  matiere  = trouverMatiere(idMatiere);

        supprimerAnciennesNotes(idCandidat, idMatiere);

        for (int i = 0; i < valeurs.size(); i++) {
            Correcteur correcteur = trouverCorrecteur(idsCorrecteurs.get(i));
            enregistrerNote(candidat, matiere, correcteur, valeurs.get(i));
        }
    }

    /**
     * Sauvegarde les notes en attribuant automatiquement les premiers
     * correcteurs disponibles en base (version sans choix explicite).
     *
     * @param valeurs    liste ordonnée des valeurs de notes
     * @param idCandidat identifiant du candidat
     * @param idMatiere  identifiant de la matière
     * @throws IllegalArgumentException si pas assez de correcteurs disponibles
     */
    @Transactional
    public void sauvegarderNotesAvecCorrecteursParDefaut(List<Double> valeurs,
                                                         Long idCandidat,
                                                         Long idMatiere) {
        List<Correcteur> disponibles = correcteurRepository.findAll();

        if (disponibles.size() < valeurs.size()) {
            throw new IllegalArgumentException(
                    "Pas assez de correcteurs disponibles. Besoin : " + valeurs.size() +
                    ", Disponibles : " + disponibles.size());
        }

        Candidat candidat = trouverCandidat(idCandidat);
        Matiere  matiere  = trouverMatiere(idMatiere);

        supprimerAnciennesNotes(idCandidat, idMatiere);

        for (int i = 0; i < valeurs.size(); i++) {
            enregistrerNote(candidat, matiere, disponibles.get(i), valeurs.get(i));
        }
    }

    // ─── Méthodes utilitaires privées ────────────────────────────────────────

    private void supprimerAnciennesNotes(Long idCandidat, Long idMatiere) {
        List<Note> anciennes = noteRepository.findByCandidatIdAndMatiereId(idCandidat, idMatiere);
        if (!anciennes.isEmpty()) {
            noteRepository.deleteAll(anciennes);
        }
    }

    private void enregistrerNote(Candidat candidat, Matiere matiere,
                                  Correcteur correcteur, Double valeur) {
        Note note = new Note();
        note.setCandidat(candidat);
        note.setMatiere(matiere);
        note.setCorrecteur(correcteur);
        note.setValeur(valeur);
        noteRepository.save(note);
    }

    public Candidat trouverCandidat(Long id) {
        return candidatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé avec l'ID : " + id));
    }

    public Matiere trouverMatiere(Long id) {
        return matiereRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Matière non trouvée avec l'ID : " + id));
    }

    public Correcteur trouverCorrecteur(Long id) {
        return correcteurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Correcteur non trouvé avec l'ID : " + id));
    }

    public List<Correcteur> listerTousLesCorrecteurs() {
        return correcteurRepository.findAll();
    }
}
