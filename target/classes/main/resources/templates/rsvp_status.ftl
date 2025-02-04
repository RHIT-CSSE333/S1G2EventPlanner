<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Event Planner - Invitees RSVP status</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <h2>RSVP Status for Event: ${event.name}</h2>

    <#if invitees?? && invitees?size gt 0>
        <table>
            <thead>
            <tr>
                <th>Invitee Name</th>
                <th>Email</th>
                <th>RSVP Status</th>
            </tr>
            </thead>
            <tbody>
            <#list invitees as invitee>
                <tr>
                    <td>${invitee.firstName} ${invitee.lastName}</td>
                    <td>${invitee.email}</td>
                    <td>
                        <#if invitee.rsvpStatus == 0>
                            Registered
                        <#elseif invitee.rsvpStatus == 1>
                            Refused
                        <#elseif invitee.rsvpStatus == 2>
                            No response
                        </#if>
                    </td>
                </tr>
            </#list>
            </tbody>
        </table>
    <#else>
        <p>No invitees found for this event.</p>
    </#if>

    <a href="/hostedevents">Back to Hosted Events</a>
</body>
</html>