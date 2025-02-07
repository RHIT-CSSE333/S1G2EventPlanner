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
        <a href="/">Home</a>
        <#if userSpecific>
            <a href="/events">Events</a>
        <#else>
            <a href="/myevents">My events</a>
            <a href="personalinfo">My Account</a>
        </#if>
        <a href="/pastevents">Past Events</a>
        <a href="/venues">Venues</a>
        <a href="hostedevents">My Hosted Events</a>
        <a href="/inbox">Inbox</a>
        <a href="/transactions">Transactions</a>
        <a href="/logout">Log out</a>
    </div>

    <h2>Available Public Events</h2>
    <div id="events-container">
        <#if events?? && events?size gt 0>
            <table >
                <thead>
                <tr>
                    <#if userSpecific>
                        <th>Name</th>
                        <th>Start Time</th>
                        <th>Venue</th>
                        <th>Address</th>
                        <th></th>
                        <th></th>
                        <th></th>
                    <#else>
                        <th>Name</th>
                        <th>Start Time</th>
                        <th>End Time</th>
                        <th>Price</th>
                        <th>Venue</th>
                        <th>Address</th>
                        <th>Max Capacity</th>
                        <th>Registration Deadline</th>
                        <th></th>
                    </#if>
                </tr>
                </thead>
                <tbody>
                <#list events as event>
                    <tr>
                        <#if userSpecific>
                            <td>${event.name}</td>
                            <td>${event.startTime}</td>
                            <td>${event.venueName}</td>
                            <td>${event.venueAddress}</td>
                            <td><a href="/event/${event.id}/cancel">Cancel registration</a></td>
                            <td><a href="/event/${event.id}/review">Add Event Review</a></td>
                            <td><a href="/venue/${event.venueId}/review">Add Venue Review</a></td>

                        <#else>
                            <td>${event.name}</td>
                            <td>${event.startTime}</td>
                            <td>${event.endTime}</td>
                            <td>${event.price}</td>
                            <td><a href="/venue/${event.venueId}">${event.venueName}</a></td>
                            <td>${event.venueAddress}</td>
                            <td>${event.maxCapacity}</td>
                            <td>${event.registrationDeadline}</td>
                            <td><a href="/event/${event.id}/register">Register</a></td>
                        </#if>
                    </tr>
                </#list>
                </tbody>
            </table>
        <#else>
            <p>No available events at the moment.</p>
        </#if>

    </div>

</body>