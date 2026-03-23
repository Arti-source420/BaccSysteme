package com.forage.controller;

import com.forage.model.Client;
import com.forage.model.Demande;
import com.forage.service.ClientService;
import com.forage.service.DemandeService;
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
        model.addAttribute("totalDemandes", demandeService.findAll().size());
        return "demandes/list";
    }

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
        demandeService.save(demande);
        redirectAttributes.addFlashAttribute("successMessage", "Demande créée avec succès !");
        return "redirect:/demandes";
    }

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

    @GetMapping("/{id}/detail")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return demandeService.findById(id)
                .map(demande -> {
                    model.addAttribute("demande", demande);
                    return "demandes/detail";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Demande introuvable.");
                    return "redirect:/demandes";
                });
    }

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
