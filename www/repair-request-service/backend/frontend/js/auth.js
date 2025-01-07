document.addEventListener("DOMContentLoaded", function () {
    const authContainer = document.getElementById("modalContainerAuth");
    if (!authContainer) {
        console.error("Элемент #modalContainerAuth не найден.");
        return;
    }

    // Загружаем модальное окно авторизации
    loadAuthModal();
    setTimeout(checkAuthStatus, 200);
});

// Загружаем модальное окно авторизации
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
    loginForm.addEventListener('submit', handleLocalLogin);

    // Обработчик кнопки "Войти"
    loginButton.addEventListener("click", () => $('#loginModal').modal('show'));

    // Обработчик кнопки "Выйти"
    logoutButton.addEventListener("click", handleLogout);
}

// Обработчик отправки формы локальной авторизации
async function handleLocalLogin(event) {
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
            saveAuthToken(data.token);  // Сохраняем токен в localStorage
            alert('Вход выполнен успешно!');
            $('#loginModal').modal('hide');
            setTimeout(checkAuthStatus , 200); // Обновление состояния кнопок
            location.reload();
        } else {
            alert('Ошибка при входе. Проверьте имя пользователя и пароль.');
        }
    } catch (error) {
        alert('Ошибка при авторизации.');
        console.error("Ошибка при авторизации:", error);
    }
}

// Обработчик кнопки "Выйти"
function handleLogout() {
    clearAuthToken();
    alert("Выход выполнен");
    setTimeout(checkAuthStatus, 200); // Обновление состояния кнопок
    location.reload();
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

// Функция декодирования токена JWT
function parseJwt(token) {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(c =>
        '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
    ).join(''));

    return JSON.parse(jsonPayload);
}

// Обработка авторизации через OpenID
document.addEventListener('DOMContentLoaded', () => {
    if (window.location.pathname === '/guest') {
        handleLocalAuth();
    } else {
        checkOpenIDAuth();
    }
});

function checkOpenIDAuth() {
    const token = getAuthToken();
    if (!token) {
        redirectToOpenIDLogin();
    } else {
        validateTokenOnBackend(token)
            .then(isValid => {
                if (isValid) {
                    setupAutoLogout(token);
                } else {
                    redirectToOpenIDLogin();
                }
            })
            .catch(() => redirectToOpenIDLogin());
    }
}

function generateRandomState() {
    return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
}

function redirectToOpenIDLogin() {
    const state = generateRandomState();
    const nonce = generateRandomState();
    sessionStorage.setItem('state', state);
    sessionStorage.setItem('nonce', nonce);

    const url = `${openIdConfig.loginUrl}&response_type=code&scope=${encodeURIComponent(openIdConfig.scope)}&client_id=${encodeURIComponent(openIdConfig.clientId)}&state=${encodeURIComponent(state)}&redirect_uri=${encodeURIComponent(openIdConfig.redirectUri)}&nonce=${encodeURIComponent(nonce)}`;
    window.location.href = url;
}

function getAuthToken() {
    const cookies = document.cookie.split('; ');
    for (const cookie of cookies) {
        const [name, value] = cookie.split('=');
        if (name === 'authToken') {
            return value;
        }
    }
    return null;
}

async function validateTokenOnBackend(token) {
    try {
        const response = await fetch('/auth/validate-token', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ token }),
        });
        return response.ok;
    } catch (error) {
        console.error('Ошибка валидации токена:', error);
        return false;
    }
}

function setupAutoLogout(token) {
    const payload = parseJwt(token);
    const expirationTime = payload.exp * 1000;

    const logoutTime = expirationTime - Date.now() - 5 * 60 * 1000;
    if (logoutTime > 0) {
        setTimeout(() => {
            logout();
        }, logoutTime);
    } else {
        logout();
    }
}

function logout() {
    clearAuthToken();
    window.location.href = '/auth/callback';
}

function handleLocalAuth() {
    const loginForm = document.getElementById('loginForm');
    loginForm?.addEventListener('submit', async (event) => {
        event.preventDefault();
        const formData = new FormData(event.target);
        const requestData = {
            username: formData.get('username'),
            password: formData.get('password')
        };

        try {
            const response = await fetch(`/auth/sign-in`, {
                method: 'POST',
                body: JSON.stringify(requestData),
                headers: { 'Content-Type': 'application/json' }
            });

            if (response.ok) {
                const data = await response.json();
                saveAuthToken(data.token);  // Сохраняем токен в localStorage
                alert('Вход выполнен успешно!');
                location.reload();
            } else {
                alert('Ошибка при входе. Проверьте имя пользователя и пароль.');
            }
        } catch (error) {
            alert('Ошибка при авторизации.');
            console.error("Ошибка при авторизации:", error);
        }
    });
}

// Сохранение токена
function saveAuthToken(token) {
    localStorage.setItem('token', token);  // Сохраняем токен в localStorage
    // Вы можете использовать secure cookies или другие методы безопасности
}

// Очистка токена
function clearAuthToken() {
    localStorage.removeItem('token');
    document.cookie = "authToken=;expires=Thu, 01 Jan 1970 00:00:00 GMT";
}
