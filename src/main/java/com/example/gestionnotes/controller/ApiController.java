package com.example.gestionnotes.controller;

import com.example.gestionnotes.entity.*;
import com.example.gestionnotes.repository.*;
import com.example.gestionnotes.service.NoteCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {
    
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
    
    @Autowired
    private OperateurRepository operateurRepository;
    
    @Autowired
    private ResolutionRepository resolutionRepository;
    
    @Autowired
    private NoteCalculationService calculationService;
    
    // ============= CRUD Note =============
    
    @GetMapping("/notes")
    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }
    
    @GetMapping("/notes/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable Long id) {
        return noteRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/notes")
    @ResponseStatus(HttpStatus.CREATED)
    public Note createNote(@RequestBody Note note) {
        return noteRepository.save(note);
    }
    
    @PutMapping("/notes/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable Long id, @RequestBody Note note) {
        return noteRepository.findById(id)
                .map(existingNote -> {
                    note.setId(id);
                    return ResponseEntity.ok(noteRepository.save(note));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/notes/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        if (noteRepository.existsById(id)) {
            noteRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    // ============= CRUD Parametre =============
    
    @GetMapping("/parametres")
    public List<Parametre> getAllParametres() {
        return parametreRepository.findAll();
    }
    
    @GetMapping("/parametres/{id}")
    public ResponseEntity<Parametre> getParametreById(@PathVariable Long id) {
        return parametreRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/parametres")
    @ResponseStatus(HttpStatus.CREATED)
    public Parametre createParametre(@RequestBody Parametre parametre) {
        return parametreRepository.save(parametre);
    }
    
    @PutMapping("/parametres/{id}")
    public ResponseEntity<Parametre> updateParametre(@PathVariable Long id, @RequestBody Parametre parametre) {
        return parametreRepository.findById(id)
                .map(existingParametre -> {
                    parametre.setId(id);
                    return ResponseEntity.ok(parametreRepository.save(parametre));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/parametres/{id}")
    public ResponseEntity<Void> deleteParametre(@PathVariable Long id) {
        if (parametreRepository.existsById(id)) {
            parametreRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    // ============= Endpoint pour le calcul de note finale =============
    
   @PostMapping("/calculer-note-finale")
public ResponseEntity<Map<String, Object>> calculerNoteFinale(
        @RequestBody Map<String, Object> payload) {
    
    try {
        Long idCandidat = Long.parseLong(payload.get("idCandidat").toString());
        Long idMatiere = Long.parseLong(payload.get("idMatiere").toString());
        String saisieNotes = payload.get("notes").toString();
        
        List<Long> idsCorrecteurs;
        
        if (payload.containsKey("idsCorrecteurs") && payload.get("idsCorrecteurs") != null) {
            // Récupérer les IDs des correcteurs fournis
            List<?> idsList = (List<?>) payload.get("idsCorrecteurs");
            idsCorrecteurs = idsList.stream()
                    .map(id -> Long.parseLong(id.toString()))
                    .toList();
        } else {
            // Utiliser des correcteurs par défaut
            List<Correcteur> correcteurs = correcteurRepository.findAll();
            if (correcteurs.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erreur", "Aucun correcteur disponible dans la base de données"));
            }
            
            // Limiter au nombre de notes saisies
            String[] notesArray = saisieNotes.split(";");
            int nbNotes = notesArray.length;
            
            idsCorrecteurs = correcteurs.stream()
                    .limit(nbNotes)
                    .map(Correcteur::getId)
                    .toList();
                    
            if (idsCorrecteurs.size() < nbNotes) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erreur", "Pas assez de correcteurs disponibles. Besoin: " + 
                                   nbNotes + ", Disponibles: " + correcteurs.size()));
            }
        }
        
        Double noteFinale = calculationService.traiterSaisieNotes(
                saisieNotes, idCandidat, idMatiere, idsCorrecteurs);
        
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("noteFinale", noteFinale);
        response.put("message", "Calcul effectué avec succès");
        response.put("candidatId", idCandidat);
        response.put("matiereId", idMatiere);
        response.put("notesSaisies", saisieNotes);
        response.put("nbCorrecteurs", idsCorrecteurs.size());
        
        return ResponseEntity.ok(response);
        
    } catch (NumberFormatException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("erreur", "Format de nombre invalide: " + e.getMessage()));
    } catch (ClassCastException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("erreur", "Format de données invalide pour les correcteurs"));
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("erreur", e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.badRequest()
                .body(Map.of("erreur", "Erreur inattendue: " + e.getMessage()));
    }
}
    
    // ============= Endpoints pour les données de référence =============
    
    @GetMapping("/candidats")
    public List<Candidat> getAllCandidats() {
        return candidatRepository.findAll();
    }
    
    @GetMapping("/matieres")
    public List<Matiere> getAllMatieres() {
        return matiereRepository.findAll();
    }
    
    @GetMapping("/correcteurs")
    public List<Correcteur> getAllCorrecteurs() {
        return correcteurRepository.findAll();
    }
    
    @GetMapping("operateurs")
    public List<Operateur> getAllOperateurs() {
        return operateurRepository.findAll();
    }
    
    @GetMapping("/resolutions")
    public List<Resolution> getAllResolutions() {
        return resolutionRepository.findAll();
    }
}