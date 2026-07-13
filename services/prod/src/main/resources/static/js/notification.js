const notificationBtn = document.getElementById('notificationBtn');
const notificationDropdown = document.getElementById('notificationDropdown');
const notificationList = document.getElementById('notificationList');
const notificationCount = document.getElementById('notificationCount');
const clearBtn = document.getElementById('clearNotifications');

let notifications = [];

// Добавление уведомления
function addNotification(id, message, type = 'info', formattedTimestamp) {
    const notif = { id, message, type, formattedTimestamp };
    notifications.push(notif);

    const li = document.createElement('li');
    li.className = `list-group-item ${type === 'error' ? 'text-danger' : type === 'warning' ? 'text-warning' : 'text-light'} handler-card`;
    li.textContent = `[${notif.formattedTimestamp}] ${notif.message}`;

    notificationList.prepend(li);
    notificationCount.textContent = notifications.length;
    notificationCount.style.display = 'inline-block';
}

// Показ/скрытие дропдауна
notificationBtn.addEventListener('click', (e) => {
    e.stopPropagation(); // не закрывать сразу
    notificationDropdown.style.display = notificationDropdown.style.display === 'none' ? 'block' : 'none';
});

// Закрытие при клике вне дропдауна
document.addEventListener('click', (e) => {
    if (!notificationDropdown.contains(e.target) && e.target !== notificationBtn) {
        notificationDropdown.style.display = 'none';
    }
});

// Очистка уведомлений
clearBtn.addEventListener('click', () => {
    const ids = notifications.map(x => x.id);
    console.log(JSON.stringify({ ids }));
    fetch('/api/v1/notifications', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ ids })
    })
        .then(response => {
            notifications = [];
            notificationList.innerHTML = '';
            notificationCount.textContent = '0';
            notificationCount.style.display = 'none';
        })
        .catch(console.error);
});

fetch("/api/v1/notifications")
    .then(res => res.json())
    .then(list => list.forEach(n => addNotification(n.id, n.message, n.type, n.formattedDate)))
    .catch(err => console.error(err));
