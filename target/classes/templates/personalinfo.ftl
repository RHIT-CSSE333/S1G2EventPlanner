<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Event Planner - Personal Info</title>
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

    <!-- Personal Info -->
    <#if user??>
        <div class="container">
            <p>Full Name: ${user.fullName} <a href="/info/updateName"><button>Change Name</button></a></p>
            <p>Email: ${user.email} <a href="/info/updateEmail"><button>Change Email</button></a></p>
            <p>Phone Number: ${user.phoneNo} <a href="/info/updatePhoneNo"><button>Change Phone Number</button></a></p>
            <p>Date of Birth: ${user.DOB}</p>
        </div>
    <#else>
        <p>${message}</p>
    </#if>
</body>
</html>