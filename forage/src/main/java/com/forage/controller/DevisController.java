package com.forage.controller;

import com.forage.dto.DevisFormDto;
import com.forage.model.Devis;
import com.forage.model.DetailDevis;
import com.forage.repository.DemandeRepository;
import com.forage.repository.TypeDevisRepository;
import com.forage.service.DevisService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/devis")
@RequiredArgsConstructor
public class DevisController {

    private final DevisService        devisService;
    private final DemandeRepository   demandeRepository;
    private final TypeDevisRepository typeDevisRepository;

    // ════════════════════════════════════════════════
    // LISTE
    // ════════════════════════════════════════════════

    @GetMapping
    public String list(Model model) {
        model.addAttribute("devisList", devisService.findAll());
        return "devis/list";
    }

    // ════════════════════════════════════════════════
    // DÉTAIL
    // ════════════════════════════════════════════════

    @GetMapping("/{id}/detail")
    public String detail(@PathVariable Long id, Model model,
                         RedirectAttributes ra) {
        return devisService.findById(id).map(devis -> {
            BigDecimal total = devis.getDetails().stream()
                    .map(DetailDevis::getSousTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addAttribute("devis", devis);
            model.addAttribute("total", total);
            return "devis/detail";
        }).orElseGet(() -> {
            ra.addFlashAttribute("errorMessage", "Devis introuvable.");
            return "redirect:/devis";
        });
    }

    // ════════════════════════════════════════════════
    // FORMULAIRE CRÉATION
    // ════════════════════════════════════════════════

    @GetMapping("/nouveau")
    public String createForm(Model model) {
        DevisFormDto dto = new DevisFormDto();
        dto.setDate(LocalDate.now());
        // Une ligne vide par défaut pour que le tableau s'affiche
        DevisFormDto.LigneDto ligne = new DevisFormDto.LigneDto();
        ligne.setDate(LocalDate.now());
        dto.getLignes().add(ligne);
        populerModele(model, dto, "Nouveau Devis");
        return "devis/form";
    }

    @PostMapping("/nouveau")
    public String create(@Valid @ModelAttribute("devisForm") DevisFormDto dto,
                         BindingResult result,
                         Model model,
                         RedirectAttributes ra) {
        if (result.hasErrors()) {
            populerModele(model, dto, "Nouveau Devis");
            return "devis/form";
        }
        try {
            Devis saved = devisService.save(dto);
            ra.addFlashAttribute("successMessage", "Devis créé avec succès.");
            return "redirect:/devis/" + saved.getId() + "/detail";
        } catch (EntityNotFoundException | IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            populerModele(model, dto, "Nouveau Devis");
            return "devis/form";
        }
    }

    // ════════════════════════════════════════════════
    // FORMULAIRE MODIFICATION
    // ════════════════════════════════════════════════

    @GetMapping("/{id}/modifier")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return devisService.findById(id).map(devis -> {
            populerModele(model, versDto(devis), "Modifier le Devis #" + id);
            return "devis/form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("errorMessage", "Devis introuvable.");
            return "redirect:/devis";
        });
    }

    @PostMapping("/{id}/modifier")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("devisForm") DevisFormDto dto,
                         BindingResult result,
                         Model model,
                         RedirectAttributes ra) {
        dto.setId(id);
        if (result.hasErrors()) {
            populerModele(model, dto, "Modifier le Devis #" + id);
            return "devis/form";
        }
        try {
            devisService.save(dto);
            ra.addFlashAttribute("successMessage", "Devis modifié avec succès.");
            return "redirect:/devis/" + id + "/detail";
        } catch (EntityNotFoundException | IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            populerModele(model, dto, "Modifier le Devis #" + id);
            return "devis/form";
        }
    }

    // ════════════════════════════════════════════════
    // SUPPRESSION
    // ════════════════════════════════════════════════

    @PostMapping("/{id}/supprimer")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            devisService.deleteById(id);
            ra.addFlashAttribute("successMessage", "Devis supprimé.");
        } catch (EntityNotFoundException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/devis";
    }

    // ════════════════════════════════════════════════
    // AJAX — sous-total d'une ligne
    // POST /devis/ajax/sous-total?prixUnitaire=&quantite=
    // ════════════════════════════════════════════════

    @PostMapping("/ajax/sous-total")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> ajaxSousTotal(
            @RequestParam(required = false) BigDecimal prixUnitaire,
            @RequestParam(required = false) Integer quantite) {

        Map<String, Object> resp = new HashMap<>();

        if (prixUnitaire == null || quantite == null
                || prixUnitaire.compareTo(BigDecimal.ZERO) <= 0 || quantite <= 0) {
            resp.put("sousTotal", "0.00");
            resp.put("valide", false);
            return ResponseEntity.ok(resp);
        }

        BigDecimal st = prixUnitaire.multiply(BigDecimal.valueOf(quantite))
                                    .setScale(2, RoundingMode.HALF_UP);
        resp.put("sousTotal", st.toString());
        resp.put("valide", true);
        return ResponseEntity.ok(resp);
    }

    // ════════════════════════════════════════════════
    // AJAX — total général (toutes les lignes)
    // POST /devis/ajax/total   body: [{prixUnitaire, quantite}, …]
    // ════════════════════════════════════════════════

    @PostMapping("/ajax/total")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> ajaxTotal(
            @RequestBody List<Map<String, Object>> lignes) {

        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> l : lignes) {
            try {
                BigDecimal pu  = new BigDecimal(l.get("prixUnitaire").toString());
                int        qte = Integer.parseInt(l.get("quantite").toString());
                if (pu.compareTo(BigDecimal.ZERO) > 0 && qte > 0) {
                    total = total.add(pu.multiply(BigDecimal.valueOf(qte)));
                }
            } catch (Exception ignored) { /* ligne incomplète — ignorée */ }
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("total", total.setScale(2, RoundingMode.HALF_UP).toString());
        return ResponseEntity.ok(resp);
    }

    // ════════════════════════════════════════════════
    // UTILITAIRES PRIVÉS
    // ════════════════════════════════════════════════

    private void populerModele(Model model, DevisFormDto dto, String titre) {
        model.addAttribute("devisForm",  dto);
        model.addAttribute("demandes",   demandeRepository.findAllWithClient());
        model.addAttribute("typesDevis", typeDevisRepository.findAll());
        model.addAttribute("pageTitle",  titre);
        model.addAttribute("today",      LocalDate.now().toString());
    }

    /** Entité → DTO pour le formulaire d'édition. */
    private DevisFormDto versDto(Devis devis) {
        DevisFormDto dto = new DevisFormDto();
        dto.setId(devis.getId());
        dto.setDate(devis.getDate());
        if (devis.getDemande()   != null) dto.setDemandeId(devis.getDemande().getId());
        if (devis.getTypeDevis() != null) dto.setTypeDevisId(devis.getTypeDevis().getId());

        List<DevisFormDto.LigneDto> lignes = new ArrayList<>();
        for (DetailDevis d : devis.getDetails()) {
            DevisFormDto.LigneDto l = new DevisFormDto.LigneDto();
            l.setId(d.getId());
            l.setLibelle(d.getLibelle());
            l.setPrixUnitaire(d.getPrixUnitaire());
            l.setQuantite(d.getQuantite());
            l.setDate(d.getDate());
            lignes.add(l);
        }
        dto.setLignes(lignes);
        return dto;
    }
}
