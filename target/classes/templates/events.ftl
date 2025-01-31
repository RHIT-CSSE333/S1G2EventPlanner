<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Event Planner - Events</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <div class="nav">
        <a href="index.ftl">Home</a>
        <a href="login.ftl">Login</a>
    </div>

    <h2>Available Public Events</h2>
    <div id="events-container">
        <#if events?? && events?size gt 0>
            <table >
                <thead>
                <tr>
                    <th>Name</th>
                    <th>Start Time</th>
                    <th>End Time</th>
                    <th>Price</th>
                    <th>Venue</th>
                    <th>Address</th>
                    <th>Max Capacity</th>
                    <th>Registration Deadline</th>
                </tr>
                </thead>
                <tbody>
                <#list events as event>
                    <tr>
                        <td>${event.name}</td>
                        <td>${event.startTime}</td>
                        <td>${event.endTime}</td>
                        <td>$${event.price}</td>
                        <td>${event.venueName}</td>
                        <td>${event.venueAddress}</td>
                        <td>${event.maxCapacity}</td>
                        <td>${event.registrationDeadline}</td>
                    </tr>
                </#list>
                </tbody>
            </table>
        <#else>
            <p>No available events at the moment.</p>
        </#if>

    </div>
</body>