<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Your Transactions</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<div class="nav">
    <a href="/">Home</a>
    <a href="/events">Events</a>
    <a href="personalinfo">My Account</a>
    <a href="/pastevents">Past Events</a>
    <a href="/venues">Venues</a>
    <a href="hostedevents">My Hosted Events</a>
    <a href="/inbox">Inbox</a>
    <a href="/transactions">Transactions</a>
    <a href="/logout">Log out</a>
</div>

    <h2>Your Transactions</h2>

    <#if transactions?? && transactions?size gt 0>
        <table>
            <thead>
            <tr>
                <th>Type</th>
                <th>Amount</th>
                <th>Paid On</th>
            </tr>
            </thead>
            <tbody>
            <#list transactions as transaction>
                <tr>
                    <td>
                        <#if transaction.type == 0>
                            <span class="status registered">Hosted</span>
                        <#else>
                            <span class="status registered">Attended</span>
                        </#if>
                    </td>
                    <td>${transaction.amount}</td>
                    <td>${transaction.paidOn}</td>
                </tr>
            </#list>
            </tbody>
        </table>
    <#else>
        <p>You have not made any transaction yet.</p>
    </#if>
</body>
</html>
