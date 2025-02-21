<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Event Planner - Create Event</title>
    <link rel="stylesheet" href="/css/style.css">
    <script src="/js/eventscript.js"></script>
</head>
<body>
<div class="container container-create-event">
    <h2>Create a New Event</h2>

    <!-- Create Event Form -->
    <form id="createEventForm" method="post">
        <div class="form-group">
            <div>
                <label for="name">Event Name:</label>
                <input type="text" id="name" name="name" pattern=".*\S.*" title="Field cannot be blank or contain only spaces" required>
            </div>
            <div>
                <span>Private or public:</span>
                <input type="radio" id="private" name="event-type" value="private" />
                <label for="private">Private</label>

                <input type="radio" id="public" name="event-type" value="public" />
                <label for="public">Public</label>
            </div>
        </div>

        <div class="form-group">
           <div>
                <label for="startTime">Start Time:</label>
                <input type="datetime-local" id="startTime" name="startTime" required>
            </div>
            <div>
                <label for="endTime">End Time:</label>
                <input type="datetime-local" id="endTime" name="endTime" required>
            </div>
        </div>

        <div class="form-group">
            <div>
                <label for="price">Price:</label>
                <input type="number" id="price" name="price" min="0" required>
            </div>
            <div>
                <label for="registrationDeadline">Registration Deadline:</label>
                <input type="datetime-local" id="registrationDeadline" name="registrationDeadline" required>
            </div>
        </div>

        <button type="submit" class="submit-btn">Create Event</button>

        <div class="form-group"></div>
            <h3>Services</h3>
            <div id="servicesContainer">
            <div class="service">
                <label for="service1">Select Service:</label>
                <select id="service1" name="services[0].id">
                    <option value="">None</option>
                    <#if services?has_content>
                        <#list services as service>
                            <option value="${service.id}">${service.name} - ${service.description} - $${service.price}</option>
                        </#list>
                    <#else>
                        <option value="">No services available</option>
                    </#if>
                </select>
            </div>
            </div>
            <button type="button" id="addServiceBtn">Add Another Service</button>
        </div>
        <input type="hidden" id="serviceCount" name="serviceCount" value="1">
    </form>

    <p>
        <ul class="error">
            ${error}
        </ul>
    </p>

    <!-- Back to Events -->
    <p class="back-link"><a href="/events">Back to Events</a></p>

    <script>
        document.addEventListener('DOMContentLoaded', function () {
            const createEventForm = document.getElementById('createEventForm');
            const startTimeInput = document.getElementById('startTime');
            const endTimeInput = document.getElementById('endTime');
            const registrationDeadlineInput = document.getElementById('registrationDeadline');

            startTimeInput.addEventListener('change', function () {
                const startTime = new Date(startTimeInput.value);
                const now = new Date();
                const threeHoursFromNow = new Date(now.getTime() + 3 * 60 * 60 * 1000);

                if (startTime < threeHoursFromNow) {
                    alert('Event cannot start less than 3 hours from now.');
                    startTimeInput.value = '';
                    return;
                }

                const registrationDeadline = new Date(startTime.getTime() - 24 * 60 * 60 * 1000);
                registrationDeadline.setMinutes(registrationDeadline.getMinutes() - registrationDeadline.getTimezoneOffset());
                registrationDeadlineInput.value = registrationDeadline.toISOString().slice(0, 16);
            });

            endTimeInput.addEventListener('change', function () {
                const startTime = new Date(startTimeInput.value);
                const endTime = new Date(endTimeInput.value);

                if (endTime <= startTime) {
                    alert('Event should last at least an hour.');
                    endTimeInput.value = '';
                    return;
                }

                const oneHourAfterStartTime = new Date(startTime.getTime() + 60 * 60 * 1000);
                if (endTime < oneHourAfterStartTime) {
                    alert('Event should last at least an hour.');
                    endTimeInput.value = '';
                }
            });

            createEventForm.addEventListener('submit', function (event) {
                const startTime = new Date(startTimeInput.value);
                const endTime = new Date(endTimeInput.value);
                const registrationDeadline = new Date(registrationDeadlineInput.value);

                if (registrationDeadline >= startTime) {
                    alert('Registration should end before the event starts.');
                    event.preventDefault();
                    return;
                }
            });
        });
    </script>

</div>
</body>
</html>