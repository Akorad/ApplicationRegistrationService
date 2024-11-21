const menuToggle = document.querySelector(".menu-toggle");
const navigation = document.querySelector(".navigation");
const navLinks = document.querySelectorAll(".nav-link");
const tabs = document.querySelectorAll(".tab-content");

// Переключение меню
menuToggle.addEventListener("click", (event) => {
    navigation.classList.toggle("active");
    event.stopPropagation(); // Предотвращаем всплытие события клика
});

// Переключение вкладок
navLinks.forEach(link => {
    link.addEventListener("click", (event) => {
        event.preventDefault();

        // Удаляем активный класс со всех ссылок
        navLinks.forEach(link => link.classList.remove("active"));
        // Устанавливаем активный класс на текущую ссылку
        event.target.classList.add("active");

        // Показываем соответствующую вкладку
        const targetTab = event.target.getAttribute("href").substring(1);
        tabs.forEach(tab => {
            tab.classList.toggle("active", tab.id === targetTab);
        });

        // Закрываем меню на мобильных устройствах
        if (window.innerWidth <= 768) {
            navigation.classList.remove("active");
        }
    });
});

// Закрытие меню при клике вне его области
document.addEventListener("click", (event) => {
    if (!navigation.contains(event.target) && !menuToggle.contains(event.target)) {
        navigation.classList.remove("active");
    }
});
