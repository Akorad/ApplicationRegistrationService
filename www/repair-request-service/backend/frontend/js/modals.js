function openModal(ticketNumber) {
    fetch(`${window.config.apiUrl}/api/html/tickets/info/${ticketNumber}`, {
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
            // Обновление содержимого контейнера
            const modalContainer = document.getElementById('modalContainer');
            modalContainer.innerHTML = html;

            // Инициализация модального окна
            const modalElement = document.getElementById('ticketInfoModal');
            const modal = new bootstrap.Modal(modalElement);

            // Показываем модальное окно
            modal.show();

            // Скрываем поле поиска материалов для неадминистраторов
            const userRole = localStorage.getItem('userRole'); // Или декодирование из токена, если требуется
            if (userRole !== 'ADMIN') {
                document.getElementById("supplySearchInput").style.display = "none";
            }

            // Инициализация tooltips Bootstrap
                let tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
                tooltipTriggerList.map(function (tooltipTriggerEl) {
                    return new bootstrap.Tooltip(tooltipTriggerEl);
                });

            // Обработчик события закрытия модального окна
            modalElement.addEventListener('hidden.bs.modal', function () {
                // Сбрасываем фокус перед закрытием
                document.activeElement.blur(); // Снимаем фокус с текущего элемента

                // Удаляем все modal-backdrop
                const backdrops = document.querySelectorAll('.modal-backdrop.fade.show');
                backdrops.forEach(backdrop => backdrop.remove());

                // Удаляем модальное окно из DOM
                modalElement.remove();

                // Очищаем контейнер
                modalContainer.innerHTML = '';
            });
            // Инициализация tooltips Bootstrap
        })
        .catch(error => console.error('Ошибка:', error));
}

//Удаление заявки
async function deleteTicket(ticketNumber) {
    try {
        const response = await fetch(`${window.config.apiUrl}/api/tickets/delete/${ticketNumber}`, {
            method: "DELETE",
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });
1
        if (response.ok) {
            showAlert("Заявка успешно удалена");
            $('#ticketInfoModal').modal('hide'); // Закрыть модальное окно
            if (document.getElementById('ticketTableBody')){
                fetchTickets(); // Обновить список заявок
            }
        } else {
            const errorText = await response.text();
            showAlert(`Ошибка при удалении заявки: ${errorText}`);
        }
    } catch (error) {
        console.error("Ошибка выполнения запроса:", error);
        showAlert("Ошибка выполнения запроса. Проверьте соединение с сервером.");
    }
}


//обновление для пользователя
async function saveUserTicket(ticketNumber, descriptionOfTheProblem, inventoryNumber, userDepartment, userPhoneNumber, refiling) {
    try {
        const response = await fetch(`${window.config.apiUrl}/api/tickets/userUpdate`, {
            method: "POST",
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify({
                ticketNumber,
                descriptionOfTheProblem,
                inventoryNumber,
                userDepartment,
                userPhoneNumber,
                refiling
            })
        });

        if (response.ok) {
            showAlert("Заявка успешно сохранена");
            $('#ticketInfoModal').modal('hide'); // Закрыть модальное окно
            if (document.getElementById('ticketTableBody')){
                fetchTickets(); // Обновить список заявок
            }
        } else {
            const errorText = await response.text();
            showAlert(`Ошибка при сохранении заявки: ${errorText}`);
        }
    } catch (error) {
        console.error("Ошибка выполнения запроса:", error);
        showAlert("Ошибка выполнения запроса. Проверьте соединение с сервером.");
    }
}


//обновления для администратора
async function saveAdminTicket(ticketNumber, detectedProblem, comments, typeOfWork, status, supplies,
                               descriptionOfTheProblem, inventoryNumber, userDepartment, userPhoneNumber, refilling) {
    const payload = {
        ticketNumber,
        detectedProblem,
        comments,
        typeOfWork,
        status,
        supplies,
        descriptionOfTheProblem,
        inventoryNumber,
        userDepartment,
        userPhoneNumber,
        refilling
    };
    try {
        const response = await fetch(`${window.config.apiUrl}/api/tickets/update`, {
            method: "PUT", // Используем PUT для обновления
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            showAlert("Заявка успешно сохранена");
            $('#ticketInfoModal').modal('hide'); // Закрыть модальное окно
            if (document.getElementById('ticketTableBody')){
                fetchTickets(); // Обновить список заявок
            }
        } else {
            const errorText = await response.text();
            showAlert(`Ошибка при сохранении заявки: ${errorText}`);
        }
    } catch (error) {
        console.error("Ошибка выполнения запроса:", error);
        showAlert(`Ошибка выполнения запроса: ${error}`);

    }
}

//При нажатии кнопки "Удалить" или "Обновить"
document.addEventListener("DOMContentLoaded", function () {

    // Привязываем обработчики после появления модального окна
    $(document).on('shown.bs.modal', '#ticketInfoModal', function () {

        const showHistoryButton = document.getElementById('showHistoryButton');
        const historyTableContainer = document.getElementById('historyTableContainer');
        const historyTableBody = document.getElementById('historyTableBody');

        let isExpanded = false; // Флаг для отслеживания состояния кнопки и таблицы

        // Обработчик нажатия на кнопку "История заявок"
        showHistoryButton.addEventListener('click', function () {
            if (isExpanded) {
                // Если таблица уже открыта, скрываем её
                historyTableContainer.classList.remove('expanded');
                showHistoryButton.classList.remove('expanded');

            } else {
                // Если таблица закрыта, показываем её
                const inventoryNumber = document.getElementById('inventoryNumber').value;

                // Выполняем запрос к API
                fetch(`${window.config.apiUrl}/api/tickets/history/${inventoryNumber}`, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${localStorage.getItem('token')}` // Получаем токен из localStorage
                    }
                })
                    .then(response => {
                        if (!response.ok) {
                            throw new Error('Ошибка при получении истории заявок');
                        }
                        return response.json();
                    })
                    .then(data => {
                        // Очищаем таблицу перед добавлением новых данных
                        historyTableBody.innerHTML = '';

                        // Добавляем строки в таблицу
                        data.forEach(ticket => {
                            const row = document.createElement('tr');

                            // Номер заявки
                            const ticketNumberCell = document.createElement('td');
                            ticketNumberCell.textContent = ticket.ticketNumber;
                            row.appendChild(ticketNumberCell);

                            // Дата создания
                            const createdDateCell = document.createElement('td');
                            createdDateCell.textContent = formatDate(ticket.createdDate);
                            row.appendChild(createdDateCell);

                            // Дата закрытия
                            const endDateCell = document.createElement('td');
                            endDateCell.textContent = ticket.endDate ? formatDate(ticket.endDate) : '—';
                            row.appendChild(endDateCell);

                            // Использованные материалы
                            const suppliesCell = document.createElement('td');
                            if (ticket.supplies && ticket.supplies.length > 0) {
                                const suppliesList = ticket.supplies.map(supply => {
                                    const dateOfUse = formatDate(supply.dateOfUse);
                                    return `${supply.nomenclature} (${supply.quantity} шт., ${dateOfUse})`;
                                }).join('<br>'); // Каждый материал с новой строки
                                suppliesCell.innerHTML = suppliesList; // Используем innerHTML для поддержки <br>
                            } else {
                                suppliesCell.textContent = '—';
                            }
                            row.appendChild(suppliesCell);

                            // Кнопка "Открыть заявку"
                            const actionsCell = document.createElement('td');
                            const openButton = document.createElement('button');
                            openButton.textContent = 'Открыть заявку';
                            openButton.className = 'btn btn-primary btn-sm';
                            openButton.onclick = () => openModal(ticket.ticketNumber);
                            actionsCell.appendChild(openButton);
                            row.appendChild(actionsCell);

                            // Добавляем строку в таблицу
                            historyTableBody.appendChild(row);
                        });

                        // Показываем таблицу с анимацией
                        historyTableContainer.style.display = 'block';
                        setTimeout(() => {
                            historyTableContainer.classList.add('expanded');
                        }, 10); // Небольшая задержка для корректного запуска анимации
                    })
                    .catch(error => {
                        console.error('Ошибка:', error);
                        alert('Не удалось загрузить историю заявок');
                    });

                // Растягиваем кнопку
                showHistoryButton.classList.add('expanded');
            }

            // Меняем состояние флага
            isExpanded = !isExpanded;
        });
        // Функция для форматирования даты в формат "дд.мм.гг, чч:мм"
        function formatDate(dateString) {
            if (!dateString) return '—';
            const date = new Date(dateString);
            const day = String(date.getDate()).padStart(2, '0');
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const year = String(date.getFullYear()).slice(-2);
            const hours = String(date.getHours()).padStart(2, '0');
            const minutes = String(date.getMinutes()).padStart(2, '0');
            return `${day}.${month}.${year}, ${hours}:${minutes}`;
        }


        // Инициализация кнопок только после появления модального окна
        const saveButton = document.getElementById("saveButton");
        const deleteButton = document.getElementById("deleteButton");

        if (saveButton) {
            saveButton.addEventListener("click", () => {
                const ticketNumber = getTicketNumber();
                const role = localStorage.getItem('userRole'); // Получение роли из локального хранилища

                if (role === "ADMIN") {
                    const detectedProblem = document.getElementById("detectedProblem").value;
                    const comments = document.getElementById("comments").value;
                    const typeOfWork = document.getElementById("typeOfWork").value;
                    const status = document.getElementById("modalStatus").value;
                    const supplies = getSupplies(); // Функция для сбора данных по материалам
                    const descriptionOfTheProblem = document.getElementById("descriptionOfTheProblem").value;
                    const inventoryNumber = document.getElementById("inventoryNumber").value;
                    const userDepartment = document.getElementById("department").value;
                    const userPhoneNumber = document.getElementById("phoneNumber").value;
                    const refilling = document.getElementById('refillingSelectModal').checked;
                    saveAdminTicket(ticketNumber, detectedProblem, comments, typeOfWork, status, supplies,
                        descriptionOfTheProblem, inventoryNumber, userDepartment, userPhoneNumber, refilling);
                } else {
                    const descriptionOfTheProblem = document.getElementById("descriptionOfTheProblem").value;
                    const inventoryNumber = document.getElementById("inventoryNumber").value;
                    const userDepartment = document.getElementById("department").value;
                    const userPhoneNumber = document.getElementById("phoneNumber").value;
                    const refilling = document.getElementById('refillingSelectModal').checked;
                    saveUserTicket(ticketNumber, descriptionOfTheProblem, inventoryNumber, userDepartment, userPhoneNumber,refilling);
                }
            });
        }

        if (deleteButton) {
            deleteButton.addEventListener("click", () => {
                const ticketNumber = getTicketNumber(); // Получение номера заявки
                if (ticketNumber && confirm("Вы уверены, что хотите удалить эту заявку?")) {
                    deleteTicket(ticketNumber);
                } else {
                    showAlert("Не удалось получить номер заявки. Попробуйте еще раз.");
                }
            });
        } else {
            console.error("Кнопка 'Удалить' не найдена.");
        }

    });
});

//функция получения номера заявки
function getTicketNumber() {
    // Если номер заявки хранится в атрибуте data-* модального окна
    const modal = document.getElementById("ticketInfoModal");
    if (modal && modal.dataset.ticketNumber) {
        return modal.dataset.ticketNumber;
    }

    console.error("Номер заявки не найден.");
    return null;
}

//функция сбора данных по материалам
function getSupplies() {
    const supplies = [];
    const supplyElements = document.querySelectorAll("#selectedSupplies .selected-item");

    supplyElements.forEach(item => {
        const nomenclatureCode = item.getAttribute("data-nomenclature-code");
        const quantity = parseInt(item.querySelector("input").value, 10) || 0;

        if (nomenclatureCode && quantity > 0) {
            supplies.push({ nomenclatureCode, quantity });
        }
    });

    return supplies;
}

// Функция для открытия PDF
function openPdf(ticketNumber) {
    // Получаем токен из localStorage
    const token = localStorage.getItem('token');

    if (!token) {
        alert("Токен не найден!");
        return;
    }

    // Делаем запрос с передачей токена в заголовке
    fetch(`${window.config.apiUrl}/api/tickets/print/${ticketNumber}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(text || 'Неизвестная ошибка');
                });
            }
            return response.blob(); // Получаем ответ как Blob (PDF)
        })
        .then(blob => {
            // Создаем ссылку на PDF и открываем его в новой вкладке
            const url = URL.createObjectURL(blob);
            window.open(url, '_blank');
        })
        .catch(error => {
            console.error('Ошибка:', error);
        });
}


