// // Подключение необходимых модулей
// const express = require('express');
// const path = require('path');
// const session = require('express-session');
// const passport = require('passport');
// const OpenIDConnectStrategy = require('passport-openidconnect').Strategy;
// const bodyParser = require('body-parser');
//
// // Отключение проверки SSL-сертификатов
// process.env.NODE_TLS_REJECT_UNAUTHORIZED = '0';
//
// const app = express();
// const PORT = process.env.PORT || 3000;
//
// // Конфигурация для OpenID Connect
// const openIDConfig = {
//     issuer: 'https://lk.ulstu.ru',
//     authorizationURL: 'https://lk.ulstu.ru/?q=oidc/auth',
//     tokenURL: 'https://lk.ulstu.ru/?q=oidc/token',
//     userInfoURL: 'https://lk.ulstu.ru/?q=oidc/userinfo',
//     clientID: '89a015d24a66b01a77fe30059820593e177c43b32c9c3c4ea711eb5610639347',
//     clientSecret: '1c1d2b50e647288824223edfbf0fafd1a003c063c5c7191b8b7ac42fb7ca714f',
//     callbackURL: 'https://repair.laop.ulstu.ru/auth/callback',
//     scope: '',
//     passReqToCallback: true
// };
//
// // Настройка Passport для работы с OpenID
// passport.use('oidc', new OpenIDConnectStrategy(openIDConfig, (req, issuer, userId, profile, accessToken, refreshToken, params, done) => {
//     const user = {
//         username: profile?.sub || userId,
//         email: profile?.email || '',
//         role: 'USER'
//     };
//     done(null, user);
// }));
//
// passport.serializeUser((user, done) => done(null, user));
// passport.deserializeUser((obj, done) => done(null, obj));
//
// app.use(session({
//     secret: 'your_secret_key',
//     resave: false,
//     saveUninitialized: false,
//     cookie: { maxAge: 48 * 60 * 60 * 1000 }
// }));
//
// app.use(passport.initialize());
// app.use(passport.session());
// app.use(bodyParser.urlencoded({ extended: true }));
// app.use(bodyParser.json());
//
// app.use(express.static(path.join(__dirname, 'frontend')));
//
// app.set('view engine', 'ejs');
// app.set("views", path.join(__dirname, "views"));
//
// app.get('/auth/login', (req, res) => res.redirect('/auth/openid'));
//
// app.get('/auth/openid', passport.authenticate('oidc'));
//
// app.get('/auth/callback', passport.authenticate('oidc', { failureRedirect: '/' }), (req, res) => {
//     const returnTo = req.session.returnTo || '/';
//     delete req.session.returnTo;
//     res.redirect(returnTo);
// });
//
// app.get('/auth/logout', (req, res) => {
//     req.logout(() => res.redirect('/'));
// });
//
// app.use((req, res, next) => {
//     if (req.path.startsWith('/guest')) return next();
//     if (req.isAuthenticated()) return next();
//     req.session.returnTo = req.originalUrl;
//     res.redirect('/auth/openid');
// });
//
// app.get('/', (req, res) => res.sendFile(path.join(__dirname, 'frontend', 'html', 'index.html')));
//
// app.get('/guest', (req, res) => res.sendFile(path.join(__dirname, 'frontend', 'html', 'guest.html')));
//
// app.get('/profile', (req, res) => {
//     if (!req.isAuthenticated()) return res.redirect('/auth/login');
//     res.send(`Профиль пользователя: ${JSON.stringify(req.user)}`);
// });
//
// app.get('/auth/status', (req, res) => res.json({ isAuthenticated: req.isAuthenticated() }));
//
// app.listen(PORT, () => console.log(`Сервер запущен на http://localhost:${PORT}`));



//без openId
// const express = require('express');
// const path = require('path');
//
// const app = express();
// const PORT = 3000;
//
// // Настройка для обслуживания статических файлов
// app.use(express.static(path.join(__dirname, 'frontend')));
//
// // Устанавливаем EJS как шаблонизатор
// app.set('view engine', 'ejs');
//
// // Папка с шаблонами
// app.set("views", path.join(__dirname, "views"));
//
// // Главная страница
// app.get('/', (req, res) => {
//     res.sendFile(path.join(__dirname, 'frontend', 'html','index.html'));
// });
//
// // Страница расходных материалов
// app.get('/stock', (req, res) => {
//     res.sendFile(path.join(__dirname, 'frontend', 'html','stock.html'));
// });
//
// // Страница для гостя
// app.get('/guest', (req, res) => {
//     res.sendFile(path.join(__dirname, 'frontend', 'html','guest.html'));
// });
//
// // Запуск сервера
// app.listen(PORT, () => {
//     console.log("Сервер запущен на http://localhost:3000");
// });

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
