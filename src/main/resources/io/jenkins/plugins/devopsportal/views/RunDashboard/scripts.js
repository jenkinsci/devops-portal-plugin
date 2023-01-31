
function getDeleteMessageTranslated() {
    let table = document.getElementById("dashboard-table");
    if (table) {
        return table.getAttribute("data-translate-popup-delete");
    }
    return null;
}

function confirmDelete(form) {
    let msg = getDeleteMessageTranslated() || "Are you sure to delete this deployment?";
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

function toggleModal(a) {
    let id = a.getAttribute("data-service-id");
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
