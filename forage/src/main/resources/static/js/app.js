// Forage App — JS

function openDeleteModal(actionUrl, itemName) {
  const modal = document.getElementById('deleteModal');
  const form  = document.getElementById('deleteForm');
  const text  = document.getElementById('deleteModalText');

  form.action = actionUrl;
  text.textContent = `Êtes-vous sûr de vouloir supprimer "${itemName}" ? Cette action est irréversible.`;
  modal.classList.add('show');
}

function closeDeleteModal() {
  document.getElementById('deleteModal').classList.remove('show');
}

// Close on overlay click
document.addEventListener('DOMContentLoaded', () => {
  const overlay = document.getElementById('deleteModal');
  if (overlay) {
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) closeDeleteModal();
    });
  }

  // Auto-dismiss alerts after 5s
  document.querySelectorAll('.alert').forEach(el => {
    setTimeout(() => {
      el.style.transition = 'opacity 0.5s ease';
      el.style.opacity = '0';
      setTimeout(() => el.remove(), 500);
    }, 5000);
  });
});
