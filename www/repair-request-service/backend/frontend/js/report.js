// Конфигурация API
window.config = {
    apiUrl: 'http://your-api-url.com' // Замените на ваш базовый URL API
};

// Функция для отображения вкладки
function showTab(tabId) {
    const tabs = document.querySelectorAll('.tab-content');
    tabs.forEach(tab => {
        tab.classList.toggle('active', tab.id === tabId);
    });
}

// Переменные для графиков
let summaryChart = null;
let materialsChart = null;
let trendsChart = null;
let forecastChart = null;

// Функция для загрузки сводного отчета
async function loadSummaryReport() {
    const startDate = document.getElementById('summaryStartDate').value;
    const endDate = document.getElementById('summaryEndDate').value;
    const token = localStorage.getItem('token'); // Получаем токен из localStorage

    try {
        const summaryData = await fetchData('/api/reports/summary', { startDate, endDate }, token);
        renderSummaryReport(summaryData);
    } catch (error) {
        console.error('Ошибка при загрузке сводного отчета:', error);
        alert('Не удалось загрузить сводный отчет. Пожалуйста, попробуйте позже.');
    }
}

// Функция для загрузки отчета по материалам
async function loadMaterialsReport() {
    const startDate = document.getElementById('materialsStartDate').value;
    const endDate = document.getElementById('materialsEndDate').value;
    const token = localStorage.getItem('token');

    try {
        const materialData = await fetchData('/api/reports/materials', { startDate, endDate }, token);
        renderMaterialUsageReport(materialData);
    } catch (error) {
        console.error('Ошибка при загрузке отчета по материалам:', error);
        alert('Не удалось загрузить отчет по материалам. Пожалуйста, попробуйте позже.');
    }
}

// Функция для загрузки отчета по тенденциям
async function loadTrendsReport() {
    const startDate = document.getElementById('trendsStartDate').value;
    const endDate = document.getElementById('trendsEndDate').value;
    const token = localStorage.getItem('token');

    try {
        const trendData = await fetchData('/api/reports/trends', { startDate, endDate }, token);
        renderTrendReport(trendData);
    } catch (error) {
        console.error('Ошибка при загрузке отчета по тенденциям:', error);
        alert('Не удалось загрузить отчет по тенденциям. Пожалуйста, попробуйте позже.');
    }
}

// Функция для загрузки прогноза
async function loadForecastReport() {
    const months = document.getElementById('forecastMonths').value;
    const token = localStorage.getItem('token');

    try {
        const forecastData = await fetchData('/api/reports/material-forecast', { months }, token);
        renderForecastReport(forecastData);
    } catch (error) {
        console.error('Ошибка при загрузке прогноза:', error);
        alert('Не удалось загрузить прогноз. Пожалуйста, попробуйте позже.');
    }
}

// Функция для получения данных с API
async function fetchData(endpoint, params = {}, token) {
    const url = new URL(endpoint, window.config.apiUrl);
    Object.keys(params).forEach(key => {
        if (params[key] !== undefined && params[key] !== null) {
            url.searchParams.append(key, params[key]);
        }
    });

    const response = await fetch(url, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });

    if (!response.ok) {
        throw new Error(`Ошибка HTTP: ${response.status}`);
    }

    return await response.json();
}

// Функция для отрисовки сводного отчета
function renderSummaryReport(data) {
    // Текстовые метрики
    document.getElementById('totalTickets').textContent = data.totalTickets;
    document.getElementById('averageCompletionTime').textContent = data.averageCompletionTime.toFixed(2);

    // Заявки по типам оборудования
    const ticketsByEquipmentList = document.getElementById('ticketsByEquipmentList');
    ticketsByEquipmentList.innerHTML = Object.entries(data.ticketsByEquipment)
        .map(([key, value]) => `<li>${key}: ${value}</li>`)
        .join('');

    // Заявки по исполнителям
    const ticketsByUserList = document.getElementById('ticketsByUserList');
    ticketsByUserList.innerHTML = Object.entries(data.ticketsByUser)
        .map(([key, value]) => `<li>${key}: ${value}</li>`)
        .join('');

    // График
    const ctx = document.getElementById('summaryChart').getContext('2d');
    if (summaryChart) summaryChart.destroy();
    summaryChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: Object.keys(data.ticketsByEquipment),
            datasets: [{
                label: 'Заявки по типам оборудования',
                data: Object.values(data.ticketsByEquipment),
                backgroundColor: 'rgba(75, 192, 192, 0.2)',
                borderColor: 'rgba(75, 192, 192, 1)',
                borderWidth: 1
            }]
        },
        options: {
            scales: { y: { beginAtZero: true } },
            animation: { duration: 1000, easing: 'easeInOutQuad' }
        }
    });
}

// Функция для отрисовки отчета по материалам
function renderMaterialUsageReport(data) {
    // Расход материалов
    const materialUsageList = document.getElementById('materialUsageList');
    materialUsageList.innerHTML = Object.entries(data.materialUsage)
        .map(([key, value]) => `<li>${key}: ${value}</li>`)
        .join('');

    // График
    const ctx = document.getElementById('materialsChart').getContext('2d');
    if (materialsChart) materialsChart.destroy();
    materialsChart = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: Object.keys(data.materialUsage),
            datasets: [{
                label: 'Расход материалов',
                data: Object.values(data.materialUsage),
                backgroundColor: [
                    'rgba(255, 99, 132, 0.2)',
                    'rgba(54, 162, 235, 0.2)',
                    'rgba(255, 206, 86, 0.2)'
                ],
                borderColor: [
                    'rgba(255, 99, 132, 1)',
                    'rgba(54, 162, 235, 1)',
                    'rgba(255, 206, 86, 1)'
                ],
                borderWidth: 1
            }]
        },
        options: {
            plugins: {
                legend: {
                    display: false // Отключаем легенду
                }
            },
            animation: { duration: 1000, easing: 'easeInOutQuad' }
        }
    });
}

// Функция для отрисовки отчета по тенденциям
function renderTrendReport(data) {
    // Прогнозируемое количество заявок
    document.getElementById('predictedTickets').textContent = data.predictedTickets;

    // Словарь для перевода месяцев
    const monthTranslations = {
        JANUARY: "Январь",
        FEBRUARY: "Февраль",
        MARCH: "Март",
        APRIL: "Апрель",
        MAY: "Май",
        JUNE: "Июнь",
        JULY: "Июль",
        AUGUST: "Август",
        SEPTEMBER: "Сентябрь",
        OCTOBER: "Октябрь",
        NOVEMBER: "Ноябрь",
        DECEMBER: "Декабрь"
    };

    // Сезонность
    const seasonalityList = document.getElementById('seasonalityList');
    seasonalityList.innerHTML = Object.entries(data.seasonality)
        .map(([key, value]) => {
            const translatedMonth = monthTranslations[key] || key; // Переводим месяц
            return `<li>${translatedMonth}: ${value}</li>`; // Возвращаем переведенный элемент
        })
        .join('');

    // График
    const ctx = document.getElementById('trendsChart').getContext('2d');
    if (trendsChart) trendsChart.destroy();
    trendsChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: Object.keys(data.ticketTrends),
            datasets: [{
                label: 'Тенденции по заявкам',
                data: Object.values(data.ticketTrends),
                borderColor: 'rgba(153, 102, 255, 1)',
                borderWidth: 2,
                fill: false
            }]
        },
        options: {
            scales: { y: { beginAtZero: true } },
            animation: { duration: 1000, easing: 'easeInOutQuad' }
        }
    });
}

// Функция для отрисовки прогноза
function renderForecastReport(data) {
    // Прогнозируемый расход материалов
    const forecastedMaterialUsageList = document.getElementById('forecastedMaterialUsageList');
    forecastedMaterialUsageList.innerHTML = Object.entries(data.forecastedMaterialUsage)
        .map(([key, value]) => `<li>${key}: ${value}</li>`)
        .join('');

    // График
    const ctx = document.getElementById('forecastChart').getContext('2d');
    if (forecastChart) forecastChart.destroy();
    forecastChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: Object.keys(data.forecastedMaterialUsage),
            datasets: [{
                label: 'Прогнозируемый расход материалов',
                data: Object.values(data.forecastedMaterialUsage),
                backgroundColor: 'rgba(255, 159, 64, 0.2)',
                borderColor: 'rgba(255, 159, 64, 1)',
                borderWidth: 1
            }]
        },
        options: {
            scales: {
                x: {
                    ticks: {
                        display: false // Отключаем подписи на оси X
                    },
                    grid: {
                        display: false // Отключаем сетку на оси X (опционально)
                    }
                },
                y: {
                    beginAtZero: true
                }
            },
            animation: { duration: 1000, easing: 'easeInOutQuad' }
        }
    });
}

// Загрузка отчетов при загрузке страницы
document.addEventListener('DOMContentLoaded', () => {
    loadSummaryReport();
    loadMaterialsReport();
    loadTrendsReport();
    loadForecastReport();
});