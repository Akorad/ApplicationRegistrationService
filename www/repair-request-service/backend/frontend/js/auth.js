//
// document.addEventListener("DOMContentLoaded", async function () {
//     const authContainer = document.getElementById("modalContainerAuth");
//
//     if (!authContainer) {
//         console.error("Элемент #modalContainerAuth не найден.");
//         return;
//     }
//
//     // Загружаем модальное окно авторизации
//     await loadAuthModal();
//     setTimeout(checkAuthStatus , 200);
//     //checkAuthStatus(); // Проверка статуса авторизации при загрузке страницы
//
//     const urlParams = new URLSearchParams(window.location.search);
//     const token = urlParams.get('token');
//
//     if (token) {
//         // Сохраняем токен в localStorage
//         localStorage.setItem('token', token);
//
//         // Декодируем токен для получения информации о роли
//         const parsedToken = parseJwt(token);
//         if (parsedToken?.role) {
//             localStorage.setItem('userRole', parsedToken.role);
//         } else {
//             console.warn("Роль не найдена в токене.");
//         }
//
//         // Убираем токен из URL
//         window.history.replaceState({}, document.title, window.location.pathname);
//     }
//     checkAuthStatus();
//
// });
//
// // Загрузка модального окна авторизации
// async function loadAuthModal() {
//     try {
//         const response = await fetch("/templates/auth.html");
//         if (!response.ok) throw new Error(`Ошибка загрузки: ${response.status}`);
//         const authHTML = await response.text();
//         document.getElementById("modalContainerAuth").innerHTML = authHTML;
//
//         // Инициализация событий для авторизации
//         initAuthEvents();
//     } catch (error) {
//         console.error("Ошибка загрузки auth.html:", error);
//     }
// }
//
// // Инициализация событий для авторизации
// function initAuthEvents() {
//     const loginForm = document.getElementById('loginForm');
//     const loginButton = document.getElementById("loginButton");
//     const logoutButton = document.getElementById("logoutButton");
//
//     if (!loginForm || !loginButton || !logoutButton) {
//         console.error("Не все элементы авторизации загружены.");
//         return;
//     }
//
//     // Обработчик отправки формы авторизации
//     loginForm.addEventListener('submit', handleLogin);
//
//     // Обработчик кнопки "Войти"
//     loginButton.addEventListener("click", () => $('#loginModal').modal('show'));
//
//     // Обработчик кнопки "Выйти"
//     logoutButton.addEventListener("click", handleLogout);
// }
//
// // Обработчик отправки формы авторизации
// async function handleLogin(event) {
//     event.preventDefault();
//     const formData = new FormData(event.target);
//     const requestData = {
//         username: formData.get('username'),
//         password: formData.get('password')
//     };
//
//     try {
//         const response = await fetch(`${window.config.apiUrl}/auth/sign-in`,  {
//             method: 'POST',
//             body: JSON.stringify(requestData),
//             headers: { 'Content-Type': 'application/json' }
//         });
//
//         if (response.ok) {
//             const data = await response.json();
//             localStorage.setItem('token', data.token);
//
//             // Декодирование роли пользователя
//             const parsedToken = parseJwt(data.token);
//             if (parsedToken?.role) {
//                 localStorage.setItem('userRole', parsedToken.role);
//                 showAlert('Вход выполнен успешно!');
//                 $('#loginModal').modal('hide');
//                 setTimeout(checkAuthStatus , 200); // Обновление состояния кнопок
//                 location.reload();
//             } else {
//                 console.error("Роль не найдена");
//             }
//         } else {
//             showAlert('Ошибка при входе. Проверьте имя пользователя и пароль.');
//         }
//     } catch (error) {
//         showAlert('Ошибка при авторизации.', 'danger'); // Используем Bootstrap alert danger
//         console.error("Ошибка при авторизации:", error);
//     }
// }
//
// // Обработчик кнопки "Выйти"
// function handleLogout() {
//     localStorage.removeItem("token");
//     localStorage.removeItem("userRole");
//     showAlert("Выход выполнен");
//     setTimeout(checkAuthStatus , 200);// Обновление состояния кнопок
//     location.reload();
// }
//
// // Проверка статуса авторизации
// function checkAuthStatus() {
//     const token = localStorage.getItem("token");
//     const loginButton = document.getElementById("loginButton");
//     const logoutButton = document.getElementById("logoutButton");
//
//     if (!loginButton || !logoutButton) {
//         setTimeout(checkAuthStatus, 50); // Повторная попытка, если элементы еще не загружены
//         return;
//     }
//
//     if (token) {
//         const parsedToken = parseJwt(token);
//
//         // Проверяем срок действия токена
//         const currentTime = Math.floor(Date.now() / 1000); // Текущее время в секундах
//         if (parsedToken?.exp && parsedToken.exp < currentTime) {
//             console.warn("Токен истек.");
//             handleLogout(); // Удаляем токен и обновляем интерфейс
//             return;
//         }
//
//         // Если токен валиден
//         if (parsedToken?.role) {
//             loginButton.style.display = "none";
//             logoutButton.style.display = "inline-block";
//         } else {
//             handleLogout(); // Удаляем токен и обновляем интерфейс
//         }
//     } else {
//         loginButton.style.display = "inline-block";
//         logoutButton.style.display = "none";
//     }
// }
//
// // Функция декодирования токена JWT
// function parseJwt(token) {
//     const base64Url = token.split('.')[1];
//     const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
//     const jsonPayload = decodeURIComponent(atob(base64).split('').map(c =>
//         '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
//     ).join(''));
//
//     return JSON.parse(jsonPayload);
// }
//
// //тест новго опен айды
// const openIdConfig = {
//     clientId: '89a015d24a66b01a77fe30059820593e177c43b32c9c3c4ea711eb5610639347',
//     redirectUri: 'http://repair.laop.ulstu.ru/wp-admin/admin-ajax.php?action=openid-connect-authorize',
//     loginUrl: 'https://lk.ulstu.ru/?q=auth%2Flogin',
//     scope: 'openid',
// };
//
// document.addEventListener('DOMContentLoaded', () => {
//     if (window.location.pathname === '/guest') {
//         handleLocalAuth();
//     } else {
//         checkOpenIDAuth();
//     }
// });
//
// function checkOpenIDAuth() {
//     const token = getAuthToken();
//     if (!token) {
//         redirectToOpenIDLogin();
//     } else {
//
//     }
// }
//
// function generateRandomState() {
//     return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
// }
//
// function redirectToOpenIDLogin() {
//     const state = generateRandomState();
//     const nonce = generateRandomState();
//     sessionStorage.setItem('state', state);
//     sessionStorage.setItem('nonce', nonce);
//
//     const url = `${openIdConfig.loginUrl}&response_type=code&scope=${encodeURIComponent(openIdConfig.scope)}&client_id=${encodeURIComponent(openIdConfig.clientId)}&state=${encodeURIComponent(state)}&redirect_uri=${encodeURIComponent(openIdConfig.redirectUri)}&nonce=${encodeURIComponent(nonce)}`;
//     window.location.href = url;
// }
//
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
//
// function setupAutoLogout(token) {
//     const payload = parseJwt(token);
//     const expirationTime = payload.exp * 1000;
//
//     const logoutTime = expirationTime - Date.now() - 5 * 60 * 1000;
//     if (logoutTime > 0) {
//         setTimeout(() => {
//             logout();
//         }, logoutTime);
//     } else {
//         logout();
//     }
// }
//
// function logout() {
//     clearAuthToken();
//     window.location.href = '/';
// }
//
// function handleLocalAuth() {
//     const loginForm = document.getElementById('loginForm');
//     loginForm?.addEventListener('submit', async (event) => {
//         event.preventDefault();
//         const formData = new FormData(event.target);
//         const requestData = {
//             username: formData.get('username'),
//             password: formData.get('password')
//         };
//
//         try {
//             const response = await fetch(`/auth/sign-in`, {
//                 method: 'POST',
//                 body: JSON.stringify(requestData),
//                 headers: { 'Content-Type': 'application/json' }
//             });
//
//             if (response.ok) {
//                 const data = await response.json();
//                 localStorage.setItem('token', data.token);
//                 alert('Вход выполнен успешно!');
//                 location.reload();
//             } else {
//                 alert('Ошибка при входе. Проверьте имя пользователя и пароль.');
//             }
//         } catch (error) {
//             alert('Ошибка при авторизации.');
//             console.error("Ошибка при авторизации:", error);
//         }
//     });
// }
//
document.addEventListener("DOMContentLoaded", async function () {
    const authContainer = document.getElementById("modalContainerAuth");
    if (!authContainer) {
        console.error("Элемент #modalContainerAuth не найден.");
        return;
    }

    await loadAuthModal();
    initializeAuth(); // Инициализация авторизации

    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get('token');
    if (token) {
        saveToken(token);
        window.history.replaceState({}, document.title, window.location.pathname);
    }

    if (window.location.pathname === '/guest') {
        handleLocalAuth();
    } else {
        checkOpenIDAuth();
    }
});

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
        redirectToOpenIDLogin();
    }
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
