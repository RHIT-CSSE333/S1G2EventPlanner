<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Event Planner - Personal Info</title>
    <link rel="stylesheet" href="/css/style.css">
    <style>
        .info-container {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 40px;
            max-width: 600px;
            background-color: #f9f9f9;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        }
        .info-row {
            display: contents;
        }
        .info-row p {
            margin: 0;
        }
        .info-row a {
            justify-self: end;
        }
    </style>
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

    <!-- Personal Info -->
    <#if user??>
        <div class="info-container container">
            <div class="info-row">
                <p>Full Name: ${user.fullName}</p>
                <a href="/info/updateName">Change Name</a>
            </div>
            <div class="info-row">
                <p>Email: ${user.email}</p>
                <a href="/info/updateEmail">Change Email</a>
            </div>
            <div class="info-row">
                <p>Phone Number: ${user.phoneNo}</p>
                <a href="/info/updatePhoneNo">Change Phone Number</a>
            </div>
            <div class="info-row">
                <p>Date of Birth: ${user.DOB}</p>
            </div>
        </div>
    <#else>
        <p>${message}</p>
    </#if>
</body>
</html>