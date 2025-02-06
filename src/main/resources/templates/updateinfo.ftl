<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Update Personal Info</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <h1>Update Personal Info</h1>
    <form id="review" method="post">
    <div class = "container">
        <#if type = "name">
        <label for="firstName">Update First Name:</label>
        <input type="text" id="firstName" name="firstName" required><br><br>
        <label for="Minit">Update Middle Initial:</label>
        <input type="text" minlength="1" maxlength="1" id="Minit" name="Minit"><br><br>
        <label for="lastName">Update Last Name:</label>
        <input type="text" id="firstName" name="firstName" required><br><br>
        </#if>

        <button type="submit">Submit Review</button>
    </div>
    </form>
    <a href="/personalinfo">Return</a>
</body>
</html>