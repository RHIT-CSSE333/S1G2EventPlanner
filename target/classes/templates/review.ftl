<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Leave a Review</title>
    <link rel="stylesheet" href="/css/style.css">
</head>
<body>
    <h1>Leave a Review</h1>
    <form id="review" method="post">
    <div class = "container">
        <label for="title">Title:</label>
        <input type="text" id="title" name="title"><br><br>

        <label for="rating">Rating:</label>
        <input type="number" id="rating" name="rating" min="1" max="5" required><br><br>

        <label for="description">Description:</label>
        <textarea id="description" name="description"></textarea><br><br>

        <input type="hidden" id="postedOn" name="postedOn" value="${.now?string("yyyy-MM-dd HH:mm:ss")}">

        <button type="submit">Submit Review</button><br><br>
        <a href="/">Return</a>
    </div>
    </form>
</body>
</html>