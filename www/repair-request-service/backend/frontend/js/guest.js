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
});
