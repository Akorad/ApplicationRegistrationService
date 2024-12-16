document.getElementById('guestTicketForm').addEventListener('submit', async function(event) {
    event.preventDefault();

    const inventoryNumber = document.getElementById('inventoryNumber').value;
    const guestDepartment = document.getElementById('guestDepartment').value;
    const guestPhoneNumber = document.getElementById('guestPhoneNumber').value;
    const descriptionOfTheProblem = document.getElementById('descriptionOfTheProblem').value;

    const requestData = {
        descriptionOfTheProblem: descriptionOfTheProblem,
        guestPhoneNumber: guestPhoneNumber,
        guestDepartment: guestDepartment,
        equipment: {
            inventoryNumber: inventoryNumber
        }
    };

    try {
        const response = await fetch(`${window.config.apiUrl}/api/guest/create`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify(requestData)
        });

        if (response.status === 403) {
            showAlert('Ошибка 403: Доступ запрещен. Возможно, у вас недостаточно прав для выполнения этого действия.');
        } else if (!response.ok) {
            const errorData = await response.json();
            showAlert(`Ошибка при создании заявки: ${errorData.error || 'Неизвестная ошибка'}`);
        } else {
            alert('Заявка успешно создана');
            document.getElementById('guestTicketForm').reset();
        }
    } catch (error) {
        showAlert(`Произошла ошибка при отправке: ${error.message}`);
    }

});