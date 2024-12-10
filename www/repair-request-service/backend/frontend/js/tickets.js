// Функция для получения списка заявок
async function fetchTickets(page = 0, size = 10, filters = {}) {
    const query = new URLSearchParams({page, size, ...filters}).toString();
    const response = await fetch(`http://localhost:8080/api/tickets/summary?${query}`, {
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
    });
    if (!response.ok) {
        showAlert('Ошибка при загрузке заявок. Пожалуйста, выполните вход в систему.');
        $('#loginModal').modal('show');
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
    for (let i = 0; i < totalPages; i++) {
        const li = document.createElement('li');
        li.className = `page-item ${i === currentPage ? 'active' : ''}`;
        li.innerHTML = `<a class="page-link" href="#" onclick="fetchTickets(${i})">${i + 1}</a>`;
        pagination.appendChild(li);
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
