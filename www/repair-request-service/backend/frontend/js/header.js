document.addEventListener("DOMContentLoaded", function () {
    const headerContainer = document.getElementById("header-container");

    if (!headerContainer) {
        console.error("Элемент #header-container не найден.");
        return;
    }

    async function loadHeader() {
        try {

            const response = await fetch("/templates/header.html");
            if (!response.ok) throw new Error(`Ошибка загрузки: ${response.status}`);

            const headerHTML = await response.text();
            headerContainer.innerHTML = headerHTML;

            // Вызов функции checkAuthStatus сразу после загрузки хедера
            checkAuthStatus();
            initHeaderEvents();

        } catch (error) {
            console.error("Ошибка загрузки header.html:", error);
        }
    }

    loadHeader();  // Вызываем функцию загрузки хедера

// Проверка статуса авторизации и настройка хедера
    function checkAuthStatus() {
        try {
            const token = localStorage.getItem("token");
            const userRole = localStorage.getItem("userRole");

            const loginButton = document.getElementById("loginButton");
            const logoutButton = document.getElementById("logoutButton");
            const navItems = document.querySelectorAll(".ad");
            const guestItems = document.querySelectorAll(".nav-link");

            // Проверяем, что все элементы существуют в DOM
            if (!loginButton || !logoutButton || navItems.length === 0) {
                console.error("Элементы хедера не найдены. Повторяем проверку позже...");
                setTimeout(checkAuthStatus, 50); // Повторная проверка с задержкой
                return;
            }

            if (token && userRole) {
                // Пользователь авторизован
                loginButton.style.display = "none";
                logoutButton.style.display = "inline-block";

                if (userRole === "USER") {
                    // Скрываем все ссылки для пользователей с ролью USER
                    navItems.forEach(item => {
                        item.style.display = "none";
                    });
                } else if (userRole === "ADMIN") {
                    // Отображаем все ссылки для администраторов
                    navItems.forEach(item => {
                        item.style.display = "list-item";
                    });
                } else if (userRole === "GUEST") {
                    // Скрываем все ссылки для пользователей с ролью USER
                    guestItems.forEach(item => {
                        item.style.display = "none";
                    });
                }
            } else {
                // Пользователь не авторизован
                loginButton.style.display = "inline-block";
                logoutButton.style.display = "none";

                // Скрываем меню для неавторизованных
                guestItems.forEach(item => {
                    item.style.display = "none";
                });
            }
        } catch (error) {
            console.error("Ошибка в checkAuthStatus:", error);
        }
    }
});

// Инициализация событий для хедера
function initHeaderEvents() {
    const menuToggle = document.querySelector(".menu-toggle");
    const navigation = document.querySelector(".navigation");
    const navLinks = document.querySelectorAll(".nav-link");

    if (!menuToggle || !navigation || navLinks.length === 0) {
        console.error("Элементы хедера не найдены.");
        return;
    }

    menuToggle.addEventListener("click", (event) => {
        navigation.classList.toggle("active");
        event.stopPropagation();
    });

    const currentPage = window.location.pathname.split("/").pop();

    navLinks.forEach(link => {
        const linkPage = link.getAttribute("href").split("/").pop();
        if (linkPage === currentPage) {
            link.classList.add("active");
        } else {
            link.classList.remove("active");
        }
    });

    document.addEventListener("click", (event) => {
        if (!navigation.contains(event.target) && !menuToggle.contains(event.target)) {
            navigation.classList.remove("active");
        }
    });

    navLinks.forEach(link => {
        link.addEventListener("click", () => {
            if (window.innerWidth <= 768) {
                navigation.classList.remove("active");
            }
        });
    });
}