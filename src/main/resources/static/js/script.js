// script.js - Fonctions JavaScript pour l'interface

// Fonction pour valider le format des notes saisies
function validateNotes(input) {
    const notes = input.value.split(';').map(n => n.trim());
    let isValid = true;
    let errorMessage = '';
    
    for (let note of notes) {
        const noteNum = parseFloat(note);
        if (isNaN(noteNum) || noteNum < 0 || noteNum > 20) {
            isValid = false;
            errorMessage = 'Chaque note doit être un nombre entre 0 et 20';
            break;
        }
    }
    
    const feedback = document.getElementById('notesFeedback');
    if (!isValid) {
        input.classList.add('is-invalid');
        if (feedback) {
            feedback.textContent = errorMessage;
            feedback.style.display = 'block';
        }
    } else {
        input.classList.remove('is-invalid');
        input.classList.add('is-valid');
        if (feedback) {
            feedback.style.display = 'none';
        }
        
        // Afficher la différence calculée
        calculateDifference(notes);
    }
    
    return isValid;
}

// Fonction pour calculer et afficher la différence
function calculateDifference(notesArray) {
    if (notesArray.length < 2) return;
    
    const notes = notesArray.map(n => parseFloat(n));
    let difference = 0;
    
    for (let i = 0; i < notes.length; i++) {
        for (let j = i + 1; j < notes.length; j++) {
            difference += Math.abs(notes[i] - notes[j]);
        }
    }
    
    const diffDisplay = document.getElementById('differenceDisplay');
    if (diffDisplay) {
        diffDisplay.textContent = `Différence calculée : ${difference.toFixed(2)}`;
        diffDisplay.style.display = 'block';
    }
}

// Fonction pour charger les notes d'un candidat
function loadCandidatNotes(candidatId, matiereId) {
    if (!candidatId || !matiereId) return;
    
    fetch(`/api/notes/candidat/${candidatId}/matiere/${matiereId}`)
        .then(response => response.json())
        .then(notes => {
            const container = document.getElementById('existingNotes');
            if (container) {
                if (notes.length > 0) {
                    let html = '<h5>Notes existantes :</h5><ul>';
                    notes.forEach(note => {
                        html += `<li>${note.correcteur.prenom} ${note.correcteur.nom}: ${note.valeur}</li>`;
                    });
                    html += '</ul>';
                    container.innerHTML = html;
                } else {
                    container.innerHTML = '<p class="text-muted">Aucune note existante</p>';
                }
            }
        })
        .catch(error => console.error('Erreur:', error));
}

// Initialisation au chargement de la page
document.addEventListener('DOMContentLoaded', function() {
    // Ajouter les écouteurs d'événements
    const notesInput = document.getElementById('notesSaisies');
    if (notesInput) {
        notesInput.addEventListener('input', function() {
            validateNotes(this);
        });
    }
    
    const candidatSelect = document.getElementById('candidatId');
    const matiereSelect = document.getElementById('matiereId');
    
    if (candidatSelect && matiereSelect) {
        candidatSelect.addEventListener('change', function() {
            if (this.value && matiereSelect.value) {
                loadCandidatNotes(this.value, matiereSelect.value);
            }
        });
        
        matiereSelect.addEventListener('change', function() {
            if (this.value && candidatSelect.value) {
                loadCandidatNotes(candidatSelect.value, this.value);
            }
        });
    }
});