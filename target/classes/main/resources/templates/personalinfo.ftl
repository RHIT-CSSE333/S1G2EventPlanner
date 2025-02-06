<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Event Planner - Personal Info</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <h2>Welcome to Event Planner</h2>

    <div class="nav">
        <a href="/">Home</a>
        <#if userSpecific>
            <a href="/events">Events</a>
        <#else>
            <a href="/myevents">My events</a>
        </#if>
        <a href="/venues">Venues</a>
        <a href="hostedevents">My Hosted Events</a>
        <a href="/inbox">Inbox</a>
        <a href="/logout">Log out</a>
    </div>

    <!-- Personal Info -->
    <#if user??>
        <div class="container">
            <p>Full Name: ${user.fullName}</p>
            <p>Email: ${user.email}</p>
            <p>Phone Number: ${user.phoneNo}</p>
            <p>Date of Birth: ${user.DOB}</p>
        </div>
    <#else>
        <p>${message}</p>
    </#if>
</body>
</html>