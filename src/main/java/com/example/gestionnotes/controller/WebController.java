package com.example.gestionnotes.controller;

import com.example.gestionnotes.entity.*;
import com.example.gestionnotes.repository.*;
import com.example.gestionnotes.service.NoteCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

@Controller
@RequestMapping("/")
public class WebController {
    
    @Autowired
    private CandidatRepository candidatRepository;
    
    @Autowired
    private MatiereRepository matiereRepository;
    
    @Autowired
    private CorrecteurRepository correcteurRepository;
    
    @Autowired
    private NoteRepository noteRepository;
    
    @Autowired
    private ParametreRepository parametreRepository;
    
    @Autowired
    private OperateurRepository operateurRepository;
    
    @Autowired
    private ResolutionRepository resolutionRepository;
    
    @Autowired
    private NoteCalculationService calculationService;
    
    @GetMapping
    public String index(Model model) {
        model.addAttribute("candidats", candidatRepository.findAll());
        model.addAttribute("matieres", matiereRepository.findAll());
        model.addAttribute("correcteurs", correcteurRepository.findAll());
        model.addAttribute("notes", noteRepository.findAll());
        model.addAttribute("parametres", parametreRepository.findAll());
        return "index";
    }
    
    // ============= CRUD Notes =============
    
    @GetMapping("/notes")
    public String listNotes(Model model) {
        model.addAttribute("notes", noteRepository.findAll());
        model.addAttribute("candidats", candidatRepository.findAll());
        model.addAttribute("matieres", matiereRepository.findAll());
        model.addAttribute("correcteurs", correcteurRepository.findAll());
        model.addAttribute("note", new Note());
        return "notes";
    }
    
    @PostMapping("/notes/save")
    public String saveNote(@ModelAttribute Note note, RedirectAttributes redirectAttributes) {
        try {
            // Vérifier que les objets associés existent
            if (note.getCandidat() != null && note.getCandidat().getId() != null) {
                note.setCandidat(candidatRepository.findById(note.getCandidat().getId())
                        .orElseThrow(() -> new RuntimeException("Candidat non trouvé")));
            }
            if (note.getMatiere() != null && note.getMatiere().getId() != null) {
                note.setMatiere(matiereRepository.findById(note.getMatiere().getId())
                        .orElseThrow(() -> new RuntimeException("Matière non trouvée")));
            }
            if (note.getCorrecteur() != null && note.getCorrecteur().getId() != null) {
                note.setCorrecteur(correcteurRepository.findById(note.getCorrecteur().getId())
                        .orElseThrow(() -> new RuntimeException("Correcteur non trouvé")));
            }
            
            noteRepository.save(note);
            redirectAttributes.addFlashAttribute("success", "Note enregistrée avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'enregistrement: " + e.getMessage());
        }
        return "redirect:/notes";
    }
    
    @GetMapping("/notes/delete/{id}")
    public String deleteNote(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (noteRepository.existsById(id)) {
                noteRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "Note supprimée avec succès");
            } else {
                redirectAttributes.addFlashAttribute("error", "Note non trouvée");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
        }
        return "redirect:/notes";
    }
    
    // ============= CRUD Paramètres =============
    
    @GetMapping("/parametres")
    public String listParametres(Model model) {
        model.addAttribute("parametres", parametreRepository.findAll());
        model.addAttribute("matieres", matiereRepository.findAll());
        model.addAttribute("operateurs", operateurRepository.findAll());
        model.addAttribute("resolutions", resolutionRepository.findAll());
        model.addAttribute("parametre", new Parametre());
        return "parametres";
    }
    
    @PostMapping("/parametres/save")
    public String saveParametre(@ModelAttribute Parametre parametre, RedirectAttributes redirectAttributes) {
        try {
            // Vérifier que les objets associés existent
            if (parametre.getMatiere() != null && parametre.getMatiere().getId() != null) {
                parametre.setMatiere(matiereRepository.findById(parametre.getMatiere().getId())
                        .orElseThrow(() -> new RuntimeException("Matière non trouvée")));
            }
            if (parametre.getOperateur() != null && parametre.getOperateur().getId() != null) {
                parametre.setOperateur(operateurRepository.findById(parametre.getOperateur().getId())
                        .orElseThrow(() -> new RuntimeException("Opérateur non trouvé")));
            }
            if (parametre.getResolution() != null && parametre.getResolution().getId() != null) {
                parametre.setResolution(resolutionRepository.findById(parametre.getResolution().getId())
                        .orElseThrow(() -> new RuntimeException("Résolution non trouvée")));
            }
            
            parametreRepository.save(parametre);
            redirectAttributes.addFlashAttribute("success", "Paramètre enregistré avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'enregistrement: " + e.getMessage());
        }
        return "redirect:/parametres";
    }
    
    @GetMapping("/parametres/delete/{id}")
    public String deleteParametre(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (parametreRepository.existsById(id)) {
                parametreRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "Paramètre supprimé avec succès");
            } else {
                redirectAttributes.addFlashAttribute("error", "Paramètre non trouvé");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
        }
        return "redirect:/parametres";
    }
    
    // ============= Page de calcul de note finale =============
    
    @GetMapping("/calcul")
    public String showCalculationPage(Model model) {
        model.addAttribute("candidats", candidatRepository.findAll());
        model.addAttribute("matieres", matiereRepository.findAll());
        model.addAttribute("correcteurs", correcteurRepository.findAll());
        
        // Ajouter un attribut pour le résultat (initialement null)
        if (!model.containsAttribute("noteFinale")) {
            model.addAttribute("noteFinale", null);
        }
        
        return "calcul";
    }
    
    @PostMapping("/calcul/resultat")
    public String calculateFinalNote(
            @RequestParam Long candidatId,
            @RequestParam Long matiereId,
            @RequestParam String notesSaisies,
            @RequestParam(required = false) List<Long> correcteurIds,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Valider les entrées
            if (candidatId == null || matiereId == null || notesSaisies == null || notesSaisies.trim().isEmpty()) {
                throw new IllegalArgumentException("Tous les champs sont requis");
            }
            
            // Compter le nombre de notes saisies
            String[] notesArray = notesSaisies.split(";");
            int nbNotes = notesArray.length;
            
            if (nbNotes < 2) {
                throw new IllegalArgumentException("Au moins 2 notes sont requises pour le calcul");
            }
            
            // Parser les notes pour le calcul de différence
            List<Double> notesList = new ArrayList<>();
            for (String note : notesArray) {
                try {
                    notesList.add(Double.parseDouble(note.trim()));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Format de note invalide: " + note);
                }
            }
            
            // Calculer la différence avec détails
            Double difference = 0.0;
            List<String> detailsPaires = new ArrayList<>();
            
            for (int i = 0; i < notesList.size(); i++) {
                for (int j = i + 1; j < notesList.size(); j++) {
                    double diff = Math.abs(notesList.get(i) - notesList.get(j));
                    difference += diff;
                    detailsPaires.add(String.format("%.1f - %.1f = %.1f", 
                        notesList.get(i), notesList.get(j), diff));
                }
            }
            
            // Construire le détail du calcul HTML
            StringBuilder detailsCalcul = new StringBuilder("<p>Paires de notes :</p><ul>");
            for (String detail : detailsPaires) {
                detailsCalcul.append("<li>").append(detail).append("</li>");
            }
            detailsCalcul.append("</ul><p class='fw-bold'>Somme totale = ").append(difference).append("</p>");
            
            // Gestion des correcteurs
            List<Long> idsCorrecteurs = new ArrayList<>();
            if (correcteurIds != null && !correcteurIds.isEmpty()) {
                // Vérifier que le nombre de correcteurs correspond au nombre de notes
                if (correcteurIds.size() != nbNotes) {
                    throw new IllegalArgumentException("Le nombre de correcteurs (" + correcteurIds.size() + 
                            ") doit correspondre au nombre de notes (" + nbNotes + ")");
                }
                idsCorrecteurs.addAll(correcteurIds);
            } else {
                // Utiliser les correcteurs par défaut
                List<Correcteur> correcteurs = correcteurRepository.findAll();
                if (correcteurs.isEmpty()) {
                    throw new IllegalArgumentException("Aucun correcteur disponible dans la base de données");
                }
                
                if (correcteurs.size() < nbNotes) {
                    throw new IllegalArgumentException("Pas assez de correcteurs disponibles. Besoin: " + 
                            nbNotes + ", Disponibles: " + correcteurs.size());
                }
                
                // Prendre les premiers correcteurs
                for (int i = 0; i < nbNotes; i++) {
                    idsCorrecteurs.add(correcteurs.get(i).getId());
                }
            }
            
            // Sauvegarder les notes et calculer la note finale
            Double noteFinale = calculationService.traiterSaisieNotes(
                    notesSaisies, candidatId, matiereId, idsCorrecteurs);
            
            // Déterminer quelle résolution a été appliquée
            String resolutionAppliquee = determinerResolution(noteFinale, notesList);
            
            // Préparer les données pour la vue
            model.addAttribute("noteFinale", noteFinale);
            model.addAttribute("candidat_delibere", candidatRepository.findById(candidatId).orElse(null));
            model.addAttribute("matiere_deliberee", matiereRepository.findById(matiereId).orElse(null));
            model.addAttribute("notesSaisies", notesSaisies);
            model.addAttribute("nbNotes", nbNotes);
            model.addAttribute("differenceCalculee", difference);
            model.addAttribute("detailsPaires", detailsPaires);
            model.addAttribute("detailsCalcul", detailsCalcul.toString());
            model.addAttribute("resolutionAppliquee", resolutionAppliquee);
            
            // Récupérer les notes existantes pour ce candidat et cette matière
            List<Note> notesExistantes = noteRepository.findByCandidatIdAndMatiereId(candidatId, matiereId);
            model.addAttribute("notesExistantes", notesExistantes);
            
        } catch (NumberFormatException e) {
            model.addAttribute("error", "Format de note invalide. Utilisez des nombres séparés par ; (ex: 14;12;15)");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        } catch (Exception e) {
            model.addAttribute("error", "Erreur inattendue: " + e.getMessage());
        }
        
        // Recharger les données pour le formulaire
        model.addAttribute("candidats", candidatRepository.findAll());
        model.addAttribute("matieres", matiereRepository.findAll());
        model.addAttribute("correcteurs", correcteurRepository.findAll());
        
        return "calcul";
    }
    
    /**
     * Détermine quelle résolution a été appliquée
     */
    private String determinerResolution(Double noteFinale, List<Double> notes) {
        if (notes == null || notes.isEmpty()) {
            return "inconnue";
        }
        
        double min = Collections.min(notes);
        double max = Collections.max(notes);
        double moyenne = notes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        // Utiliser une petite marge d'erreur pour les comparaisons de doubles
        double epsilon = 0.001;
        
        if (Math.abs(noteFinale - min) < epsilon) {
            return "plus petit";
        } else if (Math.abs(noteFinale - max) < epsilon) {
            return "plus grand";
        } else if (Math.abs(noteFinale - moyenne) < epsilon) {
            return "moyenne";
        } else {
            return "moyenne"; // Par défaut
        }
    }
    
    // ============= Méthodes supplémentaires utiles =============
    
    @GetMapping("/calcul/notes-candidat")
    @ResponseBody
    public List<Note> getNotesCandidat(@RequestParam Long candidatId, @RequestParam Long matiereId) {
        return noteRepository.findByCandidatIdAndMatiereId(candidatId, matiereId);
    }
    
    @GetMapping("/calcul/verifier-correcteurs")
    @ResponseBody
    public String verifierCorrecteurs(@RequestParam int nbNotes) {
        List<Correcteur> correcteurs = correcteurRepository.findAll();
        if (correcteurs.size() >= nbNotes) {
            return "OK";
        } else {
            return "Pas assez de correcteurs. Besoin: " + nbNotes + ", Disponibles: " + correcteurs.size();
        }
    }

    @PostMapping("/calcul/resultat-auto")
public String calculateFinalNoteAuto(
        @RequestParam Long candidatId,
        @RequestParam Long matiereId,
        Model model,
        RedirectAttributes redirectAttributes) {
    
    try {
        // Récupérer les notes existantes
        List<Note> notesExistantes = noteRepository.findByCandidatIdAndMatiereId(candidatId, matiereId);
        
        if (notesExistantes.isEmpty()) {
            throw new IllegalArgumentException("Aucune note existante pour ce candidat et cette matière");
        }
        
        // Construire la chaîne de notes séparées par ;
        StringBuilder notesSaisies = new StringBuilder();
        List<Double> notesList = new ArrayList<>();
        
        for (int i = 0; i < notesExistantes.size(); i++) {
            Double note = notesExistantes.get(i).getValeur();
            notesList.add(note);
            if (i > 0) notesSaisies.append(";");
            notesSaisies.append(note);
        }
        
        // Calculer la note finale
        Double noteFinale = calculationService.determinerNoteFinale(candidatId, matiereId);
        
        // Calculer la différence avec détails
        Double difference = 0.0;
        List<String> detailsPaires = new ArrayList<>();
        
        for (int i = 0; i < notesList.size(); i++) {
            for (int j = i + 1; j < notesList.size(); j++) {
                double diff = Math.abs(notesList.get(i) - notesList.get(j));
                difference += diff;
                detailsPaires.add(String.format("%.1f - %.1f = %.1f", 
                    notesList.get(i), notesList.get(j), diff));
            }
        }
        
        // Déterminer la résolution appliquée
        String resolutionAppliquee = determinerResolution(noteFinale, notesList);
        
        // Ajouter les attributs au modèle
        model.addAttribute("noteFinale", noteFinale);
        model.addAttribute("candidat_delibere", candidatRepository.findById(candidatId).orElse(null));
        model.addAttribute("matiere_deliberee", matiereRepository.findById(matiereId).orElse(null));
        model.addAttribute("notesSaisies", notesSaisies.toString());
        model.addAttribute("nbNotes", notesList.size());
        model.addAttribute("differenceCalculee", difference);
        model.addAttribute("detailsPaires", detailsPaires);
        model.addAttribute("resolutionAppliquee", resolutionAppliquee);
        model.addAttribute("typeCalcul", "Automatique (notes existantes)");
        model.addAttribute("notesExistantes", notesExistantes);
        
    } catch (Exception e) {
        model.addAttribute("error", "Erreur: " + e.getMessage());
    }
    
    // Recharger les données
    model.addAttribute("candidats", candidatRepository.findAll());
    model.addAttribute("matieres", matiereRepository.findAll());
    model.addAttribute("correcteurs", correcteurRepository.findAll());
    
    return "calcul";
}
}