// Функция для отправки данных формы создания заявки
function createTicket(ticketData) {
    fetch('http://localhost:8080/api/tickets/create', {
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
