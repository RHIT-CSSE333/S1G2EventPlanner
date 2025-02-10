<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Event Planner - Past Events</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <div class="nav">
        <a href="/">Home</a>
        <a href="/events">Events</a>
        <a href="personalinfo">My Account</a>
        <a href="/pastevents">Past Events</a>
        <a href="/venues">Venues</a>
        <a href="hostedevents">My Hosted Events</a>
        <a href="/inbox">Inbox</a>
        <a href="/transactions">Transactions</a>
        <a href="/logout">Log out</a>
    </div>

    <h2>Attended Past Events</h2>
    <div id="events-container">
        <#if events?? && events?size gt 0>
            <table >
                <thead>
                <tr>
                    <th>Name</th>
                     <th>Start Time</th>
                    <th>Venue</th>
                    <th>Address</th>
                </tr>
                </thead>
                <tbody>
                <#list events as event>
                    <tr>
                        <td>${event.name}</td>
                        <td>${event.startTime}</td>
                        <td>${event.venueName}</td>
                        <td>${event.venueAddress}</td>
                    </tr>
                </#list>
                </tbody>
            </table>
        <#else>
            <p>No available events at the moment.</p>
        </#if>

    </div>

</body>