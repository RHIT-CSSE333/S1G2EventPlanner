<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Event Planner - Login</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<div class="container container-login">
    <h2>Welcome to Event Planner</h2>

    <!-- Login Form -->
    <form id="loginForm" action="/login" method="post">
        <label for="email">Email:</label>
        <input type="email" id="email" name="email" required>

        <label for="password">Password:</label>
        <input type="password" id="password" name="password" required>

        <button type="submit" class="submit-btn">Login</button>

        <p class="error">${error}</p>
    </form>

    <!-- Register Link -->
    <p class="register-link">Don't have an account? <a href="register.html">Register here.</a></p>
</div>
<script src="js/script.js"></script>
</body>
</html>
