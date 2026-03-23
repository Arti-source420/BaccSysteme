package com.forage.controller;

import com.forage.service.ClientService;
import com.forage.service.DemandeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ClientService clientService;
    private final DemandeService demandeService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("totalClients", clientService.findAll().size());
        model.addAttribute("totalDemandes", demandeService.findAll().size());
        model.addAttribute("recentDemandes", demandeService.findAll().stream().limit(5).toList());
        return "index";
    }
}
