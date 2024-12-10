// Обработчики событий и инициализация
document.addEventListener('DOMContentLoaded', function () {
    const filterForm = document.getElementById('filterForm');
    const createTicketForm = document.getElementById('createTicketForm');
    const loginForm = document.getElementById('loginForm');

    // Инициализация с проверкой роли
    if (!localStorage.getItem('token')) {
        $('#loginModal').modal('show');
    } else {
        fetchTickets();
    }

    // Обработка фильтров
    filterForm.addEventListener('submit', function (event) {
        event.preventDefault();
        const filters = {
            status: document.getElementById('statusFilter').value,
            firstName: document.getElementById('firstName').value,
            lastName: document.getElementById('lastName').value,
            inventoryNumber: document.getElementById('inventoryNumberFilter').value,
        };
        // Удаляем пустые поля из фильтров
        Object.keys(filters).forEach(key => {
            if (!filters[key]) {
                delete filters[key];
            }
        });

        fetchTickets(0, 10, filters);
    });

    // Создание новой заявки
    createTicketForm.addEventListener('submit', function (event) {
        event.preventDefault();

        // Собираем данные вручную для отправки в формате JSON
        const ticketData = {
            descriptionOfTheProblem: createTicketForm.querySelector('[name="descriptionOfTheProblem"]').value,
            equipment: {
                inventoryNumber: createTicketForm.querySelector('[name="inventoryNumber"]').value
            },
            // Добавьте другие поля по аналогии
        };

        createTicket(ticketData);
    });
});

//Фильтры
document.addEventListener('DOMContentLoaded', () => {
    const filterToggleButton = document.getElementById('filterToggleButton');
    const filterPanel = document.getElementById('filterPanel');
    const closeFilterButton = document.getElementById('closeFilterButton');

    // Показать фильтры
    filterToggleButton.addEventListener('click', () => {
        filterPanel.classList.add('modal-active');
    });

    // Скрыть фильтры
    closeFilterButton.addEventListener('click', () => {
        filterPanel.classList.remove('modal-active');
    });

    // Закрытие при клике вне панели (опционально)
    document.addEventListener('click', (e) => {
        if (filterPanel.classList.contains('modal-active') && !filterPanel.contains(e.target) && e.target !== filterToggleButton) {
            filterPanel.classList.remove('modal-active');
        }
    });
});

//Проверка по инвентарному номеру
document.addEventListener('DOMContentLoaded', () => {
    const userRole = localStorage.getItem('role'); // Или декодируем из токена
    const checkInventoryButton = document.getElementById('checkInventoryButton');
    const inventoryCheckModal = document.getElementById('inventoryCheckModal');
    const closeModalButton = document.getElementById('closeModalButton');
    const inventoryCheckForm = document.getElementById('inventoryCheckForm');
    const inventoryResult = document.getElementById('inventoryResult');

    // Показ кнопки только для администратора
    if (localStorage.getItem('userRole') === 'ADMIN') {
        checkInventoryButton.classList.remove('d-none');
    }

    // Проверка инвентарного номера
    inventoryCheckForm.addEventListener('submit', (event) => {
        event.preventDefault();
        const inventoryNumber = document.getElementById('inventoryNumberInput').value;

        // Получение токена из localStorage
        const token = localStorage.getItem('token');
        if (!token) {
            inventoryResult.innerHTML = `
            <div class="alert alert-danger">
                Ошибка: отсутствует токен авторизации. Пожалуйста, войдите в систему.
            </div>`;
            return;
        }
        if (!inventoryNumber){
            inventoryResult.innerHTML = `
            <div class="alert alert-danger">
                Введите инвентарный номер.
            </div>`;
            return;
        }

        // Отправляем запрос на сервер
        fetch(`http://localhost:8080/api/equipments/${inventoryNumber}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
            .then((response) => {
                if (response.ok) {
                    return response.json(); // Успешный ответ
                } else if (response.status === 404) {
                    throw new Error(`Техника с инвентарным номером ${inventoryNumber} не найдена.`);
                } else {
                    throw new Error('Произошла ошибка на сервере.');
                }
            })
            .then((data) => {
                // Вывод данных из DTO
                inventoryResult.innerHTML = `
                <div class="alert alert-success">
                    <p><strong>Наименование:</strong> ${data.ОсновноеСредствоНаименование}</p>
                    <p><strong>Инвентарный номер:</strong> ${data.ИнвентарныйНомер}</p>
                    <p><strong>Ответственный:</strong> ${data.ЦМОНаименование}</p>
                    <p><strong>Счет учета:</strong> ${data.СчетУчета}</p>
                    <p><strong>КПС:</strong> ${data.КПС}</p>
                    <p><strong>КФО строка:</strong> ${data.КФОСтрока}</p>
                    <p><strong>КФО наименование:</strong> ${data.КФОНаим}</p>
                </div>`;
            })
            .catch((error) => {
                inventoryResult.innerHTML = `
                <div class="alert alert-danger">
                    Ошибка: ${error.message}
                </div>`;
            });
    });
});

//скрытие колонок для пользователей
document.addEventListener('DOMContentLoaded', function () {
    const userRole = localStorage.getItem('userRole'); // Или декодирование из токена, если требуется
    const creatorColumn = document.querySelectorAll('th:nth-child(6), td:nth-child(6)'); // 6-я колонка "Создатель"
    const editorColumn = document.querySelectorAll('th:nth-child(7), td:nth-child(7)'); // 7-я колонка "Редактор"

    if (userRole !== 'ADMIN') {
        // Скрываем колонки "Создатель" и "Редактор"
        creatorColumn.forEach(el => el.style.display = 'none');
        editorColumn.forEach(el => el.style.display = 'none');
    }
});





