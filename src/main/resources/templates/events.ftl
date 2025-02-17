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
        </#if>
        <a href="personalinfo">My Account</a>
        <a href="/pastevents">My Past Events</a>
        <a href="/venues">Venues</a>
        <a href="hostedevents">My Hosted Events</a>
        <a href="/inbox">Inbox</a>
        <a href="/transactions">Transactions</a>
        <a href="/vendors">Vendors</a>
        <a href="/logout">Log out</a>
    </div>

    <p class="event-hosting-info">
        Want to host an event? Browse our venue list and be the host of your next great event!
    </p>
    <p><a href="/venues" class="create-event-button">Browse Venues</a></p>

    <#if userSpecific>
        <h2>Future Events</h2>
    <#else>
        <h2>Future Public Events</h2>
    </#if>
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
                        <th>Type</th>
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
                        <th>Remaining Seats</th>
                        <th>Registration Deadline</th>
                        <th>Register</th>
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
                            <td><#if event.isPublic>Public<#else>Private</#if></td>
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
                            <td>
                                <#if event.remainingSeats gt 0>
                                    ${event.remainingSeats}
                                <#else>
                                    <span style="color: red;">Full</span>
                                </#if>
                            </td>
                            <td>${event.registrationDeadline}</td>
                            <td>
                                <#if event.remainingSeats gt 0>
                                    <a href="/event/${event.id}/register">Register</a>
                                <#else>
                                    <span style="color: gray;">Not Available</span>
                                </#if>
                            </td>
                        </#if>
                    </tr>
                </#list>
                </tbody>
            </table>
        <#else>
            <p>No available events at the moment.</p>
        </#if>

    </div>

    <#if !userSpecific>
        <h2>Past Public Events</h2>
        <div id="events-container">
            <#if pastEvents?? && pastEvents?size gt 0>
                <table>
                    <thead>
                    <tr>
                        <th>Name</th>
                        <th>Start Time</th>
                        <th>End Time</th>
                        <th>Venue</th>
                        <th>See Reviews</th>
                    </tr>
                    </thead>
                    <tbody>
                    <#list pastEvents as pastEvent>
                        <tr>
                            <td>${pastEvent.name}</td>
                            <td>${pastEvent.startTime}</td>
                            <td>${pastEvent.endTime}</td>
                            <td><a href="/venue/${pastEvent.venueId}">${pastEvent.venueName}</a></td>
                            <td><a href="/event/${pastEvent.id}/reviews">See Reviews</a></td>
                        </tr>
                    </#list>
                    </tbody>
                </table>
            <#else>
                <p>No past events available.</p>
            </#if>
        </div>
    </#if>


</body>