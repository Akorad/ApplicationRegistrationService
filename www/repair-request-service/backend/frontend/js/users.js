$(document).ready(function () {
// Загружаем список пользователей
    const token = localStorage.getItem('token');

    function loadUsers() {
    $.ajax({
        url: `${window.config.apiUrl}/api/users/getAll`,
        method: 'GET',
        headers: {
            "Authorization": `Bearer ${token}`, // Добавляем заголовок с токеном
        },
        dataType: 'json',
        success: function (users) {
            const tableBody = $("#userTableBody");
            tableBody.empty(); // Очищаем таблицу перед загрузкой новых данных

            users.forEach(user => {
                const row = `
                        <tr>
                            <td>${user.username}</td>
                            <td>${user.email}</td>
                            <td>${user.firstName}</td>
                            <td>${user.lastName}</td>
                            <td>${user.department}</td>
                            <td>${user.phoneNumber}</td>
                            <td>
                                <select class="form-select user-role" data-username="${user.username}">
                                    <option value="USER" ${user.role === "USER" ? "selected" : ""}>Пользователь</option>
                                    <option value="ADMIN" ${user.role === "ADMIN" ? "selected" : ""}>Администратор</option>
                                    <option value="GUEST" ${user.role === "GUEST" ? "selected" : ""}>Гость</option>
                                </select>
                            </td>
                            <td>
                                <button class="btn btn-primary btn-save" data-username="${user.username}">Сохранить</button>
                            </td>
                        </tr>
                    `;
                tableBody.append(row);
            });
        },
        error: function (error) {
            console.error('Ошибка загрузки пользователей:', error);
            alert("Не удалось загрузить пользователей.");
        }
    });
}

// Сохраняем изменения роли пользователя
$(document).on('click', '.btn-save', function () {
    const username = $(this).data('username');
    const role = $(`select[data-username="${username}"]`).val();

    $.ajax({
        url: `${window.config.apiUrl}/api/users/update/${username}`,
        method: 'PUT',
        contentType: 'application/json',
        headers: {
            "Authorization": `Bearer ${token}`, // Добавляем заголовок с токеном
        },
        data: JSON.stringify({ role }),
        success: function () {
            alert(`Роль пользователя ${username} успешно обновлена на ${role}.`);
        },
        error: function (error) {
            console.error('Ошибка обновления роли:', error);
            alert("Не удалось обновить роль пользователя.");
        }
    });
});

// Загружаем пользователей при загрузке страницы
$(document).ready(function () {
    loadUsers();
});
});