const modalSes = new bootstrap.Modal(document.getElementById('SessionModal'));
const form = document.getElementById('SessionForm');
const inputContainer = document.getElementById('inputContainer');
const inputLabel = document.getElementById('input_label');
const inputField = document.getElementById('input_value');
const sendInputBtn = document.getElementById('sendInput');
const button_start = document.getElementById('button_start');
const spinner = document.getElementById('spinner');
const qrContainer = document.getElementById("logInfoQRCode");
const enableForQRCode = document.getElementById("enableForQRCode");
const socket = new SockJS('/ws');
const stomp = Stomp.over(socket);


let currentInputType = null;

function openSessionModal() {
    form.reset();
    form.action = `/api/v1/session/start`;
    form.method = 'post';
    inputContainer.style.display = 'none';
    qrContainer.style.display = 'none';
    spinner.style.display = 'none';
    modalSes.show();
}

async function stopAvt() {
    inputContainer.style.display = 'none';
    inputField.value = '';
    button_start.disabled = false;
    spinner.style.display = 'none';
    enableForQRCode.disabled = false;
    enableForQRCode.checked = false;
    qrContainer.innerHTML = '';
    await fetch('/api/v1/session/stop', { method: 'POST' });
}

form.onsubmit = async function (e) {
    e.preventDefault();

    const formData = new FormData(form);
    try {
        button_start.disabled = true;
        enableForQRCode.disabled = true;
        spinner.style.display = 'block';
        const response = await fetch('/api/v1/session/start', {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            const errText = await response.text();
            await stopAvt();
            alert("Error: " + errText);
        } else {
            const resultText = await response.text();
            await stopAvt();
            alert("Successfully: " + resultText);
        }
    } catch (err) {
        await stopAvt();
        alert("Network error: " + err);
    }
};

stomp.connect({}, function (frame) {
    console.log('Connected: ' + frame);

    stomp.subscribe("/topic/sessionRequest", function (message) {
        const data = JSON.parse(message.body);
        if (data.type === "verification_code" || data.type === "password") {
            inputLabel.textContent = data.type === "verification_code"
                ? "Enter the code from Telegram"
                : "Enter your password (2FA)";
            inputContainer.style.display = 'block';
            currentInputType = data.type;
            spinner.style.display = 'none';
            qrContainer.innerHTML = '';
        }
    });

    stomp.subscribe("/topic/qrCode", function (message) {
        const data = JSON.parse(message.body);
        qrContainer.innerHTML = `
        <p>Scan the QR code using Telegram</p>
        <img src="${data}" alt="QR Code" />
        `;
        qrContainer.style.display = "block";
    });

    stomp.subscribe("/topic/checkService", function (message) {
        const data = JSON.parse(message.body);

        const el = document.querySelector(`[data-status-id=${data.serviceName}]`);
        if (el) {
            const isConnected = data.status === true;
            el.classList.toggle("connected", isConnected);
            el.classList.toggle("disconnected", !isConnected);
        }
    });

    stomp.subscribe("/topic/fatalLog", function (message) {
        const data = JSON.parse(message.body);
        updateHandlerStatus(data.handler_id, "Error", data.message);
    });

    stomp.subscribe("/topic/notifications", function (message) {
        const data = JSON.parse(message.body);

        addNotification(data.id, data.message, data.type, data.formattedDate);
    });

    stomp.subscribe("/topic/handlerWarning", function (message) {
        const data = JSON.parse(message.body);
        updateWarning(data);
    });

    stomp.subscribe("/topic/handler", function (message) {
        const data = JSON.parse(message.body);
        if (data.status === "run") {
            startHandler(data.id);
        }
        else {
            stopHandler(data.id);
        }
    });
});



sendInputBtn.addEventListener('click', async () => {
    const value = inputField.value;
    spinner.style.display = 'block';
    if (!currentInputType || !value) return;

    try {
        const formData = new FormData();
        formData.append("Type", currentInputType);
        formData.append("Value", value);

        const response = await fetch('/api/v1/session/input', {
            method: 'POST',
            body: formData
        });
        if (!response.ok) {
            const err = await response.text();
            spinner.style.display = 'none';
            alert("Input error: " + err);
        } else {
            spinner.style.display = 'none';
            inputContainer.style.display = 'none';
            inputField.value = '';
        }
    } catch (err) {
        await stopAvt();
        alert("Network error: " + err);
    }
});
