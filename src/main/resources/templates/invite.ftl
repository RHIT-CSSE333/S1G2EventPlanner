<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Event Planner - Invite</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <#if error != "">
        <div class="error-message">
            ${error}
        </div>
    <#else>
        <h3>Event Name: ${event.name}</h3>
        <p>Start Time: ${event.startTime}</p>
        <p>Registration Deadline: ${event.registrationDeadline}</p>
        <p>Remaining Spots: <strong>${remainingSeats}</strong></p>

        <form action="/event/${event.id}/invite" method="post">
            <label>Enter the email of the user you want to Invite:</label>
            <input type="email" name="email" required>
            <button type="submit" <#if remainingSeats == 0>disabled</#if>>Send Invite</button>
        </form>

        <#if remainingSeats == 0>
            <p style="color: red;">This event is full. No more invitations can be sent.</p>
        </#if>
    </#if>
    <a href="/hostedevents">Back to Hosted Events</a>
</body>

