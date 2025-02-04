<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Your Invitations</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<div class="nav">
    <a href="/">Home</a>
    <a href="/events">Events</a>
    <a href="/venues">Venues</a>
    <a href="hostedevents">My Hosted Events</a>
    <a href="/inbox">Inbox</a>
    <a href="/logout">Log out</a>
</div>

    <h2>Your Invitations</h2>

    <#if invitations?? && invitations?size gt 0>
        <table>
            <thead>
            <tr>
                <th>Event Name</th>
                <th>Start Time</th>
                <th>End Time</th>
                <th>Registration Deadline</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <#list invitations as invitation>
                <tr>
                    <td>${invitation.eventName}</td>
                    <td>${invitation.startTime}</td>
                    <td>${invitation.endTime}</td>
                    <td>${invitation.registrationDeadline}</td>
                    <td>
                        <#if invitation.rsvpStatus == 2> <!-- 2 = No Response -->
                            <form action="/privateevent/${invitation.eventId}/rsvp" method="post">
                                <button type="submit" name="rsvpStatus" value="0">Yes</button>
                                <button type="submit" name="rsvpStatus" value="1">No</button>
                            </form>
                        <#elseif invitation.rsvpStatus == 0> <!-- 0 = Registered -->
                            <span class="status registered">Registered</span>
                        <#elseif invitation.rsvpStatus == 1> <!-- 1 = Declined -->
                            <span class="status declined">Declined</span>
                        </#if>
                    </td>
                </tr>
            </#list>
            </tbody>
        </table>
    <#else>
        <p>You have no invitations at the moment.</p>
    </#if>
</body>
</html>
