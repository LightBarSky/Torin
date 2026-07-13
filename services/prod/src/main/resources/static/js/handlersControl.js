
let currentSubmitHandler = null;

window.addEventListener('DOMContentLoaded', () => {
    const hash = window.location.hash;
    if (hash) {
        const el = document.querySelector(hash);
        if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
});

function openLogTab(btn) {
    const handlerId = btn.dataset.key;
    const url = `/logs?handlerId=${handlerId}`;
    window.open(url, `_blank`);
}

function deleteHandler(id) {
    if (confirm("Are you sure you want to delete the handler??")) {
        fetch(`api/v1/handlers/${id}`, { method: 'DELETE' })
            .then(r => {
                if (r.ok) location.reload();
            });
    }
}

async function editHandler(id) {
    const response = await fetch(`api/v1/handlers/${id}`, { method: 'GET' });
    const handler = await response.json();
    openHandlerModal(handler);
}

async function openHandlerModal(handler = null) {

    const modalEl = document.getElementById('HandlerModal');
    const modal = new bootstrap.Modal(modalEl);
    const form = document.getElementById('HandlerForm');

    const select_session = document.getElementById('select_session');
    select_session.innerHTML = '<option value="">-- Select --</option>';
    try {
        const url = handler?.id ? `/api/v1/handlers/sessions-all?handlerId=${handler.id}` : `/api/v1/handlers/sessions-all`;
        const response = await fetch(url);
        if (!response.ok) throw new Error(`Network error: ${response.status}`);

        const sessions = await response.json();
        if (!Array.isArray(sessions)) {
            alert('Incorrect data: ' + JSON.stringify(sessions));
            return;
        }

        sessions.forEach(session => {
            const option = document.createElement('option');
            option.value = session.value;
            const ids = (session.handlersId || []).join(', ');
            if (ids) {
                option.text = `⚠ ${session.text} | ${session.lastModified} | ID: ${ids}`;
                option.dataset.warning = 'true';
                option.dataset.warningMessage = `The selected session is already in use: ${ids}`;
            }
            else {
                option.text = `${session.text} | ${session.lastModified}`;
                option.dataset.warning = 'false';
            }
            select_session.appendChild(option);
        });
    } catch (err) {
        alert('Error: ' + err.message);
    }

    form.reset();
    if (currentSubmitHandler) {
        form.removeEventListener('submit', currentSubmitHandler);
        currentSubmitHandler = null;
    }

    if (handler) {

        currentSubmitHandler = async function (e) {
            e.preventDefault();
            const formData = new FormData(form);
            const data = Object.fromEntries(formData.entries());
            const selected = select_session.selectedOptions[0];
            if (selected && selected.dataset.warning === 'true') {
                data.warning = selected.dataset.warningMessage;
            }
            try {
                const response = await fetch(`api/v1/handlers/${handler.id}`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(data)
                });

                if (response.ok) {
                    const result = await response.json();
                    alert('The data has been successfully updated!');
                    modal.hide();
                    window.location.href = `#handler_${result.id}`;
                    location.reload();
                } else {
                    const error = await response.json();
                    alert(`Error: ${error.message || 'Unknow error'}`);
                }
            } catch (error) {
                alert('Network error: ' + error.message);
            }
        };

        document.getElementById('HandlerLabel').textContent = 'Edit handler';

        document.getElementById('id_handler').value = handler.id;
        document.getElementById('select_category').value = handler.category;
        document.getElementById('Name').value = handler.nameHandler;
        document.getElementById('api_id').value = handler.apiId;
        document.getElementById('hash').value = handler.hash;
        select_session.value = handler.phone;
        select_session.text = handler.phone;
        const mediaCheckbox = document.getElementById("enableDirInputMedia");
        const mediaInput = document.getElementById("DirectoryForMedia");
        const mediaBlock = document.getElementById("dirInputBlockMedia");
        mediaCheckbox.checked = false;
        mediaBlock.style.display = "none";
        mediaInput.value = "";
        mediaInput.required = false;

        if (handler.directoryForMedia) {
            mediaCheckbox.checked = true;
            mediaBlock.style.display = "block";
            mediaInput.value = handler.directoryForMedia;
            mediaInput.required = true;
        }

        const photoCheckbox = document.getElementById("enableDirInputPhoto");
        const photoInput = document.getElementById("DirectoryForPhoto");
        const photoBlock = document.getElementById("dirInputBlockPhoto");
        photoCheckbox.checked = false;
        photoBlock.style.display = "none"
        photoInput.value = "";
        photoInput.required = false;

        if (handler.directoryForUserPhoto) {
            photoCheckbox.checked = true;
            photoBlock.style.display = "block";
            photoInput.value = handler.directoryForUserPhoto;
            photoInput.required = true;
        }

    }
    else {

        currentSubmitHandler = async function (e) {
            e.preventDefault();
            const formData = new FormData(form);
            const data = Object.fromEntries(formData);
            const selected = select_session.selectedOptions[0];
            if (selected && selected.dataset.warning === 'true') {
                data.warning = selected.dataset.warningMessage;
            }
            try {
                const response = await fetch(`api/v1/handlers`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(data)
                });

                if (response.ok) {
                    const result = await response.json();
                    alert('Handler create!');
                    modal.hide();
                    window.location.href = `#handler_${result.id}`;
                    location.reload();
                } else {
                    const error = await response.json();
                    alert(`Error: ${error.message || 'Unknow error'}`);
                }
            } catch (error) {
                alert('Network error: ' + error.message);
            }
        };

        document.getElementById("dirInputBlockMedia").style.display = "none";
        document.getElementById("dirInputBlockPhoto").style.display = "none";
        document.getElementById('HandlerLabel').textContent = 'New handler';
    }
    if (currentSubmitHandler) {
        form.addEventListener('submit', currentSubmitHandler);
    }
    modal.show();
}

const modalTog = document.getElementById("HandlerModal");
if (modalTog) {

    modalTog.querySelectorAll(".toggle-input").forEach(function (checkbox) {
        checkbox.addEventListener("change", function () {
            const targetSelector = this.dataset.target;
            const requiredSelector = this.dataset.required;
            const block = modalTog.querySelector(targetSelector);
            const input = modalTog.querySelector(requiredSelector);

            if (this.checked) {
                if (block) block.style.display = "block";
                if (input) input.setAttribute("required", "required");
            } else {
                if (block) block.style.display = "none";
                if (input) {
                    input.removeAttribute("required");
                    input.value = "";
                }
            }
        });
    });
}

document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".handler-block").forEach(block => {
        block.querySelectorAll(".status-badge").forEach(el => {
            const status = el.textContent.trim();
            const id = el.dataset.handlerId
            updateHandlerStatus(id, status);
        });
    });
});

function getStatusClass(status) {
    switch (status) {
        case 'Running':
            return 'bg-success text-white mb-2';
        case 'Stopped':
            return 'bg-secondary text-white mb-2';
        case 'Error':
            return 'bg-danger text-white mb-2';
        default:
            return 'bg-warning text-dark mb-2';
    }
}

async function startHandler(id) {
    const startBut = document.getElementById(`start_${id}`);
    if (startBut.disabled === true) return;
    startBut.disabled = true;
    document.getElementById(`setting_${id}`).disabled = true;
    document.getElementById(`delete_${id}`).disabled = true;
    document.getElementById(`spinner_${id}`).style.display = "block";

    try {
        const response = await fetch(`/api/v1/handlers/${id}/start`, { method: 'POST' });

        if (response.ok) {
            updateHandlerStatus(id, "Running");
            updateRunningHadlersCount("Running");
        } else {
            const result = await response.json();
            updateHandlerStatus(id, "Error", result?.message);
        }
    } catch (err) {
        updateHandlerStatus(id, "Error", err.message);
    }
}

async function stopHandler(id) {
    const stopBut = document.getElementById(`stop_${id}`);
    if (stopBut.disabled === true) return;
    stopBut.disabled = true;
    document.getElementById(`spinner_${id}`).style.display = "block";
    const response = await fetch(`/api/v1/handlers/${id}/stop`, { method: 'POST' });
    if (response.ok) {
        updateHandlerStatus(id, "Stopped");
        updateRunningHadlersCount("Stopped");
    }
    else {
        const result = await response.json();
        updateHandlerStatus(id, "Error", result.message);
    }
}

function updateHandlerStatus(handlerId, newStatus, newError) {
    document.getElementById(`spinner_${handlerId}`).style.display = "none";
    const span = document.getElementById(`status_${handlerId}`);
    if (!span) return;
    span.classList = [];
    span.classList.add('status-badge');
    span.classList.add(...getStatusClass(newStatus).split(' '));
    span.textContent = newStatus;

    switch (newStatus) {
        case 'Running':
            document.getElementById(`stop_${handlerId}`).removeAttribute('disabled');
            document.getElementById(`start_${handlerId}`).disabled = true;
            document.getElementById(`setting_${handlerId}`).disabled = true;
            document.getElementById(`delete_${handlerId}`).disabled = true;
            break;
        default:
            document.getElementById(`error_block_stat_${handlerId}`).text = "Last error:";
            document.getElementById(`stop_${handlerId}`).disabled = true;
            document.getElementById(`start_${handlerId}`).removeAttribute('disabled');
            document.getElementById(`setting_${handlerId}`).removeAttribute('disabled');
            document.getElementById(`delete_${handlerId}`).removeAttribute('disabled');
            break;
    }
    if (newError) {
        const error_stat = document.getElementById(`error_stat_${handlerId}`);
        if (error_stat) {
            error_stat.textContent = `${newError}`;
        }
    }
}

function updateRunningHadlersCount(mode) {
    const val = document.getElementById("runningHandlers");
    if (val) {
        let parts = val.textContent.split("/");
        let left = parseInt(parts[0], 10);
        let right = parseInt(parts[1], 10);
        if (mode === "Running") {
            left += 1;
        }
        else if (mode === "Stopped") {
            left -= 1;
        }
        val.textContent = `${left}/${right}`;
    }
}

function updateWarning(message) {
    const warningIcon = document.getElementById(`warning-icon-${message.id}`);
    const warningTooltip = document.getElementById(`warningBox_${message.id}`);
    if (message.message) {
        warningIcon.style.display = 'inline';
        const span = warningTooltip.querySelector('span');
        span.textContent = message.message;
    } else {
        warningContainer.style.display = 'none';
    }
}

async function runAll() {
    try {
        const response = await fetch(`/api/v1/handlers/run-all`, { method: 'POST' });

    } catch (err) {
        alert("Fetch error: ", err);
    }
}

async function stopAll() {
    try {
        const response = await fetch(`/api/v1/handlers/stop-all`, { method: 'POST' });

    } catch (err) {
        alert("Fetch error: ", err);
    }
}

