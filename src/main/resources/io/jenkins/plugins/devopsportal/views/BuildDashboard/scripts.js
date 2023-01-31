
function toggleVisibility(a) {
    let version = a.getAttribute("data-app-target");
    let items = document.querySelectorAll("tr.version-activities[version='" + version + "']");
    for (const item of items) {
        item.toggleAttribute('hidden');
    }
    items = document.querySelectorAll("tr.version-summary-tr[version='" + version + "']");
    for (const item of items) {
        item.toggleAttribute('hidden');
    }
}

function getDeleteMessageTranslated() {
    let table = document.getElementById("dashboard-table");
    if (table) {
        return table.getAttribute("data-translate-popup-delete");
    }
    return null;
}

function confirmDelete(form) {
    let msg = getDeleteMessageTranslated() || "Are you sure to delete this application version?";
    if (!confirm(msg)) {
        return false;
    }
    var input = document.createElement("input");
    input.setAttribute("type", "hidden");
    input.setAttribute("name", "origin");
    input.setAttribute("value", window.location.href);
    form.appendChild(input);
    return true;
}

function toggleModal(id) {
    var modal = document.getElementById('modal_' + id);
    var span = modal.querySelector('.close');
    modal.style.display = 'block';
    function close() {
        modal.style.display = 'none';
        span.removeEventListener('click', close);
        window.removeEventListener('click', windowClose);
    }
    function windowClose(event) {
        if (event.target === modal) {
            close();
        }
    }
    window.addEventListener('click', windowClose);
    span.addEventListener('click', close);
}
