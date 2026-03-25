package com.forage.controller;

import com.forage.model.Client;
import com.forage.model.Demande;
import com.forage.service.ClientService;
import com.forage.service.DemandeService;
import com.forage.service.DemandeStatusService;
import com.forage.repository.StatusRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/demandes")
@RequiredArgsConstructor
public class DemandeController {

    private final DemandeService demandeService;
    private final ClientService clientService;
    private final DemandeStatusService demandeStatusService;
    private final StatusRepository statusRepository;

    /* ── Liste ── */
    @GetMapping
    public String list(@RequestParam(required = false) String search, Model model) {
        List<Demande> demandes;
        if (search != null && !search.isBlank()) {
            demandes = demandeService.search(search);
            model.addAttribute("search", search);
        } else {
            demandes = demandeService.findAll();
        }
        model.addAttribute("demandes", demandes);
        return "demandes/list";
    }

    /* ── Formulaire création ── */
    @GetMapping("/nouvelle")
    public String createForm(Model model) {
        Demande demande = new Demande();
        demande.setDate(LocalDate.now());
        model.addAttribute("demande", demande);
        model.addAttribute("clients", clientService.findAll());
        model.addAttribute("pageTitle", "Nouvelle Demande");
        return "demandes/form";
    }

    @PostMapping("/nouvelle")
    public String create(@Valid @ModelAttribute Demande demande,
                         BindingResult result,
                         @RequestParam Long clientId,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("clients", clientService.findAll());
            model.addAttribute("pageTitle", "Nouvelle Demande");
            return "demandes/form";
        }
        Client client = clientService.findById(clientId).orElse(null);
        if (client == null) {
            model.addAttribute("clients", clientService.findAll());
            model.addAttribute("errorMessage", "Client invalide.");
            return "demandes/form";
        }
        demande.setClient(client);
        demandeService.save(demande); // crée aussi le statut initial "En attente"
        redirectAttributes.addFlashAttribute("successMessage", "Demande créée avec succès !");
        return "redirect:/demandes";
    }

    /* ── Formulaire modification ── */
    @GetMapping("/{id}/modifier")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return demandeService.findById(id)
                .map(demande -> {
                    model.addAttribute("demande", demande);
                    model.addAttribute("clients", clientService.findAll());
                    model.addAttribute("pageTitle", "Modifier Demande");
                    return "demandes/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Demande introuvable.");
                    return "redirect:/demandes";
                });
    }

    @PostMapping("/{id}/modifier")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute Demande demande,
                         BindingResult result,
                         @RequestParam Long clientId,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("clients", clientService.findAll());
            model.addAttribute("pageTitle", "Modifier Demande");
            return "demandes/form";
        }
        Client client = clientService.findById(clientId).orElse(null);
        if (client == null) {
            model.addAttribute("clients", clientService.findAll());
            model.addAttribute("errorMessage", "Client invalide.");
            return "demandes/form";
        }
        demande.setId(id);
        demande.setClient(client);
        demandeService.save(demande);
        redirectAttributes.addFlashAttribute("successMessage", "Demande modifiée avec succès !");
        return "redirect:/demandes";
    }

    /* ── Détail + historique des statuts ── */
    @GetMapping("/{id}/detail")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return demandeService.findById(id)
                .map(demande -> {
                    model.addAttribute("demande", demande);
                    model.addAttribute("historique", demandeStatusService.findByDemandeId(id));
                    model.addAttribute("tousLesStatuts", statusRepository.findAll());
                    return "demandes/detail";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Demande introuvable.");
                    return "redirect:/demandes";
                });
    }

    /* ── Ajouter un statut depuis la page detail ── */
    @PostMapping("/{id}/statut/ajouter")
    public String ajouterStatut(@PathVariable Long id,
                                @RequestParam Long statusId,
                                @RequestParam(required = false) String commentaire,
                                RedirectAttributes redirectAttributes) {
        try {
            demandeStatusService.ajouterStatus(id, statusId, commentaire);
            redirectAttributes.addFlashAttribute("successMessage", "Statut ajouté avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur : " + e.getMessage());
        }
        return "redirect:/demandes/" + id + "/detail";
    }

    /* ── Supprimer une entrée de suivi ── */
    @PostMapping("/{demandeId}/statut/{statutId}/supprimer")
    public String supprimerStatut(@PathVariable Long demandeId,
                                  @PathVariable Long statutId,
                                  RedirectAttributes redirectAttributes) {
        demandeStatusService.deleteById(statutId);
        redirectAttributes.addFlashAttribute("successMessage", "Entrée supprimée.");
        return "redirect:/demandes/" + demandeId + "/detail";
    }

    /* ── Suppression demande ── */
    @PostMapping("/{id}/supprimer")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (demandeService.existsById(id)) {
            demandeService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Demande supprimée avec succès !");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Demande introuvable.");
        }
        return "redirect:/demandes";
    }
}
