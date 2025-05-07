let currentFilters = {};
// Функция для получения списка заявок
async function fetchTickets(page = 0, size = 10, filters = currentFilters) {
    const query = new URLSearchParams({ page, size, ...filters }).toString();
    const response = await fetch(`${window.config.apiUrl}/api/tickets/summary?${query}`, {
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
    });
    if (!response.ok) {
        showAlert('Ошибка при загрузке заявок. Пожалуйста, выполните вход в систему.');
        // $('#loginModal').modal('show');
        return;
    }
    const data = await response.json();
    updateTicketTable(data.content);
    updatePagination(data.totalPages, page);
}

// Обновление таблицы заявок
function updateTicketTable(tickets) {
    const ticketTableBody = document.getElementById('ticketTableBody');
    ticketTableBody.innerHTML = '';
    tickets.forEach(ticket => {
        const row = document.createElement('tr');

        // Проверяем статус заявки
        const isClosed = ticket.status === 'CLOSED';

        // Изменяем стиль строки, если заявка закрыта
        if (isClosed) {
            row.classList.add('closed-ticket');
        }


        row.innerHTML = `
            <td>${ticket.ticketNumber}</td>
            <td>${ticket.inventoryNumber}</td>
            <td>${translateTicketType(ticket.status)}</td>
            <td>${new Date(ticket.createdDate).toLocaleString()}</td>
            <td>${ticket.endDate ? new Date(ticket.endDate).toLocaleString() : 'Не закрыта'}</td>
            ${localStorage.getItem('userRole') === 'ADMIN' ?
            `<td>${ticket.user.firstName ? ticket.user.firstName + ' ' + ticket.user.lastName : ticket.guestDepartment}</td>
                 <td>${ticket.editorUser ? ticket.editorUser.firstName + ' ' + ticket.editorUser.lastName : 'Не назначен'}</td>` :
            `<td style="display: none;"></td>
                 <td style="display: none;"></td>`
        }
            <td>
                <button class="btn ${isClosed ? 'btn-secondary' : 'btn-warning'}" 
                        onclick="openModal('${ticket.ticketNumber}')">
                    ${isClosed ? 'Просмотр' : (localStorage.getItem('userRole') === 'ADMIN' ? 'Обработать' : 'Редактировать')}
                </button>
            </td>
        `;
        ticketTableBody.appendChild(row);
    });
}


// Обновление пагинации
function updatePagination(totalPages, currentPage) {
    const pagination = document.getElementById('pagination');
    pagination.innerHTML = '';

    const maxVisiblePages = 9;
    const sidePages = 4;

    // Рассчитываем диапазон отображаемых страниц
    let startPage = Math.max(0, currentPage - sidePages);
    let endPage = Math.min(totalPages - 1, currentPage + sidePages);

    // Корректируем диапазон, если страниц меньше 9 в диапазоне
    if (endPage - startPage < maxVisiblePages - 1) {
        const pagesToAdd = maxVisiblePages - 1 - (endPage - startPage);
        if (startPage === 0) {
            endPage = Math.min(totalPages - 1, endPage + pagesToAdd);
        } else if (endPage === totalPages - 1) {
            startPage = Math.max(0, startPage - pagesToAdd);
        }
    }

    // Кнопка "Первая"
    if (startPage > 0) {
        const firstLi = document.createElement('li');
        firstLi.className = 'page-item';
        firstLi.innerHTML = `<a class="page-link" href="#" onclick="fetchTickets(0, 10, currentFilters)">«</a>`;
        pagination.appendChild(firstLi);
    }

    // Основной диапазон страниц
    for (let i = startPage; i <= endPage; i++) {
        const li = document.createElement('li');
        li.className = `page-item ${i === currentPage ? 'active' : ''}`;
        li.innerHTML = `<a class="page-link" href="#" onclick="fetchTickets(${i}, 10, currentFilters)">${i + 1}</a>`;
        pagination.appendChild(li);
    }

    // Кнопка "Последняя"
    if (endPage < totalPages - 1) {
        const lastLi = document.createElement('li');
        lastLi.className = 'page-item';
        lastLi.innerHTML = `<a class="page-link" href="#" onclick="fetchTickets(${totalPages - 1}, 10, currentFilters)">»</a>`;
        pagination.appendChild(lastLi);
    }
}


// Функция перевода статуса на русский
const ticketTypeTranslations = {
    CREATED: 'Создана',
    IN_WORK: 'В работе',
    WAITING_FOR_SPARE_PARTS: 'Ожидание запчастей',
    READY: 'Готово',
    CLOSED: 'Закрыта'
};

function translateTicketType(ticketType) {
    return ticketTypeTranslations[ticketType] || 'Неизвестный статус';
}
