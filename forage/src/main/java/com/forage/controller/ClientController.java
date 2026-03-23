package com.forage.controller;

import com.forage.model.Client;
import com.forage.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public String list(@RequestParam(required = false) String search, Model model) {
        List<Client> clients;
        if (search != null && !search.isBlank()) {
            clients = clientService.search(search);
            model.addAttribute("search", search);
        } else {
            clients = clientService.findAll();
        }
        model.addAttribute("clients", clients);
        model.addAttribute("totalClients", clientService.findAll().size());
        return "clients/list";
    }

    @GetMapping("/nouveau")
    public String createForm(Model model) {
        model.addAttribute("client", new Client());
        model.addAttribute("pageTitle", "Nouveau Client");
        return "clients/form";
    }

    @PostMapping("/nouveau")
    public String create(@Valid @ModelAttribute Client client,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Nouveau Client");
            return "clients/form";
        }
        clientService.save(client);
        redirectAttributes.addFlashAttribute("successMessage", "Client créé avec succès !");
        return "redirect:/clients";
    }

    @GetMapping("/{id}/modifier")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return clientService.findById(id)
                .map(client -> {
                    model.addAttribute("client", client);
                    model.addAttribute("pageTitle", "Modifier Client");
                    return "clients/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Client introuvable.");
                    return "redirect:/clients";
                });
    }

    @PostMapping("/{id}/modifier")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute Client client,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Modifier Client");
            return "clients/form";
        }
        client.setId(id);
        clientService.save(client);
        redirectAttributes.addFlashAttribute("successMessage", "Client modifié avec succès !");
        return "redirect:/clients";
    }

    @GetMapping("/{id}/detail")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return clientService.findById(id)
                .map(client -> {
                    model.addAttribute("client", client);
                    return "clients/detail";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Client introuvable.");
                    return "redirect:/clients";
                });
    }

    @PostMapping("/{id}/supprimer")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (clientService.existsById(id)) {
            clientService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Client supprimé avec succès !");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Client introuvable.");
        }
        return "redirect:/clients";
    }
}
