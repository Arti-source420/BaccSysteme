package com.example.gestionnotes.service;

import com.example.gestionnotes.entity.Parametre;
import com.example.gestionnotes.repository.ParametreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service responsable de la sélection de la règle (Parametre) à appliquer
 * selon la différence calculée, avec gestion des conflits.
 *
 * ──────────────────────────────────────────────────────────────────────────
 * ALGORITHME DE RÉSOLUTION DE CONFLIT
 * ──────────────────────────────────────────────────────────────────────────
 *
 * Problème : une même différence peut satisfaire PLUSIEURS règles en même
 * temps.  Exemple — différence = 7 avec les règles :
 *   • R1 : différence > 4  → plus grand
 *   • R2 : différence <= 8 → moyenne
 *   7 > 4  ✓   ET   7 <= 8  ✓  → conflit.
 *
 * Règle 1 — Proximité au seuil
 *   On retient la règle dont le seuil (valeur de différence paramétrée)
 *   est le plus PROCHE de la différence réelle.
 *     R1 : |7 - 4| = 3
 *     R2 : |7 - 8| = 1   ← plus proche → on applique R2 (moyenne)
 *
 * Règle 2 — Égalité de distance → seuil le plus petit
 *   Si deux règles sont à égale distance, on choisit celle dont le seuil
 *   paramétré est le plus PETIT.
 *   Exemple — différence = 6 :
 *     R1 : |6 - 4| = 2  (seuil = 4)
 *     R2 : |6 - 8| = 2  (seuil = 8)
 *   Distance égale → seuil min = 4 → on applique R1 (plus grand → note 16)
 * ──────────────────────────────────────────────────────────────────────────
 */
@Service
public class ParametreResolutionService {

    @Autowired
    private ParametreRepository parametreRepository;

    /**
     * Trouve le paramètre le mieux adapté à la différence calculée.
     *
     * 1. Filtre les règles dont la condition est satisfaite.
     * 2. S'il n'y a qu'une règle satisfaite → on l'applique directement.
     * 3. S'il y a plusieurs règles satisfaites → résolution de conflit :
     *    proximité au seuil, puis en cas d'ex-æquo, seuil le plus petit.
     *
     * @param idMatiere  identifiant de la matière
     * @param difference différence calculée entre les notes
     * @return le Parametre à appliquer, ou null si aucune règle ne correspond
     */
    public Parametre selectionnerParametre(Long idMatiere, Double difference) {
        List<Parametre> toutesLesRegles =
                parametreRepository.findByMatiereIdOrderByDifference(idMatiere);

        // 1. Filtrer les règles satisfaites
        List<Parametre> reglesSatisfaites = filtrerReglesSatisfaites(toutesLesRegles, difference);

        if (reglesSatisfaites.isEmpty()) {
            return null;
        }
        if (reglesSatisfaites.size() == 1) {
            return reglesSatisfaites.get(0);
        }

        // 2. Résolution de conflit
        return resoudreConflit(reglesSatisfaites, difference);
    }

    /**
     * Filtre les règles dont la condition (opérateur + seuil) est vérifiée
     * par la différence donnée.
     */
    private List<Parametre> filtrerReglesSatisfaites(List<Parametre> regles, Double difference) {
        List<Parametre> satisfaites = new ArrayList<>();
        for (Parametre p : regles) {
            if (conditionSatisfaite(difference, p.getDifference(), p.getOperateur().getSymbole())) {
                satisfaites.add(p);
            }
        }
        return satisfaites;
    }

    /**
     * Résout un conflit entre plusieurs règles satisfaites simultanément.
     *
     * Critère 1 : distance minimale entre la différence réelle et le seuil
     *             paramétré  → |différenceRéelle - seuilRègle|
     * Critère 2 (ex-æquo) : seuil paramétré le plus petit
     */
    private Parametre resoudreConflit(List<Parametre> reglesSatisfaites, Double difference) {
        Parametre meilleure = null;
        double distanceMin  = Double.MAX_VALUE;

        for (Parametre p : reglesSatisfaites) {
            double distance = Math.abs(difference - p.getDifference());

            if (meilleure == null) {
                meilleure   = p;
                distanceMin = distance;
                continue;
            }

            if (distance < distanceMin) {
                // Règle plus proche → elle gagne
                meilleure   = p;
                distanceMin = distance;

            } else if (Math.abs(distance - distanceMin) < 1e-9) {
                // Ex-æquo en distance → on prend le seuil le plus petit
                if (p.getDifference() < meilleure.getDifference()) {
                    meilleure = p;
                    // distanceMin reste identique
                }
            }
        }

        return meilleure;
    }

    /**
     * Évalue si «valeur [opérateur] seuil» est vraie.
     *
     * @param valeur    différence réelle
     * @param seuil     valeur de seuil configurée dans le paramètre
     * @param operateur symbole : "<", ">", "<=", ">="
     */
    public boolean conditionSatisfaite(Double valeur, Double seuil, String operateur) {
        return switch (operateur) {
            case "<"  -> valeur < seuil;
            case ">"  -> valeur > seuil;
            case "<=" -> valeur <= seuil;
            case ">=" -> valeur >= seuil;
            default   -> false;
        };
    }

    /**
     * Retourne une description lisible de la règle sélectionnée et du motif
     * de sélection (utile pour l'affichage dans la vue).
     *
     * @param idMatiere  identifiant de la matière
     * @param difference différence calculée
     * @return texte explicatif, ou "Aucune règle applicable" si null
     */
    public String expliquerSelection(Long idMatiere, Double difference) {
        List<Parametre> toutesLesRegles =
                parametreRepository.findByMatiereIdOrderByDifference(idMatiere);
        List<Parametre> satisfaites = filtrerReglesSatisfaites(toutesLesRegles, difference);

        if (satisfaites.isEmpty()) {
            return "Aucune règle applicable — moyenne appliquée par défaut";
        }
        if (satisfaites.size() == 1) {
            Parametre p = satisfaites.get(0);
            return String.format("1 règle satisfaite : différence %s %.1f → %s",
                    p.getOperateur().getSymbole(), p.getDifference(),
                    p.getResolution().getNom());
        }

        // Conflit détecté
        Parametre choisie = resoudreConflit(satisfaites, difference);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("⚠ Conflit : %d règles satisfaites simultanément :%n",
                satisfaites.size()));

        for (Parametre p : satisfaites) {
            double dist = Math.abs(difference - p.getDifference());
            boolean estChoisie = (p == choisie);
            sb.append(String.format("  • différence %s %.1f → %-12s (distance au seuil : %.1f)%s%n",
                    p.getOperateur().getSymbole(),
                    p.getDifference(),
                    p.getResolution().getNom(),
                    dist,
                    estChoisie ? "  ← CHOISIE" : ""));
        }

        sb.append(String.format("Règle retenue : %s %.1f → %s",
                choisie.getOperateur().getSymbole(),
                choisie.getDifference(),
                choisie.getResolution().getNom()));

        return sb.toString();
    }
}
