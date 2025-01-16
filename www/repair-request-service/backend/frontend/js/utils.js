// Функция для отправки данных формы создания заявки
function createTicket(ticketData) {
    fetch(`${window.config.apiUrl}/api/tickets/create`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify(ticketData)  // Преобразуем объект в JSON-строку
    })
        .then(response => {
            if (response.ok) {
                showAlert('Заявка создана!');
                $('#createTicketModal').modal('hide');
                fetchTickets(); // Обновление списка заявок
            } else {
                response.json().then(errorData => {
                    showAlert(`Ошибка при создании заявки: ${errorData.error}`);
                });
            }
        })
        .catch(error => {
            console.error('Ошибка при отправке запроса:', error);
            showAlert('Произошла ошибка при соединении с сервером.');
        });
}

function getTicketInfo() {
    const departmentSelect = document.getElementById('departmentSelect');
    const phoneNumberInput = document.getElementById('phoneNumberSelect');

    departmentSelect.innerHTML = '';

    fetch(`${window.config.apiUrl}/api/users/getTicketInfo`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка при получении данных');
            }
            return response.json();
        })
        .then(data => {
            // Заполняем номер телефона
            phoneNumberInput.value = data.phoneNumber;

            // Разделяем строку департаментов по запятым
            const departments = data.department.split(',').map(dept => dept.trim());

            // Заполняем селект департаментами
            departments.forEach(dept => {
                const option = document.createElement('option');
                option.value = dept;
                option.textContent = dept;
                departmentSelect.appendChild(option);
            });
        })
        .catch(error => {
            console.error('Ошибка:', error);
            // Можно добавить уведомление пользователю об ошибке
        });
}
