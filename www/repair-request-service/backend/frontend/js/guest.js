document.getElementById('guestTicketForm').addEventListener('submit', async function(event) {
    event.preventDefault();

    const inventoryNumber = document.getElementById('inventoryNumber').value;
    const guestDepartment = document.getElementById('guestDepartmentSearch').value;
    const guestPhoneNumber = document.getElementById('guestPhoneNumber').value;
    const descriptionOfTheProblem = document.getElementById('descriptionOfTheProblem').value;
    const refilling = document.getElementById('refilling').checked;


    const requestData = {
        descriptionOfTheProblem: descriptionOfTheProblem,
        guestPhoneNumber: guestPhoneNumber,
        guestDepartment: guestDepartment,
        refilling:refilling,
        equipment: {
            inventoryNumber: inventoryNumber
        }
    };

    try {
        const response = await fetch(`${window.config.apiUrl}/api/guest/create`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify(requestData)
        });

        if (response.status === 403) {
            showAlert('Ошибка 403: Доступ запрещен. Возможно, у вас недостаточно прав для выполнения этого действия.');
        } else if (!response.ok) {
            const errorData = await response.json();
            showAlert(`Ошибка при создании заявки: ${errorData.error || 'Неизвестная ошибка'}`);
        } else {
            alert('Заявка успешно создана');
            document.getElementById('guestTicketForm').reset();
        }
    } catch (error) {
        showAlert(`Произошла ошибка при отправке: ${error.message}`);
    }

});

$(document).ready(function () {
    const observer = new MutationObserver(function (mutations) {
        mutations.forEach(function (mutation) {
            if ($('#loginModal').length) {
                if (localStorage.getItem('token') == null) {
                    $('#loginModal').modal('show');
                }
                observer.disconnect(); // Останавливаем наблюдение после открытия модального окна
            }
        });
    });

    // Начинаем наблюдение за изменениями в DOM
    observer.observe(document.body, { childList: true, subtree: true });
});

$(document).ready(function () {

    const apiUrl = `${window.config.apiUrl}/api/departments/names`;
    // Загружаем данные из API
    $.ajax({
        url: apiUrl,
        method: 'GET',
        dataType: 'json',
        success: function (data) {
            const departmentList = $('#guestDepartmentList');
            data.forEach(department => {
                departmentList.append(`
                    <li>
                        <button class="dropdown-item" type="button" data-value="${department}">${department}</button>
                    </li>
                `);
            });

            // Обработчик выбора элемента
            departmentList.on('click', '.dropdown-item', function () {
                const selectedText = $(this).text();
                $('#guestDepartmentSearch').val(selectedText);
                departmentList.hide(); // Скрываем выпадающий список
            });

            // Показываем список при фокусе на поле
            $('#guestDepartmentSearch').on('focus', function () {
                departmentList.show();
            });

            // Фильтрация списка при вводе
            $('#guestDepartmentSearch').on('input', function () {
                const searchText = $(this).val().toLowerCase();
                departmentList.children('li').each(function () {
                    const itemText = $(this).text().toLowerCase();
                    $(this).toggle(itemText.includes(searchText));
                });
            });

            // Скрываем список при клике вне
            $(document).on('click', function (e) {
                if (!$(e.target).closest('.dropdown').length) {
                    departmentList.hide();
                }
            });
        },
        error: function (error) {
            console.error('Ошибка загрузки данных:', error);
        }
    });
    let currentIndex = -1;

    $('#guestDepartmentSearch').on('keydown', function (e) {
        const items = $('#guestDepartmentList .dropdown-item:visible');

        if (!items.length) return;

        if (e.key === 'ArrowDown') {
            e.preventDefault();
            currentIndex = (currentIndex + 1) % items.length;
            highlightItem(items, currentIndex);
        } else if (e.key === 'ArrowUp') {
            if (currentIndex <= 0) return; // ⛔ блокируем вверх, если ничего не выбрано
            e.preventDefault();
            currentIndex = (currentIndex - 1 + items.length) % items.length;
            highlightItem(items, currentIndex);
        } else if (e.key === 'Enter') {
            e.preventDefault();
            if (currentIndex >= 0 && currentIndex < items.length) {
                const selectedText = $(items[currentIndex]).text();
                $('#guestDepartmentSearch').val(selectedText);
                $('#guestDepartmentList').hide();
            }
        }
    });

    function highlightItem(items, index) {
        items.removeClass('active');
        const activeItem = $(items[index]);
        activeItem.addClass('active');
        activeItem[0].scrollIntoView({
            block: 'nearest', // прокручивает так, чтобы элемент оказался видим
            behavior: 'smooth' // можно убрать, если мешает
        });
    }


// При новом вводе — сбрасываем текущий индекс
    $('#guestDepartmentSearch').on('input', function () {
        currentIndex = -1;
        $('#guestDepartmentList .dropdown-item').removeClass('active');
    });

});

document.getElementById('refilling').addEventListener('change', function() {
    const textarea = document.getElementById('descriptionOfTheProblem');
    if (this.checked && !textarea.value.includes('Заправка')) {
        textarea.value += (textarea.value ? ' ' : '') + 'Заправка';
    }
});
// Инициализация tooltips Bootstrap
document.addEventListener("DOMContentLoaded", function () {
    let tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
});


