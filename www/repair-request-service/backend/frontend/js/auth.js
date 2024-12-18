
document.addEventListener("DOMContentLoaded", async function () {
    const authContainer = document.getElementById("modalContainerAuth");

    if (!authContainer) {
        console.error("Элемент #modalContainerAuth не найден.");
        return;
    }

    // Загружаем модальное окно авторизации
    await loadAuthModal();
    setTimeout(checkAuthStatus , 200);
    //checkAuthStatus(); // Проверка статуса авторизации при загрузке страницы
});

// Загрузка модального окна авторизации
async function loadAuthModal() {
    try {
        const response = await fetch("/templates/auth.html");
        if (!response.ok) throw new Error(`Ошибка загрузки: ${response.status}`);
        const authHTML = await response.text();
        document.getElementById("modalContainerAuth").innerHTML = authHTML;

        // Инициализация событий для авторизации
        initAuthEvents();
    } catch (error) {
        console.error("Ошибка загрузки auth.html:", error);
    }
}

// Инициализация событий для авторизации
function initAuthEvents() {
    const loginForm = document.getElementById('loginForm');
    const loginButton = document.getElementById("loginButton");
    const logoutButton = document.getElementById("logoutButton");

    if (!loginForm || !loginButton || !logoutButton) {
        console.error("Не все элементы авторизации загружены.");
        return;
    }

    // Обработчик отправки формы авторизации
    loginForm.addEventListener('submit', handleLogin);

    // Обработчик кнопки "Войти"
    loginButton.addEventListener("click", () => $('#loginModal').modal('show'));

    // Обработчик кнопки "Выйти"
    logoutButton.addEventListener("click", handleLogout);
}

// Обработчик отправки формы авторизации
async function handleLogin(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const requestData = {
        username: formData.get('username'),
        password: formData.get('password')
    };

    try {
        const response = await fetch(`${window.config.apiUrl}/auth/sign-in`,  {
            method: 'POST',
            body: JSON.stringify(requestData),
            headers: { 'Content-Type': 'application/json' }
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('token', data.token);

            // Декодирование роли пользователя
            const parsedToken = parseJwt(data.token);
            if (parsedToken?.role) {
                localStorage.setItem('userRole', parsedToken.role);
                showAlert('Вход выполнен успешно!');
                $('#loginModal').modal('hide');
                setTimeout(checkAuthStatus , 200); // Обновление состояния кнопок
                location.reload();
            } else {
                console.error("Роль не найдена");
            }
        } else {
            showAlert('Ошибка при входе. Проверьте имя пользователя и пароль.');
        }
    } catch (error) {
        showAlert('Ошибка при авторизации.', 'danger'); // Используем Bootstrap alert danger
        console.error("Ошибка при авторизации:", error);
    }
}

// Обработчик кнопки "Выйти"
function handleLogout() {
    localStorage.removeItem("token");
    localStorage.removeItem("userRole");
    showAlert("Выход выполнен");
    setTimeout(checkAuthStatus , 200);// Обновление состояния кнопок
    location.reload();
}

// Проверка статуса авторизации
// 📌 Удаляем использование localStorage и JWT на клиенте для OpenID
async function checkAuthStatus() {
    const loginButton = document.getElementById("loginButton");
    const logoutButton = document.getElementById("logoutButton");

    if (!loginButton || !logoutButton) {
        setTimeout(checkAuthStatus, 50);
        return;
    }

    try {
        const response = await fetch('/auth/status'); // 📌 Делаем запрос на сервер
        if (response.ok) {
            const data = await response.json();
            if (data.isAuthenticated) {
                loginButton.style.display = "none";
                logoutButton.style.display = "inline-block";
            } else {
                loginButton.style.display = "inline-block";
                logoutButton.style.display = "none";
            }
        }
    } catch (error) {
        console.error("Ошибка проверки статуса авторизации", error);
    }
}


// Функция декодирования токена JWT
function parseJwt(token) {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(c =>
        '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
    ).join(''));

    return JSON.parse(jsonPayload);
}
