
const startBtn = document.getElementById('startListener');
const stopBtn = document.getElementById('stopListener');
const spinnerListener = document.getElementById('spinnerListener');
const runningListener = document.getElementById('runningListener');
const source = new EventSource(`/api/v1/logs/status-listeners`);

source.onmessage = (event) => {
    const data = JSON.parse(event.data);
    document.getElementById("userListener").textContent = `${data.statUsers.count} | ${data.statUsers.date}`;
    document.getElementById("chatListener").textContent = `${data.statChats.count} | ${data.statChats.date}`;
    document.getElementById("adminChatsListener").textContent = `${data.statAdminChats.count} | ${data.statAdminChats.date}`;
    document.getElementById("giftsListener").textContent = `${data.statGifts.count} | ${data.statGifts.date}`;
    document.getElementById("reactionsListener").textContent = `${data.statReaction.count} | ${data.statReaction.date}`;
    document.getElementById("reactionsGeneralListener").textContent = `${data.statReactionGeneral.count} | ${data.statReactionGeneral.date}`;
    document.getElementById("messagesListener").textContent = `${data.statMessages.count} | ${data.statMessages.date}`;
    document.getElementById("messagesPropListener").textContent = `${data.statMessagesProp.count} | ${data.statMessagesProp.date}`;
    document.getElementById("messagesEntetListener").textContent = `${data.statMessagesEntet.count} | ${data.statMessagesEntet.date}`;
    document.getElementById("taskChatsListener").textContent = `${data.statTaskChats.count} | ${data.statTaskChats.date}`;
    document.getElementById("wordGroupAllListener").textContent = `${data.statWordGroupAll.count} | ${data.statWordGroupAll.date}`;
};

source.onerror = () => {
    console.error('The log stream has stopped');
};

window.addEventListener('beforeunload', () => {
    source.close();
});

async function startListener() {
    try {

        spinnerListener.style.display = 'block';
        startBtn.disabled = true;
        const response = await fetch('/api/v1/listener/start', {
            method: 'POST'
        });

        if (!response.ok) {
            startBtn.disabled = false;
            const errText = await response.text();
            alert("Error: " + errText);
        }
        else {
            runningListener.classList.add("connected");
            runningListener.classList.remove("disconnected");
            stopBtn.disabled = false;
        }
    } catch (err) {
        alert("Network error: " + err);
    }

    spinnerListener.style.display = 'none';
}

async function stopListener() {
    try {

        spinnerListener.style.display = 'block';
        stopBtn.disabled = true;
        const response = await fetch('/api/v1/listener/stop', {
            method: 'POST'
        });

        if (!response.ok) {
            stopBtn.disabled = false;
            const errText = await response.text();
            alert("Error: " + errText);
        }
        else {
            startBtn.disabled = false;
            runningListener.classList.add("disconnected");
            runningListener.classList.remove("connected");
        }
    } catch (err) {
        alert("Network error: " + err);
    }

    spinnerListener.style.display = 'none';
}