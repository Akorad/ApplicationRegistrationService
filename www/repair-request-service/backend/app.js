//тест нового openid
const express = require('express');
const path = require('path');
const cookieParser = require('cookie-parser');

const app = express();
const PORT = 3000;

// Подключаем middleware для работы с cookie
app.use(cookieParser());

// Настройка для обслуживания статических файлов
app.use(express.static(path.join(__dirname, 'frontend')));

// Устанавливаем EJS как шаблонизатор
app.set('view engine', 'ejs');

// Папка с шаблонами
app.set("views", path.join(__dirname, "views"));

// Главная страница
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'frontend', 'html','index.html'));
});

// Страница расходных материалов
app.get('/stock', (req, res) => {
    res.sendFile(path.join(__dirname, 'frontend', 'html','stock.html'));
});

// Страница для гостя (только локальная авторизация)
app.get('/guest', (req, res) => {
    res.sendFile(path.join(__dirname, 'frontend', 'html','guest.html'));
});

// Страница для управления пользователями (только локальная авторизация)
app.get('/users', (req, res) => {
    res.sendFile(path.join(__dirname, 'frontend', 'html','users.html'));
});

// Обработка OpenID callback после авторизации
app.get('/auth/callback', (req, res) => {
    const { code, state } = req.query;

    if (!code) {
        res.status(400).send('Ошибка авторизации: не удалось получить код авторизации.');
        return;
    }

    // Логика для обмена кода на токен и сохранения токена в cookie
    // Для упрощения предполагается, что токен передаётся через query string
    const token = req.query.token;

    if (token) {
        res.cookie('authToken', token, {
            maxAge: 48 * 60 * 60 * 1000, // 48 часов
            httpOnly: true,
            secure: true,
            sameSite: 'Strict'
        });
        res.redirect('/'); // Редирект на главную страницу после успешной авторизации
    } else {
        res.status(400).send('Ошибка авторизации: токен не был получен.');
    }
});

// Запуск сервера
app.listen(PORT, () => {
    console.log("Сервер запущен на http://localhost:3000");
});
