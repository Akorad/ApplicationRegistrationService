
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
//тест опен айды
// // Устанавливаем токен в HTTP-only Secure Cookie
// function setAuthToken(token) {
//     document.cookie = `authToken=${token}; Max-Age=172800; Path=/; Secure; HttpOnly; SameSite=Strict;`;
// }
//
// // Удаляем токен из Cookie
// function clearAuthToken() {
//     document.cookie = 'authToken=; Max-Age=0; Path=/; Secure; HttpOnly; SameSite=Strict;';
// }
//
// // Получаем токен из Cookie
// function getAuthToken() {
//     const cookies = document.cookie.split('; ');
//     for (const cookie of cookies) {
//         const [name, value] = cookie.split('=');
//         if (name === 'authToken') {
//             return value;
//         }
//     }
//     return null;
// }
//
// // Функция автоматического логаута при истечении срока действия токена
// function setupAutoLogout(expirationTime) {
//     const logoutTime = expirationTime - Date.now() - 5 * 60 * 1000; // 5 минут до истечения
//     if (logoutTime > 0) {
//         setTimeout(() => {
//             alert('Ваша сессия истекает, вы будете перенаправлены на страницу входа.');
//             logout();
//         }, logoutTime);
//     } else {
//         logout();
//     }
// }
//
// // Логаут пользователя (удаляем токен и перенаправляем на страницу входа)
// function logout() {
//     clearAuthToken();
//     window.location.href = '/login'; // Перенаправление на страницу входа
// }
//
// // Обработчик события для кнопки "Выйти"
// document.getElementById('logoutButton')?.addEventListener('click', () => {
//     logout();
// });
//
// // Проверяем токен при загрузке страницы и настраиваем авто-логаут
// window.addEventListener('load', () => {
//     const token = getAuthToken();
//     if (token) {
//         const payload = JSON.parse(atob(token.split('.')[1])); // Декодируем payload токена JWT
//         const expirationTime = payload.exp * 1000; // Время истечения токена
//         setupAutoLogout(expirationTime);
//     } else {
//         window.location.href = '/login'; // Перенаправление на страницу входа, если токена нет
//     }
// });
//
// const openIdConfig = {
//     loginUrl: 'https://lk.ulstu.ru/?q=oidc/auth',
//     clientId: '89a015d24a66b01a77fe30059820593e177c43b32c9c3c4ea711eb5610639347',
//     redirectUri: window.location.origin + '/login',
//     responseType: 'token',
//     scope: 'openid',
// };
//
// function redirectToOpenIDLogin() {
//     const loginUrl = `${openIdConfig.loginUrl}?response_type=${openIdConfig.responseType}&client_id=${openIdConfig.clientId}&redirect_uri=${encodeURIComponent(openIdConfig.redirectUri)}&scope=${openIdConfig.scope}`;
//     window.location.href = loginUrl;
// }
//
// function handleOpenIDResponse() {
//     const hash = window.location.hash.substring(1);
//     const params = new URLSearchParams(hash);
//     const token = params.get('access_token');
//     if (token) {
//         setAuthToken(token);
//         window.location.href = '/'; // Перенаправляем на домашнюю страницу
//     } else {
//         console.error('Token not found in OpenID response');
//     }
// }
//
// window.addEventListener('DOMContentLoaded', () => {
//     if (window.location.pathname === '/login') {
//         handleOpenIDResponse();
//     }
// });

//тест новго опен айды
const openIdConfig = {
    clientId: '89a015d24a66b01a77fe30059820593e177c43b32c9c3c4ea711eb5610639347',
    redirectUri: 'http://repair.laop.ulstu.ru/wp-admin/admin-ajax.php?action=openid-connect-authorize',
    loginUrl: 'https://lk.ulstu.ru/?q=auth%2Flogin',
    scope: 'openid',
};

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

function parseJwt(token) {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(c =>
        '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
    ).join(''));
    return JSON.parse(jsonPayload);
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
                localStorage.setItem('token', data.token);
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

