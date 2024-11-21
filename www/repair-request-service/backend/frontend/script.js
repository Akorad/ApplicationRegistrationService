// Открытие модального окна с информацией о заявке
function openModal(ticketNumber) {
    fetch(`http://localhost:8080/api/html/tickets/info/${ticketNumber}`, {
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка при загрузке данных заявки');
            }
            return response.text();
        })
        .then(html => {
            document.getElementById('modalContainer').innerHTML = html; // Обновление содержимого контейнера
            $('#ticketInfoModal').modal('show'); // Показываем модальное окно
            console.log("Открытие модельного окна");
        })
        .catch(error => console.error('Ошибка:', error));
}

document.addEventListener('DOMContentLoaded', function () {
    const filterForm = document.getElementById('filterForm');
    const createTicketForm = document.getElementById('createTicketForm');
    const loginForm = document.getElementById('loginForm');
    const ticketTableBody = document.getElementById('ticketTableBody');
    const pagination = document.getElementById('pagination');

    // Получаем роль пользователя из localStorage
    let userRole = localStorage.getItem('userRole') || 'USER';

    // Функция для получения списка заявок
    async function fetchTickets(page = 0, size = 10, filters = {}) {
        const query = new URLSearchParams({page, size, ...filters}).toString();
        const response = await fetch(`http://localhost:8080/api/tickets/summary?${query}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });
        if (!response.ok) {
            alert('Ошибка при загрузке заявок. Пожалуйста, выполните вход в систему.');
            $('#loginModal').modal('show');
            return;
        }
        const data = await response.json();
        updateTicketTable(data.content);
        updatePagination(data.totalPages, page);
    }

    // Обновление таблицы заявок
    function updateTicketTable(tickets) {
        ticketTableBody.innerHTML = '';
        tickets.forEach(ticket => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${ticket.ticketNumber}</td>
                <td>${ticket.inventoryNumber}</td>
                <td>${translateTicketType(ticket.status)}</td>
                <td>${new Date(ticket.createdDate).toLocaleString()}</td>
                <td>${ticket.endDate ? new Date(ticket.endDate).toLocaleString() : 'Не закрыта'}</td>
                ${userRole === 'ADMIN' ? `
                    <td>${ticket.user.firstName} ${ticket.user.lastName}</td>
                    <td>${ticket.editorUser ? ticket.editorUser.firstName + ' ' + ticket.editorUser.lastName : 'Не назначен'}</td>
                ` : `
                    <td style="display: none;"></td>
                    <td style="display: none;"></td>
                `}
                <td>
                    <button class="btn btn-warning" onclick="openModal('${ticket.ticketNumber}')">
                        ${userRole === 'ADMIN' ? 'Обработать' : 'Редактировать'}
                    </button>
                </td>
            `;
            ticketTableBody.appendChild(row);
        });
    }

    // Обновление пагинации
    function updatePagination(totalPages, currentPage) {
        pagination.innerHTML = '';
        for (let i = 0; i < totalPages; i++) {
            const li = document.createElement('li');
            li.className = `page-item ${i === currentPage ? 'active' : ''}`;
            li.innerHTML = `<a class="page-link" href="#" onclick="fetchTickets(${i})">${i + 1}</a>`;
            pagination.appendChild(li);
        }
    }

    // Функция обработки фильтров
    filterForm.addEventListener('submit', function (event) {
        event.preventDefault();
        const filters = {
            status: document.getElementById('status').value,
            firstName: document.getElementById('firstName').value,
            lastName: document.getElementById('lastName').value,
            inventoryNumber: document.getElementById('inventoryNumber').value,
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
    createTicketForm.addEventListener('submit', async function (event) {
        event.preventDefault();

        // Собираем данные вручную для отправки в формате JSON
        const ticketData = {
            descriptionOfTheProblem: createTicketForm.querySelector('[name="descriptionOfTheProblem"]').value,
            equipment: {
                inventoryNumber: createTicketForm.querySelector('[name="inventoryNumber"]').value
            }
        };
        try {
            const response = await fetch('http://localhost:8080/api/tickets/create', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('token')}`
                },
                body: JSON.stringify(ticketData)  // Преобразуем объект в JSON-строку
            });

            if (response.ok) {
                alert('Заявка создана!');
                $('#createTicketModal').modal('hide');
                fetchTickets(); // Обновление списка заявок
            } else {
                const errorData = await response.json();
                alert(`Ошибка при создании заявки: ${errorData.error}`);
            }
        } catch (error) {
            console.error('Ошибка при отправке запроса:', error);
            alert('Произошла ошибка при соединении с сервером.');
        }
    });


    // Авторизация пользователя
    loginForm.addEventListener('submit', async function (event) {
        event.preventDefault();
        const formData = new FormData(loginForm);
        const requestData = {
            username: formData.get('username'),
            password: formData.get('password')
        };

        const response = await fetch('http://localhost:8080/auth/sign-in', {
            method: 'POST',
            body: JSON.stringify(requestData),
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('token', data.token);

            // Декодирование роли пользователя из токена
            const token = data.token;
            const parsedToken = parseJwt(token);

            if (parsedToken && parsedToken.role) { // Проверяем, что role определена
                userRole = parsedToken.role;
                localStorage.setItem('userRole', userRole);
                alert('Вход выполнен успешно!');
                $('#loginModal').modal('hide');
                fetchTickets(); // Обновление списка заявок после входа
            } else {
                console.error("Роль не найдена");
            }
        } else {
            alert('Ошибка при входе. Проверьте имя пользователя и пароль.');
        }
    });

    // Инициализация с проверкой роли
    if (!localStorage.getItem('token')) {
        $('#loginModal').modal('show');
    } else {
        fetchTickets();
    }

    // Инициализация Select2 при каждом открытии модального окна
    $('#ticketInfoModal').on('shown.bs.modal', function () {
        console.log("АБОБА");
        $('#supplySelect').select2({
            placeholder: "Выберите расходные материалы",
            allowClear: true,
            ajax: {
                url: 'http://localhost:8080/supplies/mol/Дроздова Татьяна Викторовна',
                dataType: 'json',
                processResults: function (data) {
                    return {
                        results: data.map(function (item) {
                            return {
                                id: item.НоменклатураКод,
                                text: item.Номенклатура
                            };
                        })
                    };
                }
            }
        });
    });

});

// Функция для перевода статуса на русский
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

// Функция декодирования токена JWT
function parseJwt(token) {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(function (c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));

    return JSON.parse(jsonPayload);
}

document.getElementById("loginButton").addEventListener("click", function () {
    // Здесь можно вызвать модальное окно для авторизации или выполнить запрос на сервер
    $('#loginModal').modal('show');
    alert("Окно авторизации");
});

document.getElementById("logoutButton").addEventListener("click", function () {
    // Здесь выполняем логаут, например, удалив токен из localStorage
    localStorage.removeItem('token');
    alert("Выход выполнен");
});

$(document).ready(function() {

    //селект 2
    const selectedSupplies = []; // Массив для хранения выбранных материалов и их количества
    let supplyData = []; // Глобальный массив для хранения всех данных

    $(document).on('shown.bs.modal', '#ticketInfoModal', function () {
        console.log("Модальное окно открыто");

        if (!$('#supplySelect').hasClass('select2-hidden-accessible')) {
            // Загружаем данные один раз
            if (supplyData.length === 0) {
                $.ajax({
                    url: 'http://localhost:8080/supplies/mol/Дроздова Татьяна Викторовна',
                    dataType: 'json',
                    success: function (data) {
                        supplyData = data.map(function (item) {
                            return {
                                id: item.НоменклатураКод,
                                text: item.Номенклатура
                            };
                        });
                        initializeSelect2(supplyData); // Инициализируем Select2 после загрузки данных
                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        console.error("Ошибка при загрузке данных:", textStatus, errorThrown);
                        alert("Ошибка при загрузке расходных материалов. Проверьте соединение с сервером.");
                    }
                });
            } else {
                initializeSelect2(supplyData); // Если данные уже загружены, инициализируем Select2
            }
        }

        updateSelectedSupplies(); // Обновить выбранные элементы, если были ранее добавлены
    });

    function initializeSelect2(data) {
        $('#supplySelect').select2({
            placeholder: "Выберите расходные материалы",
            allowClear: true,
            dropdownParent: $('#ticketInfoModal'),
            data: data // Передаём все загруженные данные
        });

        // Обработка события выбора расходного материала
        $('#supplySelect').on('select2:select', function (e) {
            const selectedItem = e.params.data;
            addSupply(selectedItem); // Добавление материала в список
            $('#supplySelect').val(null).trigger('change'); // Сброс выбора
        });
    }

    // Функция добавления расходного материала
    function addSupply(item) {
        if (selectedSupplies.some(supply => supply.id === item.id)) return; // Проверка на дубликаты
        const supply = { id: item.id, name: item.text, quantity: 1 };
        selectedSupplies.push(supply);
        updateSelectedSupplies();
    }

    // Обновление отображения выбранных материалов
    function updateSelectedSupplies() {
        $('#selectedSupplies').empty();
        selectedSupplies.forEach((supply, index) => {
            $('#selectedSupplies').append(`
                <div class="d-flex align-items-center mb-2">
                    <span class="mr-2">${supply.name}</span>
                    <input type="number" class="form-control mr-2" value="${supply.quantity}" min="1" style="width: 80px;"
                           data-index="${index}" />
                    <button class="btn btn-danger btn-sm remove-supply" data-index="${index}">Удалить</button>
                </div>
            `);
        });
    }

    // Удаление расходного материала
    $('#selectedSupplies').on('click', '.remove-supply', function () {
        const index = $(this).data('index');
        selectedSupplies.splice(index, 1);
        updateSelectedSupplies();
    });
});

