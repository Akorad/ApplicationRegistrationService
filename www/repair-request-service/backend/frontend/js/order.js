document.addEventListener('DOMContentLoaded', function() {
    fetchPurchaseItems();
});

// Функция для загрузки и отображения всех элементов заказа
function fetchPurchaseItems() {
    fetch(`${window.config.apiUrl}/api/purchases/getAll`, {
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
    })
        .then(response => response.json())
        .then(data => {
            const purchaseItemsContainer = document.getElementById('purchaseItems');
            purchaseItemsContainer.innerHTML = ''; // Очищаем контейнер перед добавлением новых элементов

            data.forEach(item => {
                const row = document.createElement('tr');
                row.innerHTML = `
                <td>${item.name}</td>
                <td>${item.quantity}</td>
                <td>${item.notes}</td>
                <td>
                    <button onclick="openEditModal(${JSON.stringify(item).replace(/"/g, '&quot;')})" class="btn btn-primary btn-sm">Редактировать</button>
                    <button onclick="confirmDelete(${item.id})" class="btn btn-danger btn-sm">Удалить</button>
                    <button onclick="toggleProductInfo(${item.id})" class="btn btn-info btn-sm">Подробнее</button>
                </td>
            `;

                // Добавляем строку для отображения информации о товарах
                const infoRow = document.createElement('tr');
                infoRow.id = `productInfo-${item.id}`;
                infoRow.style.display = 'none';
                infoRow.innerHTML = `
                <td colspan="4">
                    <div class="collapse-container">
                        <div class="search-info-button">
                            <button onclick="searchProductInfo(this, ${item.id})" class="btn btn-info btn-sm">
                                Найти информацию
                            </button>
                        </div>
                        <div class="collapse-blocks">
                            ${renderProductInfos(item.productInfos)}
                        </div>
                    </div>
                </td>
            `;

                purchaseItemsContainer.appendChild(row);
                purchaseItemsContainer.appendChild(infoRow);
            });
        })
        .catch(error => console.error('Ошибка при загрузке данных:', error));
}

// Функция для отображения информации о товарах
function renderProductInfos(productInfos) {
    if (!productInfos || productInfos.length === 0) {
        return '<p>Нет информации о товарах</p>';
    }

    // Разделяем товары на две части
    const half = Math.ceil(productInfos.length / 2);
    const firstHalf = productInfos.slice(0, half);
    const secondHalf = productInfos.slice(half);

    return `
        <div class="collapse-block">
            ${firstHalf.map(info => renderProductInfo(info)).join('')}
        </div>
        <div class="collapse-block">
            ${secondHalf.map(info => renderProductInfo(info)).join('')}
        </div>
    `;
}

// Функция для отображения информации об одном товаре
function renderProductInfo(info) {
    return `
        <div class="product-info">
            <a href="${info.productUrl}" target="_blank">
                <img src="${info.imageUrl}" alt="${info.title}">
            </a>
            <p><strong>Название:</strong> ${info.title}</p>
            <p><strong>Цена:</strong> ${info.price}</p>
            <p><strong>Источник:</strong> ${info.sourceType}</p>
            <p><strong>Обновлено:</strong> ${new Date(info.updatedAt).toLocaleString()}</p>
            <a href="${info.productUrl}" target="_blank" class="btn btn-link">Перейти к товару</a>
        </div>
    `;
}

// Функция для раскрытия/скрытия информации о товарах
function toggleProductInfo(id) {
    const infoRow = document.getElementById(`productInfo-${id}`);
    if (infoRow.style.display === 'none') {
        infoRow.style.display = 'table-row';
    } else {
        infoRow.style.display = 'none';
    }
}

// Функция для отправки данных из модального окна
function submitOrder() {
    // Показываем спиннер
    const spinner = document.getElementById('spinner');
    spinner.style.display = 'block';

    // Собираем данные из формы
    const newItem = {
        name: document.getElementById('partName').value,
        quantity: parseInt(document.getElementById('quantity').value),
        notes: document.getElementById('notes').value
    };

    // Отправляем данные на сервер
    fetch(`${window.config.apiUrl}/api/purchases/with-product-info`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(newItem)
    })
        .then(response => response.json())
        .then(data => {
            // Обновляем список заказов
            fetchPurchaseItems();

            // Закрываем модальное окно
            const orderModal = bootstrap.Modal.getInstance(document.getElementById('orderModal'));
            orderModal.hide();

            // Очищаем форму
            document.getElementById('addItemForm').reset();
        })
        .catch(error => {
            showAlert(`Ошибка при добавлении элемента: ${error.message}`);
            spinner.style.display = 'none';
        })
        .finally(() => {
            // Скрываем спиннер после завершения запроса
            spinner.style.display = 'none';
        });
}

// Функция для открытия модального окна редактирования
function openEditModal(item) {
    document.getElementById('editId').value = item.id;
    document.getElementById('editName').value = item.name;
    document.getElementById('editQuantity').value = item.quantity;
    document.getElementById('editNotes').value = item.notes;
    $('#editModal').modal('show'); // Показываем модальное окно
}

// Функция для сохранения изменений
function saveChanges() {
    const updatedItem = {
        id: document.getElementById('editId').value,
        name: document.getElementById('editName').value,
        quantity: parseInt(document.getElementById('editQuantity').value),
        notes: document.getElementById('editNotes').value
    };

    fetch(`${window.config.apiUrl}/api/purchases/${updatedItem.id}`, {
        method: 'PUT',
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(updatedItem)
    })
        .then(response => response.json())
        .then(data => {
            fetchPurchaseItems(); // Обновляем список после редактирования
            $('#editModal').modal('hide'); // Закрываем модальное окно
        })
        .catch(error => console.error('Ошибка при обновлении элемента:', error));
}

// Глобальная переменная для хранения ID элемента, который нужно удалить
let itemIdToDelete = null;

// Функция для открытия модального окна подтверждения удаления
function confirmDelete(id) {
    itemIdToDelete = id; // Сохраняем ID элемента
    const confirmDeleteModal = new bootstrap.Modal(document.getElementById('confirmDeleteModal'));
    confirmDeleteModal.show(); // Открываем модальное окно
}

// Функция для удаления элемента после подтверждения
function deleteItem() {
    if (itemIdToDelete === null) return; // Если ID не установлен, выходим

    fetch(`${window.config.apiUrl}/api/purchases/${itemIdToDelete}`, {
        method: 'DELETE',
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
    })
        .then(response => {
            if (response.ok) {
                fetchPurchaseItems(); // Обновляем список после удаления
            }
        })
        .catch(error => console.error('Ошибка при удалении элемента:', error))
        .finally(() => {
            itemIdToDelete = null; // Сбрасываем ID после удаления
            const confirmDeleteModal = bootstrap.Modal.getInstance(document.getElementById('confirmDeleteModal'));
            confirmDeleteModal.hide(); // Закрываем модальное окно
        });
}

// Назначаем обработчик для кнопки "Удалить" в модальном окне
document.getElementById('confirmDeleteButton').addEventListener('click', deleteItem);

// Функция для обновления всех товаров
function updateAllProducts() {
    fetch(`${window.config.apiUrl}/api/purchases/update-all-products`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
    })
        .then(response => response.json())
        .then(data => {
            alert(data); // Показываем сообщение об успешном обновлении
            fetchPurchaseItems(); // Обновляем список
        })
        .catch(error => console.error('Ошибка при обновлении товаров:', error));
}

// Функция для обновления конкретного товара
function searchProductInfo(button, id) {
    // Сохраняем оригинальный текст кнопки
    const originalText = button.innerHTML;

    // Делаем кнопку неактивной и заменяем текст на спиннер
    button.disabled = true;
    button.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Поиск...`;

    fetch(`${window.config.apiUrl}/api/purchases/search-product-info/${id}`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
    })
        .then(response => response.json())
        .then(data => {
            // Обновляем список заказов
            fetchPurchaseItems();
        })
        .catch(error => console.error('Ошибка при поиске информации:', error))
        .finally(() => {
            // Возвращаем кнопку в исходное состояние
            button.innerHTML = originalText;
            button.disabled = false;
        });
}


//функция скачивания ПДФ списка закупок
document.getElementById('previewPdfButton').addEventListener('click', function () {
    // URL вашего эндпоинта
    const url = `${window.config.apiUrl}/api/purchases/preview-pdf`;

    // Отправляем GET-запрос
    fetch(url, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
            'Accept': 'application/pdf' // Указываем, что ожидаем PDF
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка при загрузке PDF');
            }
            return response.blob(); // Преобразуем ответ в Blob
        })
        .then(blob => {
            // Создаем URL для Blob
            const pdfUrl = URL.createObjectURL(blob);

            // Открываем PDF в новой вкладке
            window.open(pdfUrl, '_blank');

            // Освобождаем ресурсы (опционально)
            URL.revokeObjectURL(pdfUrl);
        })
        .catch(error => {
            console.error('Ошибка:', error);
            alert('Не удалось загрузить PDF. Пожалуйста, попробуйте снова.');
        });
});