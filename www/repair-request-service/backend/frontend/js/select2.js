document.addEventListener("DOMContentLoaded", function () {
    const supplyData = [];
    const selectedSupplies = [];
    let searchInput, dropdown, selectedSuppliesContainer;

// Загрузка данных с сервера
    async function loadSupplies() {
        try {
            const token = localStorage.getItem('token');
            const apiUrl1C = `${window.config.apiUrl}/api/supplies/mol/Дроздова Татьяна Викторовна`;
            const apiUrlStock = `${window.config.apiUrl}/api/stock-supplies/all`;

            // Выполняем запросы параллельно
            const [response1C, responseStock] = await Promise.all([
                fetch(apiUrl1C, {
                    method: 'GET',
                    headers: {
                        "Authorization": `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                }),
                fetch(apiUrlStock, {
                    method: 'GET',
                    headers: {
                        "Authorization": `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                })
            ]);

            // Проверяем успешность запросов
            if (!response1C.ok) throw new Error(`Ошибка 1С: ${response1C.status}`);
            if (!responseStock.ok) throw new Error(`Ошибка склада: ${responseStock.status}`);

            // Получаем JSON-данные
            const [data1C, dataStock] = await Promise.all([
                response1C.json(),
                responseStock.json()
            ]);

            const uniqueNomenclatures = new Map();
            // Обработка данных со склада
            dataStock.forEach(item => {
                const nomenclatureCode = item.nomenclatureCode;
                if (!uniqueNomenclatures.has(nomenclatureCode)) {
                    uniqueNomenclatures.set(nomenclatureCode, {
                        id: nomenclatureCode,
                        name: "(Склад) "+item.nomenclature
                    });
                }
            });

            // Обработка данных 1С
            data1C.forEach(item => {
                const nomenclatureCode = item.НоменклатураКод;
                if (!uniqueNomenclatures.has(nomenclatureCode)) {
                    uniqueNomenclatures.set(nomenclatureCode, {
                        id: nomenclatureCode,
                        name: "(1С) "+item.Номенклатура
                    });
                }
            });



            // Очищаем supplyData и добавляем новые данные
            supplyData.length = 0;
            supplyData.push(...uniqueNomenclatures.values());
        } catch (error) {
            console.error("Ошибка загрузки данных:", error);
            showAlert("Ошибка при загрузке данных. Проверьте соединение с сервером.");
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
            enableDropdownOption(supply.id); // Активируем пункт в выпадающем списке
            div.remove();
        });

        return div;
    }

    // Включение пункта в выпадающем списке
    function enableDropdownOption(itemId) {
        const dropdownItems = dropdown.querySelectorAll(".dropdown-item");
        dropdownItems.forEach((option) => {
            if (option.dataset.id === String(itemId)) {
                option.classList.remove("disabled"); // Убираем класс "disabled"
                option.style.pointerEvents = "auto"; // Включаем клики
            }
        });
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
            const isAlreadySelected = selectedSupplies.some(
                (supply) => supply.id === item.id
            );

            const option = document.createElement("div");
            option.classList.add("dropdown-item");
            if (isAlreadySelected) {
                option.classList.add("disabled");
                option.style.pointerEvents = "none"; // Отключаем клики
            }
            option.textContent = item.name;
            option.dataset.id = item.id;
            dropdown.appendChild(option);

            option.addEventListener("click", () => {
                if (!isAlreadySelected) {
                    addSupply(item);
                    dropdown.classList.remove("active");
                    searchInput.value = "";
                }
            });
        });

        dropdown.classList.add("active");
        positionDropdown();
    }

    // Добавление расходного материала
    function addSupply(item) {
        if (selectedSupplies.some((supply) => supply.id === item.id)) {
            showAlert("Этот материал уже добавлен.");
            return; // Прерываем выполнение, если материал уже выбран
        }

        selectedSupplies.push({ id: item.id, name: item.name, quantity: 1 });
        renderSelectedSupplies();
        disableDropdownOption(item.id); // Отключаем выбранный пункт
    }

    // Отключение выбранного пункта в выпадающем списке
    function disableDropdownOption(itemId) {
        const dropdownItems = dropdown.querySelectorAll(".dropdown-item");
        dropdownItems.forEach((option) => {
            if (option.dataset.id === String(itemId)) {
                option.classList.add("disabled"); // Добавляем класс "disabled"
                option.style.pointerEvents = "none"; // Отключаем клики
            }
        });
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

        let highlightedIndex = -1;

        searchInput.addEventListener("keydown", (e) => {
            const items = dropdown.querySelectorAll(".dropdown-item:not(.disabled)");

            if (items.length === 0) return;

            if (e.key === "ArrowDown") {
                e.preventDefault();
                highlightedIndex = (highlightedIndex + 1) % items.length;
                highlightItem(items, highlightedIndex);
            } else if (e.key === "ArrowUp") {
                e.preventDefault();
                highlightedIndex = (highlightedIndex - 1 + items.length) % items.length;
                highlightItem(items, highlightedIndex);
            } else if (e.key === "Enter") {
                e.preventDefault();
                if (highlightedIndex >= 0 && highlightedIndex < items.length) {
                    items[highlightedIndex].click();
                    highlightedIndex = -1;
                }
            }
        });

        function highlightItem(items, index) {
            items.forEach((item, i) => {
                if (i === index) {
                    item.classList.add("active");
                    item.scrollIntoView({
                        block: "nearest", // "nearest" делает мягкую прокрутку внутри контейнера
                        behavior: "smooth"
                    });
                } else {
                    item.classList.remove("active");
                }
            });
        }


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
