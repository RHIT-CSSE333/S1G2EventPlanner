<#-- services.ftl -->
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Vendor Services</title>
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
    <h1>Vendor Services</h1>
    <#if services?has_content>
        <table>
            <thead>
                <tr>
                    <th>Name</th>
                    <th>Description</th>
                </tr>
            </thead>
            <tbody>
                <#list services as service>
                    <tr>
                        <td>${service.name}</td>
                        <td>${service.description}</td>
                    </tr>
                </#list>
            </tbody>
        </table>
    <#else>
        <p>No services available for this vendor.</p>
    </#if>
</body>
</html>
