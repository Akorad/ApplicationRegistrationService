document.addEventListener("DOMContentLoaded", async function () {
    const materialsTableBody = document.getElementById("materialsTableBody");
    const searchInput = document.getElementById("materialSearch");
    let materialsData = [];
    let currentMaterialId = null; // Для хранения текущего ID материала

    // Загрузка списка материалов
    async function loadMaterials() {
        try {
            const response = await fetch(`${window.config.apiUrl}/api/supplies/mol/Дроздова Татьяна Викторовна`, {
                headers: {
                    "Authorization": `Bearer ${localStorage.getItem('token')}`
                }
            });
            if (!response.ok) throw new Error("Ошибка загрузки материалов");

            const data = await response.json();
            materialsData = data.map(item => ({
                id: item.НоменклатураКод,
                name: item.Номенклатура,
                quantity: item.Количество || 0,
                imageUrl: `/images/${item.НоменклатураКод}.jpg` // Генерация пути к изображению
            }));

            renderMaterials(materialsData); // Рендерим материалы после загрузки
        } catch (error) {
            console.error("Ошибка загрузки данных:", error);
            showAlert("Ошибка при загрузке данных. Проверьте соединение с сервером.");
        }
    }

    // Рендеринг таблицы материалов
    function renderMaterials(data) {
        materialsTableBody.innerHTML = ""; // Очищаем таблицу перед рендерингом
        data.forEach(material => {
            const row = document.createElement("tr");

            row.innerHTML = `
        <td class="image-cell" style="position: relative;">
            <img src="${window.config.apiUrl}/images/${material.id}.jpg" 
                 alt="${material.name}" 
                 class="material-image"
                 onclick="openImageModal('${material.id}')"
                 onerror="this.onerror=null;this.src='${window.config.apiUrl}/images/default-material.jpg';">
            <button class="edit-image-btn" onclick="openEditModal('${material.id}')">
                🖊️
            </button>
        </td>
        <td>${material.name}</td>
        <td>${material.quantity}</td>
        <td>
            <button class="btn btn-info btn-sm me-2" onclick="openRequestsModal('${material.id}')">Заявки</button>
            <button class="btn btn-success btn-sm" onclick="openIssueModal('${material.id}')">Выдача</button>
        </td>
        `;
            materialsTableBody.appendChild(row);
        });
    }

    // Фильтрация материалов
    searchInput.addEventListener("input", () => {
        const query = searchInput.value.toLowerCase();
        const filteredMaterials = materialsData.filter(material =>
            material.name.toLowerCase().includes(query)
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
                requestsList.innerHTML = "Заявки с этим расходным материалом не найдены.";
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
        document.getElementById("nomenclatureCodeInventory").value = nomenclatureCode;
        document.getElementById("nomenclatureCodeMol").value = nomenclatureCode;

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
            inventoryNumber: document.getElementById("inventoryNumber").value,
            nomenclatureCode: document.getElementById("nomenclatureCodeInventory").value,
            comment: document.getElementById("commentInventory").value,
            quantity: parseInt(document.getElementById("quantityInventory").value, 10),
        };

        fetch(`${window.config.apiUrl}/api/SuppliesIssue/byInventory`, {
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
        const request = {
            molName: document.getElementById("molName").value,
            comment: document.getElementById("commentMol").value,
            nomenclatureCode: document.getElementById("nomenclatureCodeMol").value,
            quantity: parseInt(document.getElementById("quantityMol").value, 10),
        };

        fetch(`${window.config.apiUrl}/api/SuppliesIssue/byMol`, {
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
});
