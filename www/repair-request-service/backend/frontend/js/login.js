const express = require('express');
const axios = require('axios');
const router = express.Router();
const cookieParser = require('cookie-parser');

router.use(cookieParser());

// OpenID конфигурация
const openIDConfig = {
    clientId: '89a015d24a66b01a77fe30059820593e177c43b32c9c3c4ea711eb5610639347',
    redirectUri: 'http://repair.laop.ulstu.ru/wp-admin/admin-ajax.php?action=openid-connect-authorize',
    loginUrl: 'https://lk.ulstu.ru/?q=auth%2Flogin',
    tokenUrl: 'https://lk.ulstu.ru/?q=oidc/token',
    scope: 'openid',
    clientSecret: '1c1d2b50e647288824223edfbf0fafd1a003c063c5c7191b8b7ac42fb7ca714f'
};

// Функция для получения ссылки на авторизацию OpenID
function getOpenIDAuthUrl(state, nonce) {
    return `${openIDConfig.loginUrl}&response_type=code&scope=${encodeURIComponent(openIDConfig.scope)}&client_id=${encodeURIComponent(openIDConfig.clientId)}&state=${encodeURIComponent(state)}&redirect_uri=${encodeURIComponent(openIDConfig.redirectUri)}&nonce=${encodeURIComponent(nonce)}`;
}

// Middleware для проверки наличия токена
function ensureAuthenticated(req, res, next) {
    const token = req.cookies['auth_token'];
    if (!token) {
        const state = generateRandomState();
        const nonce = '123456'; // Статический nonce для тестов
        const authUrl = getOpenIDAuthUrl(state, nonce);
        return res.redirect(authUrl);
    }
    next();
}

// Генерация случайного состояния для защиты от CSRF-атак
function generateRandomState() {
    return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
}

// Callback для обработки ответа от OpenID
router.get('/auth/callback', async (req, res) => {
    const code = req.query.code;
    if (!code) {
        return res.status(400).send('Не указан код авторизации');
    }

    try {
        const tokenResponse = await axios.post(openIDConfig.tokenUrl, {
            grant_type: 'authorization_code',
            code: code,
            redirect_uri: openIDConfig.redirectUri,
            client_id: openIDConfig.clientId,
            client_secret: openIDConfig.clientSecret
        });

        const token = tokenResponse.data.access_token;
        if (!token) {
            return res.status(400).send('Не удалось получить токен');
        }

        // Сохраняем токен в куки
        res.cookie('auth_token', token, { httpOnly: true });
        res.redirect('/'); // Редирект на главную страницу после успешной авторизации
    } catch (error) {
        console.error('Ошибка при обмене кода на токен:', error.response?.data || error.message);
        res.status(500).send('Произошла ошибка при авторизации');
    }
});

// Ручной выход пользователя
router.get('/logout', (req, res) => {
    res.clearCookie('auth_token');
    res.redirect('/');
});

module.exports = router;
