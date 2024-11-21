// Авторизация пользователя
document.getElementById('loginForm').addEventListener('submit', async function (event) {
    event.preventDefault();
    const formData = new FormData(document.getElementById('loginForm'));
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
            let userRole = parsedToken.role;
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

// Проверка авторизации пользователя
function checkAuthStatus() {
    const token = localStorage.getItem("token");

    const loginButton = document.getElementById("loginButton");
    const logoutButton = document.getElementById("logoutButton");

    if (!loginButton || !logoutButton) {
        // Если кнопки ещё не загрузились, повторяем попытку позже
        setTimeout(checkAuthStatus, 50);
        return;
    }


    if (token) {
        const parsedToken = parseJwt(token);

        if (parsedToken && parsedToken.role) {
            // Пользователь авторизован
            loginButton.style.display = "none";
            logoutButton.style.display = "inline-block";
        } else {
            // Некорректный токен, удаляем его
            localStorage.removeItem("token");
            loginButton.style.display = "inline-block";
            logoutButton.style.display = "none";
        }
    } else {
        // Пользователь не авторизован
        loginButton.style.display = "inline-block";
        logoutButton.style.display = "none";
    }
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

// Обработчик для кнопки "Войти"
document.addEventListener("click", function (event) {
    if (event.target.id === "loginButton") {
        $('#loginModal').modal('show'); // Открытие модального окна авторизации
    }
});

// Обработчик для кнопки "Выйти"
document.addEventListener("click", function (event) {
    if (event.target.id === "logoutButton") {
        localStorage.removeItem("token");
        localStorage.removeItem("userRole");
        alert("Выход выполнен");
        checkAuthStatus(); // Обновление состояния кнопок
    }
});

// Проверка авторизации после загрузки страницы и хедера
document.addEventListener("DOMContentLoaded", checkAuthStatus);