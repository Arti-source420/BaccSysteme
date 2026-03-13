package com.example.gestionnotes.repository;

import com.example.gestionnotes.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByCandidatAndMatiere(Candidat candidat, Matiere matiere);
    
    @Query("SELECT n FROM Note n WHERE n.candidat.id = :idCandidat AND n.matiere.id = :idMatiere")
    List<Note> findByCandidatIdAndMatiereId(@Param("idCandidat") Long idCandidat, 
                                           @Param("idMatiere") Long idMatiere);
}