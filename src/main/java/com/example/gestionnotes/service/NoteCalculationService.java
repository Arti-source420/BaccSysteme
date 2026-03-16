package com.example.gestionnotes.service;

import com.example.gestionnotes.entity.*;
import com.example.gestionnotes.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Façade principale du calcul de délibération.
 *
 * Ce service orchestre les quatre services spécialisés :
 *   DifferenceService          — calcul des écarts entre notes
 *   ParametreResolutionService — sélection de la règle (avec gestion des conflits)
 *   NoteResolutionService      — application de la résolution (min/max/moyenne)
 *   NotePersistenceService     — sauvegarde des notes en base
 *
 * Les méthodes publiques sont conservées pour la compatibilité avec
 * WebController et ApiController existants.
 */
@Service
public class NoteCalculationService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private DifferenceService differenceService;

    @Autowired
    private ParametreResolutionService parametreResolutionService;

    @Autowired
    private NoteResolutionService noteResolutionService;

    @Autowired
    private NotePersistenceService notePersistenceService;

    // ─── API publique (compatibilité controllers) ─────────────────────────────

    public Double determinerNoteFinale(Long idCandidat, Long idMatiere) {
        List<Note> notesEnBase = noteRepository.findByCandidatIdAndMatiereId(idCandidat, idMatiere);
        if (notesEnBase.isEmpty()) return null;
        List<Double> valeurs = extraireValeurs(notesEnBase);
        return calculerNoteFinaleDepuisValeurs(valeurs, idMatiere);
    }

    @Transactional
    public Double traiterSaisieNotes(String saisieNotes, Long idCandidat,
                                     Long idMatiere, List<Long> idsCorrecteurs) {
        List<Double> valeurs = differenceService.parserNotes(saisieNotes);
        notePersistenceService.sauvegarderNotes(valeurs, idCandidat, idMatiere, idsCorrecteurs);
        return calculerNoteFinaleDepuisValeurs(valeurs, idMatiere);
    }

    @Transactional
    public Double traiterSaisieNotes(String saisieNotes, Long idCandidat, Long idMatiere) {
        List<Double> valeurs = differenceService.parserNotes(saisieNotes);
        notePersistenceService.sauvegarderNotesAvecCorrecteursParDefaut(valeurs, idCandidat, idMatiere);
        return calculerNoteFinaleDepuisValeurs(valeurs, idMatiere);
    }

    public List<Double> parserNotes(String saisieNotes) {
        return differenceService.parserNotes(saisieNotes);
    }

    public Double calculerDifference(List<Double> notes) {
        return differenceService.calculerDifference(notes);
    }

    // ─── Nouvelles méthodes exposant les détails du conflit ──────────────────

    /**
     * Retourne l'explication textuelle de la sélection de règle,
     * incluant les détails du conflit si plusieurs règles s'appliquent.
     */
    public String obtenirExplicationSelection(Long idMatiere, Double difference) {
        return parametreResolutionService.expliquerSelection(idMatiere, difference);
    }

    /**
     * Identifie par comparaison quelle résolution a été appliquée.
     */
    public String identifierResolutionAppliquee(Double noteFinale, List<Double> notes) {
        return noteResolutionService.identifierResolutionAppliquee(noteFinale, notes);
    }

    // ─── Logique interne ─────────────────────────────────────────────────────

    private Double calculerNoteFinaleDepuisValeurs(List<Double> valeurs, Long idMatiere) {
        Double difference = differenceService.calculerDifference(valeurs);
        Parametre parametreApplicable =
                parametreResolutionService.selectionnerParametre(idMatiere, difference);

        if (parametreApplicable == null) {
            return noteResolutionService.calculerMoyenne(valeurs);
        }
        return noteResolutionService.appliquer(valeurs, parametreApplicable.getResolution().getNom());
    }

    private List<Double> extraireValeurs(List<Note> notes) {
        List<Double> valeurs = new ArrayList<>();
        for (Note n : notes) valeurs.add(n.getValeur());
        return valeurs;
    }
}
