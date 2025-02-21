<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Event Planner - Venues</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <div class="nav">
        <a href="/">Home</a>
        <a href="/events">Events</a>
        <a href="personalinfo">My Account</a>
        <a href="/pastevents">My Past Events</a>
        <a href="/venues">Venues</a>
        <a href="hostedevents">My Hosted Events</a>
        <a href="/inbox">Inbox</a>
        <a href="/transactions">Transactions</a>
        <a href="/vendors">Vendors</a>
        <a href="/logout">Log out</a>
    </div>

    <h2>Available Venues</h2>
    <p class="event-hosting-info">
        Looking for the perfect place to host your event? Choose a venue from the list below and start planning!
    </p>
    <div id="venues-container">
        <#if venues?? && venues?size gt 0>
            <table>
                <thead>
                <tr>
                    <th>Venue Name</th>
                    <th>Address</th>
                    <th>Max Capacity</th>
                    <th>Pricing Type</th>
                    <th>Price</th>
                    <th>State</th>
                    <th>City</th>
                    <th>Street Address</th>
                    <th>Zip Code</th>
                    <th>Reviews</th>
                </tr>
                </thead>
                <tbody>
                <#list venues as venue>
                    <tr>
                        <td><a href="/venue/${venue.id}">${venue.name}</a></td>
                        <td>${venue.address}</td>
                        <td>${venue.maxCapacity}</td>
                        <td>
                            <#if venue.pricingType == 0>
                                Hourly
                            <#elseif venue.pricingType == 1>
                                Daily
                            </#if>
                        </td>
                        <td>${venue.price}</td>
                        <td>${venue.state}</td>
                        <td>${venue.city}</td>
                        <td>${venue.streetAddress}</td>
                        <td>${venue.zipCode?c}</td>
                        <td><a href="/venue/${venue.id}/reviews">See Reviews</a></td>
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