// Функция для отображения Bootstrap алертов
function showAlert(message, type) {
    const alertContainer = document.getElementById('alertContainer');

    // Проверяем, существует ли контейнер для алертов
    if (!alertContainer) return;

    // Создаем новый элемент для алерта
    const alert = document.createElement('div');
    alert.classList.add('toast');
    alert.classList.add(`text-bg-${type}`); // Используем цветовую схему от Bootstrap для типа алерта (например, bg-success, bg-danger)
    alert.classList.add('toast-hide'); // Класс для скрытия (необязательно, но может помочь в анимации)
    alert.setAttribute('role', 'alert');
    alert.setAttribute('aria-live', 'assertive');
    alert.setAttribute('aria-atomic', 'true');

    alert.innerHTML = `
        <div class="toast-header">
            <strong class="me-auto">Сообщение</strong>
            <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Закрыть"></button>
        </div>
        <div class="toast-body">
            ${message}
        </div>
    `;

    // Добавляем алерт в контейнер
    alertContainer.appendChild(alert);

    // Инициализация и отображение тоста с использованием Bootstrap JS
    const toast = new bootstrap.Toast(alert);
    toast.show();

    // Убираем алерт через 5 секунд
    setTimeout(() => {
        toast.hide();
        alert.remove();
    }, 5000);
}
