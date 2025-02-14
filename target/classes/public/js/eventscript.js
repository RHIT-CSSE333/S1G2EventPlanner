document.addEventListener('DOMContentLoaded', function() {
    let serviceCount = 1; // Initialize service count
    const addServiceButton = document.getElementById('addServiceBtn');
    const servicesContainer = document.getElementById('servicesContainer');
    const serviceCountInput = document.getElementById('serviceCount');

    addServiceButton.addEventListener('click', function() {
        serviceCount++; // Increment service count
        serviceCountInput.value = serviceCount; // Update hidden input with service count

        // Create a new service container
        const serviceContainer = document.createElement('div');
        serviceContainer.classList.add('service');

        // Set the inner HTML of the service container
        serviceContainer.innerHTML = `
            <label for="service${serviceCount}">Select Service:</label>
            <select id="service${serviceCount}" name="services[${serviceCount - 1}].id" required>
                ${document.querySelector('#service1').innerHTML}
            </select>
        `;

        // Append the new service container to the services container
        servicesContainer.appendChild(serviceContainer);
    });
});