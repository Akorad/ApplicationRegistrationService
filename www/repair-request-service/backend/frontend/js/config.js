const isProduction = window.location.hostname === 'repair.laop.ulstu.ru';

window.config = {
    apiUrl: isProduction ? 'http://repair.laop.ulstu.ru' : 'http://localhost:8080'
};
