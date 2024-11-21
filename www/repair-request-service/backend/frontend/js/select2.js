document.addEventListener("DOMContentLoaded", function () {
    const supplyData = [];
    const selectedSupplies = [];
    let searchInput, dropdown, selectedSuppliesContainer;

    // Загрузка данных с сервера
    async function loadSupplies() {
        try {
            const response = await fetch("http://localhost:8080/supplies/mol/Дроздова Татьяна Викторовна");
            const data = await response.json();
            supplyData.push(
                ...data.map((item) => ({
                    id: item.НоменклатураКод,
                    name: item.Номенклатура,
                }))
            );
        } catch (error) {
            console.error("Ошибка загрузки данных:", error);
            alert("Ошибка при загрузке данных. Проверьте соединение с сервером.");
        }
    }

    // Создание элемента расходного материала
    function createSupplyElement(supply) {
        const div = document.createElement("div");
        div.classList.add("selected-item");
        div.setAttribute("data-nomenclature-code", supply.id);

        div.innerHTML = `
            <span>${supply.name}</span>
            <input type="number" value="${supply.quantity}" min="1" />
            <button class="btn btn-danger btn-sm">Удалить</button>
        `;

        div.querySelector("input").addEventListener("input", (e) => {
            supply.quantity = Number(e.target.value);
        });

        div.querySelector("button").addEventListener("click", () => {
            const index = selectedSupplies.indexOf(supply);
            if (index > -1) selectedSupplies.splice(index, 1);
            div.remove();
        });

        return div;
    }

    // Рендеринг выбранных материалов
    function renderSelectedSupplies() {
        selectedSuppliesContainer.innerHTML = "";
        selectedSupplies.forEach((supply) => {
            const supplyElement = createSupplyElement(supply);
            selectedSuppliesContainer.appendChild(supplyElement);
        });
    }

    // Позиционирование выпадающего списка
    function positionDropdown() {
        const inputRect = searchInput.getBoundingClientRect();
        const dropdownHeight = dropdown.offsetHeight;
        const offsetParentRect = dropdown.offsetParent.getBoundingClientRect();

        dropdown.style.left = `${inputRect.left - offsetParentRect.left}px`;
        dropdown.style.width = `${inputRect.width}px`;

        const spaceAbove = inputRect.top - offsetParentRect.top;
        if (spaceAbove >= dropdownHeight + 10) {
            dropdown.style.top = `${inputRect.top - offsetParentRect.top - dropdownHeight - 5}px`;
        } else {
            dropdown.style.top = `${inputRect.bottom - offsetParentRect.top + 5}px`;
        }
    }

    // Рендеринг выпадающего списка
    function renderDropdown(items) {
        dropdown.innerHTML = "";

        if (items.length === 0) {
            dropdown.classList.remove("active");
            return;
        }

        items.forEach((item) => {
            const option = document.createElement("div");
            option.classList.add("dropdown-item");
            option.textContent = item.name;
            option.dataset.id = item.id;
            dropdown.appendChild(option);

            option.addEventListener("click", () => {
                addSupply(item);
                dropdown.classList.remove("active");
                searchInput.value = "";
            });
        });

        dropdown.classList.add("active");
        positionDropdown();
    }

    // Добавление расходного материала
    function addSupply(item) {
        if (selectedSupplies.some((supply) => supply.id === item.id)) return;
        selectedSupplies.push({ id: item.id, name: item.name, quantity: 1 });
        renderSelectedSupplies();
    }

    // Обработчики поиска
    function setupSearchHandlers() {
        searchInput.addEventListener("focus", () => {
            if (supplyData.length > 0) renderDropdown(supplyData);
        });

        searchInput.addEventListener("input", () => {
            const query = searchInput.value;
            const filtered = query
                ? supplyData.filter((item) =>
                    item.name.toLowerCase().includes(query.toLowerCase())
                )
                : supplyData;

            renderDropdown(filtered);
        });

        document.addEventListener("click", (e) => {
            if (!dropdown.contains(e.target) && !searchInput.contains(e.target)) {
                dropdown.classList.remove("active");
            }
        });
    }

    // Сброс состояния модального окна
    function resetModal() {
        selectedSupplies.length = 0; // Очистка массива
        if (selectedSuppliesContainer) {
            selectedSuppliesContainer.innerHTML = ""; // Очистка контейнера
        }
    }

    // Инициализация модального окна
    async function initializeModal() {
        searchInput = document.getElementById("supplySearchInput");
        dropdown = document.getElementById("supplyDropdown");
        selectedSuppliesContainer = document.getElementById("selectedSupplies");

        if (!searchInput || !dropdown || !selectedSuppliesContainer) {
            console.error("Не удалось найти элементы модального окна.");
            return;
        }

        if (supplyData.length === 0) await loadSupplies();
        setupSearchHandlers();

        const suppliesJson = selectedSuppliesContainer.getAttribute("data-supplies-json");
        if (suppliesJson) {
            try {
                const suppliesData = JSON.parse(suppliesJson);
                suppliesData.forEach((supply) => {
                    selectedSupplies.push({
                        id: supply.nomenclatureCode,
                        name: supply.nomenclature,
                        quantity: supply.quantity,
                    });
                });
                renderSelectedSupplies();
            } catch (error) {
                console.error("Ошибка парсинга JSON:", error);
            }
        }
    }

    // Открытие модального окна
    $(document).on("shown.bs.modal", "#ticketInfoModal", async function () {
        resetModal(); // Сброс состояния перед открытием
        await initializeModal();
    });
});
