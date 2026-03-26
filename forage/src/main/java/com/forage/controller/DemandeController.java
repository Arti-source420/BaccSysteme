package com.forage.controller;

import com.forage.model.Client;
import com.forage.model.Demande;
import com.forage.repository.StatusRepository;
import com.forage.service.ClientService;
import com.forage.service.DemandeService;
import com.forage.service.DemandeStatusService;
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

    private final DemandeService       demandeService;
    private final ClientService        clientService;
    private final DemandeStatusService demandeStatusService;
    private final StatusRepository     statusRepository;

    /* ── Liste ───────────────────────────────────────── */
    @GetMapping
    public String list(@RequestParam(required = false) String search, Model model) {
        List<Demande> demandes = (search != null && !search.isBlank())
                ? demandeService.search(search)
                : demandeService.findAll();
        model.addAttribute("demandes", demandes);
        if (search != null) model.addAttribute("search", search);
        return "demandes/list";
    }

    /* ── Formulaire création ─────────────────────────── */
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
    public String create(@Valid @ModelAttribute("demande") Demande demande,
                         BindingResult result,
                         @RequestParam(value = "clientId", required = false) Long clientId,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        // Validation manuelle du client (non couvert par Bean Validation)
        if (clientId == null) {
            result.rejectValue("client", "required", "Le client est obligatoire");
        }

        if (result.hasErrors()) {
            model.addAttribute("clients", clientService.findAll());
            model.addAttribute("pageTitle", "Nouvelle Demande");
            return "demandes/form";
        }

        Client client = clientService.findById(clientId).orElse(null);
        if (client == null) {
            model.addAttribute("clients", clientService.findAll());
            model.addAttribute("pageTitle", "Nouvelle Demande");
            model.addAttribute("errorMessage", "Client sélectionné invalide.");
            return "demandes/form";
        }

        demande.setClient(client);
        demandeService.save(demande); // isNew=true → crée aussi le statut initial

        redirectAttributes.addFlashAttribute("successMessage", "Demande créée avec succès !");
        return "redirect:/demandes";
    }

    /* ── Formulaire modification ─────────────────────── */
    @GetMapping("/{id}/modifier")
    public String editForm(@PathVariable Long id, Model model,
                           RedirectAttributes redirectAttributes) {
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
                         @Valid @ModelAttribute("demande") Demande demande,
                         BindingResult result,
                         @RequestParam(value = "clientId", required = false) Long clientId,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        if (clientId == null) {
            result.rejectValue("client", "required", "Le client est obligatoire");
        }

        if (result.hasErrors()) {
            model.addAttribute("clients", clientService.findAll());
            model.addAttribute("pageTitle", "Modifier Demande");
            return "demandes/form";
        }

        Client client = clientService.findById(clientId).orElse(null);
        if (client == null) {
            model.addAttribute("clients", clientService.findAll());
            model.addAttribute("pageTitle", "Modifier Demande");
            model.addAttribute("errorMessage", "Client sélectionné invalide.");
            return "demandes/form";
        }

        // Charger l'existant, mettre à jour les champs, sauvegarder
        // → isNew=false car id != null, pas de doublon de statut
        demande.setId(id);
        demande.setClient(client);
        demandeService.save(demande);

        redirectAttributes.addFlashAttribute("successMessage", "Demande modifiée avec succès !");
        return "redirect:/demandes";
    }

    /* ── Détail + historique ─────────────────────────── */
    @GetMapping("/{id}/detail")
    public String detail(@PathVariable Long id, Model model,
                         RedirectAttributes redirectAttributes) {
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

    /* ── Ajouter un statut ───────────────────────────── */
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

    /* ── Supprimer une entrée de suivi ───────────────── */
    @PostMapping("/{demandeId}/statut/{statutId}/supprimer")
    public String supprimerStatut(@PathVariable Long demandeId,
                                  @PathVariable Long statutId,
                                  RedirectAttributes redirectAttributes) {
        demandeStatusService.deleteById(statutId);
        redirectAttributes.addFlashAttribute("successMessage", "Entrée supprimée.");
        return "redirect:/demandes/" + demandeId + "/detail";
    }

    /* ── Suppression demande ─────────────────────────── */
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
