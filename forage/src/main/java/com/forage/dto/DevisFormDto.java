package com.forage.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO de formulaire pour créer / modifier un devis.
 * Porte les champs scalaires du devis ET la liste de ses lignes de détail.
 * Jamais persisté directement : c'est le service qui mappe vers les entités JPA.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class DevisFormDto {

    /** Null à la création, renseigné à la modification. */
    private Long id;

    @NotNull(message = "La demande est obligatoire")
    private Long demandeId;

    @NotNull(message = "La date du devis est obligatoire")
    private LocalDate date;

    @NotNull(message = "Le type de devis est obligatoire")
    private Long typeDevisId;

    @Valid
    @NotEmpty(message = "Le devis doit contenir au moins une ligne de détail")
    private List<LigneDto> lignes = new ArrayList<>();

    // ────────────────────────────────────────────────────────────
    // Ligne imbriquée
    // ────────────────────────────────────────────────────────────

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class LigneDto {

        /** Null pour une nouvelle ligne, renseigné pour une existante (mode édition). */
        private Long id;

        @NotBlank(message = "Le libellé est obligatoire")
        private String libelle;

        @NotNull(message = "Le prix unitaire est obligatoire")
        @DecimalMin(value = "0.01", message = "Le prix unitaire doit être > 0")
        private BigDecimal prixUnitaire;

        @NotNull(message = "La quantité est obligatoire")
        @Min(value = 1, message = "La quantité minimum est 1")
        private Integer quantite;

        @NotNull(message = "La date de la ligne est obligatoire")
        private LocalDate date;

        /** Sous-total exposé dans le DTO — utile pour l'endpoint AJAX recap. */
        public BigDecimal getSousTotal() {
            if (prixUnitaire == null || quantite == null) return BigDecimal.ZERO;
            return prixUnitaire.multiply(BigDecimal.valueOf(quantite));
        }
    }
}
