<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Event Planner - Vendors</title>
    <link rel="stylesheet" href="css/style.css">
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

    <h2>Vendors</h2>
    <div id="vendors-container">
        <#if vendors?? && vendors?size gt 0>
            <table >
                <thead>
                <tr>
                    <th>Name</th>
                    <th>Offered Services</th>
                </tr>
                </thead>
                <tbody>
                <#list vendors as vendor>
                    <tr>
                        <td>${vendor.name}</td>
                        <td><a href="/vendors/${vendor.ID}/services">Search Offered Services from ${vendor.name}</a></td>
                </#list>
                </tbody>
            </table>
        <#else>
            <p>No available vendors at the moment.</p>
        </#if>

    </div>

</body>