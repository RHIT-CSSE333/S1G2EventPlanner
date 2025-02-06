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
        <input type="text" id="lastName" name="lastName" required><br><br>
        </#if>
        <#if type = "email">
        <label for="email">Update Email:</label>
        <input type="email" id="email" name="email" required><br><br>
        </#if>
        <#if type = "phone">
        <label for="phoneNo">Update Phone Number (10 Digits):</label>
        <input type="tel" id="phoneNo" name="phoneNo" pattern="\d{10}" required><br><br>
        </#if>

        <button type="submit">Submit Update</button>
    </div>
    </form>
    <a href="/personalinfo">Return</a>
</body>
</html>