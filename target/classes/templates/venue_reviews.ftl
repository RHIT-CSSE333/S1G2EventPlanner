<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Venue Reviews</title>
    <link rel="stylesheet" href="/css/style.css">
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
    <a href="/logout">Log out</a>
</div>

<div class="container">
    <h2>Reviews for Venue:
        <span>
            <#if reviews?? && reviews?size gt 0>
                ${reviews[0].venueName}
            </#if>
        </span>
    </h2>

    <#if reviews?? && reviews?size gt 0>
        <table>
            <thead>
            <tr>
                <th>Title</th>
                <th>Rating</th>
                <th>Comment</th>
                <th>Posted On</th>
                <th>Reviewer</th>
            </tr>
            </thead>
            <tbody>
            <#list reviews as review>
                <tr>
                    <td>${review.title}</td>
                    <td>${review.rating} / 5</td>
                    <td>${review.comment}</td>
                    <td>${review.postedOn}</td>
                    <td>${review.reviewerName}</td>
                </tr>
            </#list>
            </tbody>
        </table>
    <#else>
        <p>No reviews available for this venue.</p>
    </#if>
</div>

</body>
</html>
