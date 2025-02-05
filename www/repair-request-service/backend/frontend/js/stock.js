document.addEventListener("DOMContentLoaded", async function () {
    const materialsTableBody = document.getElementById("materialsTableBody");
    const searchInput = document.getElementById("materialSearch");
    let materialsData = []; // коллекция для поиска
    let currentMaterialId = null; // Для хранения текущего ID материала

    // Загрузка материалов из 1С
    async function loadMaterialsFrom1C() {
        try {
            const response = await fetch(`${window.config.apiUrl}/api/supplies/mol/Дроздова Татьяна Викторовна`, {
                headers: {
                    "Authorization": `Bearer ${localStorage.getItem('token')}`
                }
            });
            if (!response.ok) throw new Error("Ошибка загрузки данных из 1С");

            const data = await response.json();
            return data.map(item => normalizeNomenclature(item, '1C'));
        } catch (error) {
            console.error("Ошибка загрузки данных из 1С:", error);
            showAlert("Ошибка при загрузке данных из 1С.");
            return [];
        }
    }

// Загрузка материалов со склада
    async function loadMaterialsFromStock() {
        try {
            const response = await fetch(`${window.config.apiUrl}/api/stock-supplies/all`, {
                headers: {
                    "Authorization": `Bearer ${localStorage.getItem('token')}`
                }
            });
            if (!response.ok) throw new Error("Ошибка загрузки материалов со склада");

            const data = await response.json();
            return data.map(item => normalizeNomenclature(item, 'Склад'));
        } catch (error) {
            console.error("Ошибка загрузки данных со склада:", error);
            showAlert("Ошибка при загрузке данных со склада.");
            return [];
        }
    }

// Объединение данных из 1С и склада по номенклатурному коду
    function mergeMaterials(onescData, stockData) {
        const mergedData = [];

        // Создаём карту по коду номенклатуры для материалов со склада
        const stockMap = stockData.reduce((acc, item) => {
            if (item.nomenclatureCode) {
                acc[item.nomenclatureCode] = item;
            }
            return acc;
        }, {});

        onescData.forEach(item => {
            if (item.nomenclatureCode) {
                const stockItem = stockMap[item.nomenclatureCode];

                // Если материал есть на складе, добавляем количество со склада
                if (stockItem) {
                    // Добавляем материал из 1С с количеством со склада
                    mergedData.push({
                        ...item,
                        quantityStock: stockItem.quantity || 0
                    });
                    // Убираем из stockMap, чтобы не было дублирования
                    delete stockMap[item.nomenclatureCode];
                } else {
                    // Если на складе нет материала, добавляем только данные из 1С
                    mergedData.push({
                        ...item,
                        quantityStock: 0
                    });
                }
            }
        });

        // Добавляем оставшиеся материалы из склада
        for (let code in stockMap) {
            const stockItem = stockMap[code];
            mergedData.push({
                ...stockItem,
                quantity: 0, // Для материалов, которые есть только на складе, но нет в 1С
                quantityStock: stockItem.quantity || 0 // Если материал только на складе, то его количество
            });
        }
        materialsData = mergedData;
        return mergedData;
    }


// Загрузка всех данных и рендеринг
    async function loadMaterials() {
        const onescMaterials = await loadMaterialsFrom1C();
        const stockMaterials = await loadMaterialsFromStock();

        // Объединяем данные из 1С и склада
        const materialsData = mergeMaterials(onescMaterials, stockMaterials);

        renderMaterials(materialsData); // Рендерим таблицу с объединёнными данными
    }

// Рендеринг таблицы материалов
    function renderMaterials(data) {
        materialsTableBody.innerHTML = ""; // Очищаем таблицу перед рендерингом
        data.forEach(material => {
            const row = document.createElement("tr");

            // Пути к изображению
            const imageUrl = material.nomenclatureCode ? `${window.config.apiUrl}/images/${material.nomenclatureCode}.jpg` : '';
            const defaultImageUrl = `${window.config.apiUrl}/images/default-material.jpg`;

            // Проверяем, что материал имеет нужные данные, прежде чем их отображать
            row.innerHTML = `
            <td class="image-cell" style="position: relative;">
                <img src="${imageUrl || defaultImageUrl}" 
                     alt="${material.nomenclature || 'Неизвестный материал'}" 
                     class="material-image"
                     onclick="openImageModal('${material.nomenclatureCode || ''}')"
                     onerror="this.onerror=null;this.src='${defaultImageUrl}';">
                <button class="edit-image-btn" onclick="openEditModal('${material.nomenclatureCode || ''}')">
                    🖊️
                </button>
            </td>
            <td>${material.nomenclature || 'Неизвестная номенклатура'}</td>
            <td>${material.quantity || 0}</td>
            <td>${material.quantityStock || 0}</td>
            <td>
                <button class="btn btn-info btn-sm me-2" onclick="openRequestsModal('${material.nomenclatureCode || ''}')">Заявки</button>
                <button class="btn btn-success btn-sm" onclick="openIssueModal('${material.nomenclatureCode || ''}')">Выдача</button>
            </td>
        `;
            materialsTableBody.appendChild(row);
        });
    }

    // Фильтрация материалов
    searchInput.addEventListener("input", () => {
        const query = searchInput.value.toLowerCase();
        const filteredMaterials = materialsData.filter(material =>
            material.nomenclature && material.nomenclature.toLowerCase().includes(query)
        );
        renderMaterials(filteredMaterials); // Отображаем только отфильтрованные материалы
    });


    // Открытие модального окна со списком заявок
    window.openRequestsModal = async function (nomenclatureCode) {
        const requestsList = document.getElementById("requestsList");
        requestsList.innerHTML = "Загрузка...";
        try {
            const response = await fetch(`${window.config.apiUrl}/api/supplies/nomenclatureCode/${nomenclatureCode}`, {
                headers: {
                    "Authorization": `Bearer ${localStorage.getItem('token')}`
                }
            });
            if (!response.ok) throw new Error("Ошибка загрузки заявок");

            const requests = await response.json();
            if (requests.length === 0) {
                showAlert ("Заявки с этим расходным материалом не найдены.");
                return;
            }

            // Сортировка заявок по дате использования (по убыванию)
            requests.sort((a, b) => new Date(b.dateOfUse) - new Date(a.dateOfUse));

            // Создаем таблицу
            const table = document.createElement("table");
            table.className = "table table-bordered table-hover";

            // Устанавливаем стили для горизонтальных границ
            table.style.borderCollapse = "collapse";
            table.style.borderWidth = "1px 0"; // Только горизонтальные границы

            // Создаем заголовок таблицы
            const thead = document.createElement("thead");
            thead.innerHTML = `
        <tr>
            <th>Где использовалось</th>
            <th>Дата использования</th>
            <th>Количество</th>
            <th>Действие</th>
        </tr>
    `;
            table.appendChild(thead);

            // Создаем тело таблицы
            const tbody = document.createElement("tbody");

            requests.forEach(request => {
                const row = document.createElement("tr");
                row.innerHTML = `
<!--            <td>#${request.ticketNumber}</td>-->
            <td>${request.ticketNumber ? request.inventoryNumber + ' (#' + request.ticketNumber +')' : request.molName || 'Не указано'}</td>
            <td>${new Date(request.dateOfUse).toLocaleString()}</td>
            <td>${request.quantity} шт.</td>
            <td>
                <button class="btn btn-primary btn-sm" onclick="openModal('${request.ticketNumber}')">
                    Открыть
                </button>
            </td>
        `;
                tbody.appendChild(row);
            });
            table.appendChild(tbody);

            // Очищаем контейнер и добавляем таблицу
            requestsList.innerHTML = "";
            requestsList.appendChild(table);
        } catch (error) {
            requestsList.innerHTML = "Ошибка загрузки заявок";
            console.error("Ошибка загрузки заявок:", error);
        }

        // Показываем модальное окно
        const requestsModal = new bootstrap.Modal(document.getElementById("requestsModal"));
        requestsModal.show();
    };


    //Открытие модельного окна для введения ссылки изображения
    window.openEditModal = function(materialId) {
        currentMaterialId = materialId; // Сохраняем id материала
        document.getElementById('imageUrlInput').value = ''; // Очищаем поле ввода
        new bootstrap.Modal(document.getElementById('imageEditModal')).show();
    };

    // Открытие модального окна с изображением
    window.openImageModal = function(imageId) {
        const modalImage = document.getElementById('modalImage');
        modalImage.src = `${window.config.apiUrl}/images/${imageId}.jpg`;
        const imageModal = new bootstrap.Modal(document.getElementById('imageModal'));
        imageModal.show();
    }

    // Функция отправки изображения на сервер
    window.saveImageUrl =  function()  {
        const newImageUrl = document.getElementById('imageUrlInput').value;

        if (!newImageUrl) {
            showAlert('Пожалуйста, введите ссылку на изображение!');
            return;
        }

        // Формируем объект для отправки
        const imageRequest = {
            nomenclatureCode: currentMaterialId,
            imageUrl: newImageUrl
        };

        // Отправляем запрос на сервер для сохранения или обновления URL изображения
        fetch(`${window.config.apiUrl}/api/supplies-image/update-supplies-images`, {
            method: 'POST',
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify(imageRequest)
        })
            .then(response => response.text()) // Ожидаем текст вместо JSON
            .then(message => {
                // Найти модальное окно
                const modalElement = document.getElementById('imageModal');
                const modalInstance = bootstrap.Modal.getInstance(modalElement);

                if (modalInstance) {
                    modalInstance.hide(); // Закрыть уже существующее модальное окно
                }

                showAlert(message); // Выводим текст сообщения
            })
            .catch(error => {
                console.error('Ошибка при сохранении изображения:', error);
                showAlert('Ошибка при сохранении изображения: ' + error.message);
            });
    }


    // Открытие модального окна с созданием заявки
    window.openIssueModal = function (nomenclatureCode) {
        const modal = new bootstrap.Modal(document.getElementById("suppliesModal"));
        // Заполняем данные формы (если нужно)
        document.getElementById("nomenclatureCodeInventoryCreate").value = nomenclatureCode;
        document.getElementById("nomenclatureCodeMolCreate").value = nomenclatureCode;

        modal.show();
    };
    window.showByInventoryForm = function () {
        document.getElementById("byInventoryForm").classList.remove("hidden");
        document.getElementById("byMolForm").classList.add("hidden");
    };

    window.showByMolForm = function () {
        document.getElementById("byMolForm").classList.remove("hidden");
        document.getElementById("byInventoryForm").classList.add("hidden");
    };

    function closeModal() {
        $('#suppliesModal').modal('hide');
    }

    //функция выдачи по инвентарному номеру
    window.submitByInventory=function() {
        const request = {
            inventoryNumber: document.getElementById("inventoryNumberCreate").value,
            nomenclatureCode: document.getElementById("nomenclatureCodeInventoryCreate").value,
            comment: document.getElementById("commentInventoryCreate").value,
            quantity: parseInt(document.getElementById("quantityInventoryCreate").value, 10),
        };

        fetch(`${window.config.apiUrl}/api/SuppliesIssue/create/byInventory`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify(request),
        })
            .then((response) => {
                if (!response.ok) {
                    return response.text().then((text) => { throw new Error(text); });
                }
                loadMaterials();
                return response.json();
            })
            .then((data) => {
                showAlert(`Успех: ${data.message}`);
                closeModal();
            })
            .catch((error) => showAlert("Ошибка: " + error.message));
    }

    //функция выдачи по МОЛ
    window. submitByMol=function() {

        const molName = document.getElementById("molNameCreate").value.trim();

        // Проверка заполненности поля molNameCreate
        if (!molName) {
            showAlert("Ошибка: Поле 'ФИО МОЛ' не может быть пустым!");
            return;
        }

        const request = {
            molName: document.getElementById("molNameCreate").value,
            comment: document.getElementById("commentMolCreate").value,
            nomenclatureCode: document.getElementById("nomenclatureCodeMolCreate").value,
            quantity: parseInt(document.getElementById("quantityMolCreate").value, 10),
        };

        console.log(request);
        fetch(`${window.config.apiUrl}/api/SuppliesIssue/create/byMol`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify(request),
        })
            .then((response) => {
                if (!response.ok) {
                    return response.text().then((text) => { throw new Error(text); });
                }
                loadMaterials();
                return response.json();
            })
            .then((data) => {
                showAlert(`Успех: ${data.message}`);
                closeModal();
            })
            .catch((error) => showAlert("Ошибка: " + error.message));
    }

    // Применение фильтра по датам
    window.applyDateFilter = async function () {
        const startDate = document.getElementById("startDate").value;
        const endDate = document.getElementById("endDate").value;

        // Проверяем, чтобы даты были валидными
        if (!startDate || !endDate) {
            showAlert("Пожалуйста, выберите обе даты.");
            return;
        }

        // Формируем запрос с фильтрацией по датам
        try {
            const response = await fetch(`${window.config.apiUrl}/api/supplies/filterByDate?startDate=${startDate}&endDate=${endDate}`, {
                headers: {
                    "Authorization": `Bearer ${localStorage.getItem('token')}`
                }
            });
            if (!response.ok) throw new Error("Ошибка загрузки расходных материалов");

            const materials = await response.json();
            if (materials.length === 0) {
                document.getElementById("filteredMaterials").innerHTML = "Расходные материалы не найдены за этот период.";
                return;
            }

            // Сортировка заявок по дате использования (по убыванию)
            materials.sort((a, b) => new Date(b.dateOfUse) - new Date(a.dateOfUse));

            // Создаем таблицу с расходными материалами
            const table = document.createElement("table");
            table.className = "table table-bordered table-hover";
            table.style.borderCollapse = "collapse";
            table.style.borderWidth = "1px 0";

            const thead = document.createElement("thead");
            thead.innerHTML = `
        <tr>
            <th>Номенклатурный код</th>
            <th>Количество</th>
            <th>Дата использования</th>
            <th>Где использовалось</th>
            <th>Комментарий</th>
        </tr>
    `;
            table.appendChild(thead);

            const tbody = document.createElement("tbody");
            materials.forEach(material => {
                const row = document.createElement("tr");
                row.innerHTML = `
            <td>${material.nomenclature}</td>
            <td>${material.quantity} шт.</td>
            <td>${new Date(material.dateOfUse).toLocaleString()}</td>
            <td>${material.ticketNumber ? material.inventoryNumber + ' (#' + material.ticketNumber +')' : material.molName || 'Не указано'}</td>
            <td>${material.comments}</td>
        `;
                tbody.appendChild(row);
            });
            table.appendChild(tbody);

            // Добавляем таблицу в контейнер
            const container = document.getElementById("filteredMaterials");
            container.innerHTML = ""; // Очищаем предыдущие данные
            container.appendChild(table);
        } catch (error) {
            document.getElementById("filteredMaterials").innerHTML = "Ошибка при фильтрации материалов.";
            console.error("Ошибка:", error.message);
            showAlert("Ошибка при фильтрации: " + error.message);
        }
    };

    // Загрузка материалов при загрузке страницы
    loadMaterials();

    // Получаем ссылку на кнопку
    const scrollToTopBtn = document.getElementById("scrollToTopBtn");

    // Когда пользователь прокручивает страницу
    window.onscroll = function() {
        // Если страница прокручена более чем на 300px, показываем кнопку
        if (document.body.scrollTop > 300 || document.documentElement.scrollTop > 300) {
            scrollToTopBtn.style.display = "block";
        } else {
            scrollToTopBtn.style.display = "none";
        }
    };

    // Когда пользователь кликает на кнопку, плавно прокручиваем страницу наверх
    scrollToTopBtn.onclick = function() {
        window.scrollTo({
            top: 0,
            left: 0,
            behavior: "smooth"
        });
    };

});
document.addEventListener("DOMContentLoaded", async function () {
    // Загрузка данных при открытии модального окна
    document.getElementById('issueListModal').addEventListener('shown.bs.modal', async function () {
        await loadIssueHistory();
    });

    // Загрузка расходных материалов для выпадающего списка
    await loadSupplies();
});

// Загрузка истории заявок
async function loadIssueHistory() {
    try {
        const response = await fetch(`${window.config.apiUrl}/api/SuppliesIssue/history`, {
            headers: {
                "Authorization": `Bearer ${localStorage.getItem('token')}`
            }
        });
        if (!response.ok) throw new Error("Ошибка загрузки данных");
        const data = await response.json();

        // Очистка таблицы
        const tableBody = document.getElementById('issueTableBody');
        tableBody.innerHTML = '';

        // Заполнение таблицы
        data.forEach(issue => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${issue.molnumber}</td>
                <td>${issue.user.firstName} ${issue.user.lastName}</td>
                <td>${issue.molName}</td>
                <td>${issue.supplies[0].nomenclature}</td>
                <td>${issue.supplies[0].quantity}</td>
                <td>${formatDate(issue.supplies[0].dateOfUse)}</td>
                <td>${issue.comment}</td>
                <td>
                    <button class="btn btn-sm btn-warning" onclick="openEditModalIssue(${issue.molnumber})">Изменить</button>
                    <button class="btn btn-sm btn-danger" onclick="deleteIssue(${issue.molnumber})">Удалить</button>
                </td>
            `;
            tableBody.appendChild(row);
        });
    } catch (error) {
        console.error("Ошибка:", error);
        alert("Не удалось загрузить данные");
    }
}

// Форматирование даты
function formatDate(dateString) {
    const date = new Date(dateString);
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = String(date.getFullYear()).slice(-2);
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${day}.${month}.${year} ${hours}:${minutes}`;
}

// Загрузка расходных материалов
async function loadSupplies() {
    const apiUrl = `${window.config.apiUrl}/api/supplies/mol/Дроздова Татьяна Викторовна`;

    // Загружаем данные из API
    $.ajax({
        url: apiUrl,
        method: 'GET',
        headers: {
            "Authorization": `Bearer ${localStorage.getItem('token')}`,
            'Content-Type': 'application/json'
        },
        success: function (data) {
            const supplyList = $('#supplyList');

            // Очищаем список перед добавлением новых элементов
            supplyList.empty();

            // Добавляем элементы в список
            data.forEach(supply => {
                supplyList.append(`
                            <li>
                                <button class="dropdown-item" type="button" data-value="${supply.НоменклатураКод}">${supply.Номенклатура})</button>
                            </li>
                        `);
            });

            // Обработчик выбора элемента
            supplyList.on('click', '.dropdown-item', function () {
                const selectedText = $(this).text();
                const selectedValue = $(this).data('value');
                $('#supplySearch').val(selectedText);
                supplyList.hide(); // Скрываем выпадающий список

                // Сохраняем выбранное значение
                $('#selectedSupplyCode').val(selectedValue);
            });

            // Показываем список при фокусе на поле
            $('#supplySearch').on('focus', function () {
                supplyList.show();
            });

            // Фильтрация списка при вводе
            $('#supplySearch').on('input', function () {
                const searchText = $(this).val().toLowerCase();
                supplyList.children('li').each(function () {
                    const itemText = $(this).text().toLowerCase();
                    $(this).toggle(itemText.includes(searchText));
                });
            });

            // Скрываем список при клике вне
            $(document).on('click', function (e) {
                if (!$(e.target).closest('.dropdown').length) {
                    supplyList.hide();
                }
            });
        },
        error: function (error) {
            console.error('Ошибка загрузки данных:', error);
            alert("Ошибка при загрузке данных. Проверьте соединение с сервером.");
        }
    });
}

// Открытие модального окна для редактирования
async function openEditModalIssue(molNumber) {
    try {
        // Загрузка данных заявки
        const response = await fetch(`${window.config.apiUrl}/api/SuppliesIssue/history`, {
            headers: {
                "Authorization": `Bearer ${localStorage.getItem('token')}`
            }
        });
        if (!response.ok) throw new Error("Ошибка загрузки данных");
        const data = await response.json();
        const issue = data.find(i => i.molnumber === molNumber);

        // Заполнение формы
        document.getElementById('molName').value = issue.molName;
        document.getElementById('comment').value = issue.comment;
        document.getElementById('quantity').value = issue.supplies[0].quantity;
        document.getElementById('molNumber').value = issue.molnumber;

        // Установка выбранного расходного материала
        $('#supplySearch').val(issue.supplies[0].nomenclature);
        $('#selectedSupplyCode').val(issue.supplies[0].nomenclatureCode);

        // Открытие модального окна
        new bootstrap.Modal(document.getElementById('editIssueModal')).show();
    } catch (error) {
        console.error("Ошибка:", error);
        alert("Не удалось загрузить данные для редактирования");
    }
}

// Сохранение изменений
async function saveChanges() {
    const formData = {
        molName: document.getElementById('molName').value,
        comment: document.getElementById('comment').value,
        nomenclatureCode: $('#selectedSupplyCode').val(),
        quantity: parseInt(document.getElementById('quantity').value),
        molnumber: parseInt(document.getElementById('molNumber').value)
    };

    try {
        const response = await fetch(`${window.config.apiUrl}/api/SuppliesIssue/update`, {
            method: 'PUT',
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify(formData)
        });
        if (!response.ok) throw new Error("Ошибка обновления данных");
        alert("Заявка успешно обновлена");
        await loadIssueHistory(); // Перезагрузка данных
        new bootstrap.Modal(document.getElementById('editIssueModal')).hide();
    } catch (error) {
        console.error("Ошибка:", error);
        alert("Не удалось обновить заявку");
    }
}

// Удаление заявки
async function deleteIssue(molNumber) {
    if (!confirm("Вы уверены, что хотите удалить заявку?")) return;

    try {
        const response = await fetch(`${window.config.apiUrl}/api/SuppliesIssue/delete/${molNumber}`, {
            method: 'DELETE',
            headers: {
                "Authorization": `Bearer ${localStorage.getItem('token')}`
            }
        });
        if (!response.ok) throw new Error("Ошибка удаления данных");
        alert("Заявка успешно удалена");
        await loadIssueHistory(); // Перезагрузка данных
    } catch (error) {
        console.error("Ошибка:", error);
        alert("Не удалось удалить заявку");
    }
}

//загрузка списка в модельном окне создания расходных материалов
$(document).ready(function () {
    const apiUrl1C = `${window.config.apiUrl}/api/supplies/mol/Дроздова Татьяна Викторовна`;
    const apiUrlStock = `${window.config.apiUrl}/api/stock-supplies/all`;

    // Загружаем данные из обоих источников
    Promise.all([
        fetch(apiUrl1C, {
            headers: {
                "Authorization": `Bearer ${localStorage.getItem('token')}`
            }
        }).then(response => response.json()),
        fetch(apiUrlStock, {
            headers: {
                "Authorization": `Bearer ${localStorage.getItem('token')}`
            }
        }).then(response => response.json())
    ]).then(([data1C, dataStock]) => {
        const nomenclatureList = $('#nomenclatureList');
        const uniqueNomenclatures = new Map(); // Используем Map для избежания дубликатов

        // Добавляем данные из 1С
        data1C.forEach(item => {
            const normalizedItem = normalizeNomenclature(item, '1C');
            if (normalizedItem && !uniqueNomenclatures.has(normalizedItem.nomenclatureCode)) {
                uniqueNomenclatures.set(normalizedItem.nomenclatureCode, normalizedItem);
            }
        });

        // Добавляем данные со склада
        dataStock.forEach(item => {
            const normalizedItem = normalizeNomenclature(item, 'Склад');
            if (normalizedItem && !uniqueNomenclatures.has(normalizedItem.nomenclatureCode)) {
                uniqueNomenclatures.set(normalizedItem.nomenclatureCode, normalizedItem);
            }
        });

        // Отображаем данные в списке
        uniqueNomenclatures.forEach((item, key) => {
            const label = item.source === '1C' ? '(1C)' : '(Склад)';
            nomenclatureList.append(`
        <li>
          <button class="dropdown-item" type="button" data-value="${key}" data-name="${item.nomenclature}">${label} ${item.nomenclature}</button>
        </li>
      `);
        });

        // Обработчик выбора элемента
        nomenclatureList.on('click', '.dropdown-item', function () {
            const selectedText = $(this).data('name'); // Наименование
            const selectedValue = $(this).data('value'); // Код номенклатуры
            $('#nomenclatureSearch').val(selectedText); // Вставляем наименование в поле поиска
            selectedNomenclatureCode = selectedValue; // Сохраняем выбранный код
            nomenclatureList.hide(); // Скрываем выпадающий список
        });

        // Показываем список при фокусе на поле
        $('#nomenclatureSearch').on('focus', function () {
            nomenclatureList.show();
        });

        // Фильтрация списка при вводе
        $('#nomenclatureSearch').on('input', function () {
            const searchText = $(this).val().toLowerCase();
            nomenclatureList.children('li').each(function () {
                const itemText = $(this).text().toLowerCase();
                $(this).toggle(itemText.includes(searchText)); // Показываем/скрываем элементы
            });
        });

        // Скрываем список при клике вне
        $(document).on('click', function (e) {
            if (!$(e.target).closest('.dropdown').length) {
                nomenclatureList.hide();
            }
        });
    }).catch(error => {
        console.error('Ошибка загрузки данных:', error);
    });
});

function normalizeNomenclature(item, source) {
    if (source === '1C') {
        return {
            id: null, // У 1С нет поля id
            nomenclature: item.Номенклатура,
            nomenclatureCode: item.НоменклатураКод,
            quantity: item.Количество,
            mol: item.МОЛ,
            source: '1C'
        };
    } else if (source === 'Склад') {
        return {
            id: item.id,
            nomenclature: item.nomenclature,
            nomenclatureCode: item.nomenclatureCode,
            quantity: item.quantity,
            mol: item.mol,
            source: 'Склад'
        };
    }
    return null;
}
let selectedNomenclatureCode = null; // Для хранения выбранного кода номенклатуры

//отправка данных на сервре
async function submitSupplyForm() {
    const nomenclatureName = $('#nomenclatureSearch').val(); // Наименование из input
    const quantity = $('#quantityInput').val(); // Количество

    if (!nomenclatureName || !quantity) {
        alert('Заполните все поля!');
        return;
    }

    // Формируем payload в зависимости от выбора пользователя
    const payload = selectedNomenclatureCode
        ? { nomenclature: nomenclatureName,
            nomenclatureCode: selectedNomenclatureCode,
            quantity: parseInt(quantity, 10) } // Если выбран код
        : { nomenclature: nomenclatureName,
            nomenclatureCode: "",
            quantity: parseInt(quantity, 10)}; // Если введено вручную

    // Отправляем данные на сервер

    console.log (payload);
    try {
        const response = await fetch(`${window.config.apiUrl}/api/stock-supplies/create`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            const data = await response.json(); // Предполагаем, что сервер возвращает созданный объект
            alert('Расходный материал успешно добавлен!');

            // Если это была новая номенклатура, добавляем её в список
            if (!selectedNomenclatureCode) {
                const nomenclatureList = $('#nomenclatureList');
                nomenclatureList.append(`
          <li>
            <button class="dropdown-item" type="button" data-value="${data.nomenclatureCode}" data-name="${nomenclatureName}">(Склад) ${nomenclatureName}</button>
          </li>
        `);
            }

            // Закрываем модальное окно
            bootstrap.Modal.getInstance(document.getElementById('addSupplyModal')).hide();
        } else {
            alert('Ошибка при добавлении расходного материала');
        }
    } catch (error) {
        console.error('Ошибка:', error);
        alert('Произошла ошибка при отправке данных');
    }
}
let sortDirection = {}; // Хранит текущее состояние сортировки

function sortTable(columnIndex, thElement) {
    let table = document.getElementById("materialsTableBody");
    let rows = Array.from(table.getElementsByTagName("tr"));

    // Определяем текущий порядок сортировки
    let ascending = sortDirection[columnIndex] !== true;
    sortDirection[columnIndex] = ascending; // Переключаем порядок

    rows.sort((rowA, rowB) => {
        let cellA = rowA.getElementsByTagName("td")[columnIndex]?.innerText.trim();
        let cellB = rowB.getElementsByTagName("td")[columnIndex]?.innerText.trim();

        // Определяем тип данных (число или текст)
        let isNumber = !isNaN(parseFloat(cellA)) && !isNaN(parseFloat(cellB));

        if (isNumber) {
            return ascending ? cellA - cellB : cellB - cellA;
        } else {
            return ascending ? cellA.localeCompare(cellB) : cellB.localeCompare(cellA);
        }
    });

    // Перерисовываем строки в нужном порядке
    table.innerHTML = "";
    rows.forEach(row => table.appendChild(row));

    // Обновляем стрелки в заголовке
    document.querySelectorAll(".sortable span").forEach(span => span.innerHTML = ""); // Сброс всех стрелок
    thElement.querySelector("span").innerHTML = ascending ? " ▲" : " ▼";
}
