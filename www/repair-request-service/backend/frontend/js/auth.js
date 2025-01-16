document.addEventListener("DOMContentLoaded", async function () {
    const authContainer = document.getElementById("modalContainerAuth");
    if (!authContainer) {
        console.error("Элемент #modalContainerAuth не найден.");
        return;
    }

    await loadAuthModal();

    initializeAuth(); // Инициализация авторизации

    processURLToken(); // Проверка и сохранение токена из URL

    if (window.location.pathname === '/guest') {
        handleLocalAuth();
    } else {
        checkOpenIDAuth();
    }
});

// Обработка токена из URL
function processURLToken() {
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get('token');
    if (token) {
        saveToken(token);
        window.history.replaceState({}, document.title, window.location.pathname);
    }
}

// Локальная авторизация
function handleLocalAuth() {
    const loginForm = document.getElementById('loginForm');
    loginForm?.addEventListener('submit', async (event) => {
        event.preventDefault();
        const requestData = getFormData(event.target);
        try {
            const response = await sendAuthRequest('/auth/sign-in', requestData);
            handleAuthResponse(response);
        } catch (error) {
            handleAuthError(error);
        }
    });
}

// Загрузка модального окна
async function loadAuthModal() {
    try {
        const response = await fetch("/templates/auth.html");
        if (!response.ok) throw new Error(`Ошибка загрузки: ${response.status}`);
        const authHTML = await response.text();
        document.getElementById("modalContainerAuth").innerHTML = authHTML;
        initAuthEvents();
    } catch (error) {
        console.error("Ошибка загрузки auth.html:", error);
    }
}

// Инициализация событий авторизации
function initAuthEvents() {
    const loginForm = document.getElementById('loginForm');
    const loginButton = document.getElementById("loginButton");
    const logoutButton = document.getElementById("logoutButton");

    if (!loginForm || !loginButton || !logoutButton) {
        console.error("Не все элементы авторизации загружены. Повторная попытка через 100 мс...");
        setTimeout(initAuthEvents, 100); // Повторная попытка через 100 мс
        return;
    }

    loginForm.addEventListener('submit', handleLogin);
    loginButton.addEventListener("click", () => $('#loginModal').modal('show'));
    logoutButton.addEventListener("click", handleLogout);
}

// Обработчик входа
async function handleLogin(event) {
    event.preventDefault();
    const requestData = getFormData(event.target);
    try {
        const response = await sendAuthRequest(`${window.config.apiUrl}/auth/sign-in`, requestData);
        handleAuthResponse(response);
    } catch (error) {
        handleAuthError(error);
    }
}

// Получение данных из формы
function getFormData(form) {
    const formData = new FormData(form);
    return {
        username: formData.get('username'),
        password: formData.get('password')
    };
}

// Отправка запроса авторизации
async function sendAuthRequest(url, data) {
    return await fetch(url, {
        method: 'POST',
        body: JSON.stringify(data),
        headers: { 'Content-Type': 'application/json' }
    });
}

// Обработка успешного ответа авторизации
function handleAuthResponse(response) {
    if (response.ok) {
        response.json().then(data => {
            saveToken(data.token);
            const userRole = parseJwt(data.token)?.role;
            if (userRole) {
                localStorage.setItem('userRole', userRole);
                showAlert('Вход выполнен успешно!');
                $('#loginModal').modal('hide');
                location.reload();
            }
        });
    } else {
        showAlert('Ошибка при входе. Проверьте имя пользователя и пароль.');
    }
}

// Обработка ошибок авторизации
function handleAuthError(error) {
    showAlert('Ошибка при авторизации.', 'danger');
    console.error("Ошибка при авторизации:", error);
}

// Проверка статуса авторизации
function checkAuthStatus() {
    const token = localStorage.getItem("token");
    const loginButton = document.getElementById("loginButton");
    const logoutButton = document.getElementById("logoutButton");

    if (!loginButton || !logoutButton) {
        setTimeout(checkAuthStatus, 50); // Повторная попытка, если элементы еще не загружены
        return;
    }

    if (token) {
        const parsedToken = parseJwt(token);

        // Проверяем срок действия токена
        const currentTime = Math.floor(Date.now() / 1000); // Текущее время в секундах
        if (parsedToken?.exp && parsedToken.exp < currentTime) {
            console.warn("Токен истек.");
            handleLogout(); // Удаляем токен и обновляем интерфейс
            return;
        }

        // Если токен валиден
        if (parsedToken?.role) {
            loginButton.style.display = "none";
            logoutButton.style.display = "inline-block";
        } else {
            handleLogout(); // Удаляем токен и обновляем интерфейс
        }
    } else {
        loginButton.style.display = "inline-block";
        logoutButton.style.display = "none";
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

// Инициализация авторизации
function initializeAuth() {
    const token = getToken();
    if (token && isTokenValid(token)) {
        setupAutoLogout(token);
        updateUI(true);
    } else {
        updateUI(false);
    }
}

// Сохранение токена
function saveToken(token) {
    localStorage.setItem('token', token);
    const parsedToken = parseJwt(token);
    if (parsedToken?.role) {
        localStorage.setItem('userRole', parsedToken.role);
    }
}

// Получение токена
function getToken() {
    return localStorage.getItem('token');
}

// Проверка валидности токена
function isTokenValid(token) {
    try {
        const parsedToken = parseJwt(token);
        const currentTime = Math.floor(Date.now() / 1000);
        return parsedToken?.exp && parsedToken.exp > currentTime;
    } catch (error) {
        console.error("Ошибка при проверке токена:", error);
        return false;
    }
}

// Обновление интерфейса
function updateUI(isLoggedIn) {
    const loginButton = document.getElementById("loginButton");
    const logoutButton = document.getElementById("logoutButton");
    if (!loginButton || !logoutButton) return;

    if (isLoggedIn) {
        loginButton.style.display = "none";
        logoutButton.style.display = "inline-block";
    } else {
        loginButton.style.display = "inline-block";
        logoutButton.style.display = "none";
    }
}

// Проверка OpenID авторизации
function checkOpenIDAuth() {
    const token = getToken();
    if (!token) {
        if (isLocalhost()) {
            console.warn("Локальное тестирование: OpenID авторизация пропущена.");
            return;
        }
        redirectToOpenIDLogin();
    }
}

// Проверка, работает ли приложение на localhost
function isLocalhost() {
    return window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1";
}

// Редирект на OpenID
function redirectToOpenIDLogin() {
    const state = generateRandomState();
    const nonce = generateRandomState();
    sessionStorage.setItem('state', state);
    sessionStorage.setItem('nonce', nonce);

    const url = `${openIdConfig.loginUrl}&response_type=code&scope=${encodeURIComponent(openIdConfig.scope)}&client_id=${encodeURIComponent(openIdConfig.clientId)}&state=${encodeURIComponent(state)}&redirect_uri=${encodeURIComponent(openIdConfig.redirectUri)}&nonce=${encodeURIComponent(nonce)}`;
    window.location.href = url;
}

// Генерация случайного состояния
function generateRandomState() {
    return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
}

// Установка автоматического выхода
function setupAutoLogout(token) {
    const payload = parseJwt(token);
    const expirationTime = payload.exp * 1000;
    const logoutTime = expirationTime - Date.now() - 5 * 60 * 1000;
    if (logoutTime > 0) {
        setTimeout(() => logout(), logoutTime);
    } else {
        logout();
    }
}

// Выход из системы
function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userRole');
    updateUI(false);
    window.location.href = '/';
}

// Декодирование токена JWT
function parseJwt(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = atob(base64);
        return JSON.parse(jsonPayload);
    } catch (error) {
        console.error("Ошибка декодирования токена:", error);
        return null;
    }
}
const openIdConfig = {
    clientId: '89a015d24a66b01a77fe30059820593e177c43b32c9c3c4ea711eb5610639347',
    redirectUri: 'http://repair.laop.ulstu.ru/wp-admin/admin-ajax.php?action=openid-connect-authorize',
    loginUrl: 'https://lk.ulstu.ru/?q=auth%2Flogin',
    scope: 'openid',
};