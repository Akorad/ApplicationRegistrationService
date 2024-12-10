const express = require('express');
const path = require('path');

const app = express();
const PORT = 3000;

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

// Страница для гостя
app.get('/guest', (req, res) => {
    res.sendFile(path.join(__dirname, 'frontend', 'html','guest.html'));
});

// Запуск сервера
app.listen(PORT, () => {
    console.log(`Сервер запущен на http://localhost:${PORT}`);
});