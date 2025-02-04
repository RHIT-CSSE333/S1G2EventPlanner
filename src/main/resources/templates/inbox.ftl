<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Your Invitations</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<div class="nav">
    <a href="/">Home</a>
    <a href="/events">Events</a>
    <a href="/venues">Venues</a>
    <a href="hostedevents">My Hosted Events</a>
    <a href="/inbox">Inbox</a>
    <a href="/logout">Log out</a>
</div>

    <h2>Your Invitations</h2>

    <#if invitations?? && invitations?size gt 0>
        <table>
            <thead>
            <tr>
                <th>Event Name</th>
                <th>Start Time</th>
                <th>End Time</th>
                <th>Registration Deadline</th>
            </tr>
            </thead>
            <tbody>
            <#list invitations as invitation>
                <tr>
                    <td>${invitation.eventName}</td>
                    <td>${invitation.startTime}</td>
                    <td>${invitation.endTime}</td>
                    <td>${invitation.registrationDeadline}</td>
                </tr>
            </#list>
            </tbody>
        </table>
    <#else>
        <p>You have no invitations at the moment.</p>
    </#if>
</body>
</html>
