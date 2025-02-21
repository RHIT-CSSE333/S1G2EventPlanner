<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Event Planner - Hosted Events</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<div class="nav">
    <a href="/">Home</a>
    <a href="/events">Events</a>
    <a href="/personalinfo">My Account</a>
    <a href="/pastevents">My Past Events</a>
    <a href="/venues">Venues</a>
    <a href="hostedevents">My Hosted Events</a>
    <a href="/inbox">Inbox</a>
    <a href="/transactions">Transactions</a>
    <a href="/vendors">Vendors</a>
    <a href="/logout">Log out</a>
</div>

<h2>Hosted Events</h2>
<div id="events-container">
    <#if events?? && events?size gt 0>
        <table>
            <thead>
            <tr>
                <th>Event Name</th>
                <th>Start Time</th>
                <th>End Time</th>
                <th>Venue Name</th>
                <th>Max Capacity</th>
                <th>Registration deadline</th>
                <th>Event Type</th>
                <th>Payment Status</th>
                <th>Actions</th>
                <th>Invitees RSVP Status</th>
                <th>Check In QR-Code</th>
            </tr>
            </thead>
            <tbody>
            <#list events as event>
                <tr>
                    <td><a href="/event/${event.id}/services">${event.name}</a></td>
                    <td>${event.startTime}</td>
                    <td>${event.endTime}</td>
                    <td>${event.venueName}</td>
                    <td>${event.maxCapacity}</td>
                    <td>${event.registrationDeadline}</td>
                    <td>
                        <#if event.isPublic>
                            Public
                        <#else>
                            Private
                        </#if>
                    </td>

                    <td>
                        <#if event.paymentStatus>
                            Paid
                        <#else>
                            Unpaid. <a href="/pay/host/${event.id}">Pay</a>
                        </#if>
                    </td>

                    <td>
                        <#if !event.isPublic>
                            <a href="/event/${event.id}/invite" class="invite-btn">Invite</a>
                        </#if>
                    </td>

                    <td>
                        <#if !event.isPublic>
                            <a href="/event/${event.id}/invitees-rsvp-status" class="rsvp-status-btn">View RSVP Status</a>
                        <#else>
                            N/A
                        </#if>
                    </td>

                    <td>
                        <a href="/qr/${event.id}">Check In</a>
                    </td>
                </tr>
            </#list>
            </tbody>
        </table>
    <#else>
        <p>${message}</p>
    </#if>
</div>
</body>
</html>