const btn = document.getElementById("scrollToTopBtn");

function openLogTabFilter(filter) {
    const url = `/logs?filter=${filter}`;
    window.open(url, `_blank`);
}

window.onscroll = function () {
    if (document.body.scrollTop > 100 || document.documentElement.scrollTop > 100) {
        btn.style.display = "block";
    } else {
        btn.style.display = "none";
    }
};

btn.addEventListener("click", function () {
    window.scrollTo({ top: 0, behavior: "smooth" });
});

document.addEventListener('DOMContentLoaded', () => {
    const hoverTarget = document.getElementById('hoverTarget');
    const tooltipBox = document.getElementById('tooltipBox');
    let isPinned = false; // закреплен ли тултип

    function showTooltip() {
        // временно показываем для корректного расчёта ширины
        tooltipBox.style.display = 'block';
        const rect = hoverTarget.getBoundingClientRect();
        const tooltipWidth = tooltipBox.offsetWidth;
        tooltipBox.style.top = rect.bottom + window.scrollY + 5 + 'px';
        tooltipBox.style.left = rect.left + window.scrollX + rect.width + 5 + 'px';
        tooltipBox.classList.add('show');
    }

    function hideTooltip() {
        tooltipBox.classList.remove('show');
        tooltipBox.style.display = 'none';
    }

    // Hover: показываем, если не закреплено
    hoverTarget.addEventListener('mouseenter', () => {
        if (!isPinned) showTooltip();
    });

    hoverTarget.addEventListener('mouseleave', () => {
        if (!isPinned) hideTooltip();
    });

    // Click: закрепляем/открепляем
    hoverTarget.addEventListener('click', (e) => {
        e.stopPropagation(); // чтобы клик не сработал на document
        isPinned = !isPinned;
        if (isPinned) {
            showTooltip();
        } else {
            hideTooltip();
        }
    });

    // Click вне: скрываем, если закреплено
    document.addEventListener('click', (e) => {
        if (isPinned && !hoverTarget.contains(e.target) && !tooltipBox.contains(e.target)) {
            hideTooltip();
            isPinned = false;
        }
    });

    document.querySelectorAll('.warning-wrapper').forEach(wrapper => {
        const icon = wrapper.querySelector('i');
        const tooltip = wrapper.querySelector('.tooltip-box');

        if (!icon || !tooltip) return;

        document.body.appendChild(tooltip); // вынесли из карточки

        icon.addEventListener('mouseenter', () => {
            const rect = icon.getBoundingClientRect();
            tooltip.style.position = 'absolute';
            tooltip.style.top = rect.bottom + window.scrollY + 5 + 'px';
            tooltip.style.left = rect.left + window.scrollX + rect.width + 5 + 'px';
            tooltip.classList.add('show');
        });

        icon.addEventListener('mouseleave', () => {
            tooltip.classList.remove('show');
        });
    });
});

