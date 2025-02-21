<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Event Planner - Services</title>
    <link rel="stylesheet" href="/css/style.css">
</head>
<body>
    <div class="nav">
        <a href="/">Home</a>
        <a href="/events">Events</a>
        <a href="/personalinfo">My Account</a>
        <a href="/pastevents">My Past Events</a>
        <a href="/venues">Venues</a>
        <a href="/hostedevents">My Hosted Events</a>
        <a href="/inbox">Inbox</a>
        <a href="/transactions">Transactions</a>
        <a href="/vendors">Vendors</a>
        <a href="/logout">Log out</a>
    </div>

    <h2>Services</h2>
    <div id="services-container">
        <#if services?? && services?size gt 0>
            <table>
                <thead>
                <tr>
                    <th>Service Name</th>
                    <th>Description</th>
                    <th>Price</th>
                    <th>Vendor Name</th>
                </tr>
                </thead>
                <tbody>
                <#list services as service>
                    <tr>
                        <td>${service.name}</td>
                        <td>${service.description}</td>
                        <td>${service.price}</td>
                        <td>${service.vendorName}</td>
                    </tr>
                </#list>
                </tbody>
            </table>
        <#else>
            <p>This event is not using any services</p>
        </#if>
    </div>
</body>
</html>
