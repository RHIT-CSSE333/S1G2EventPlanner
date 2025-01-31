<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Event Planner - Register</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<div class="container container-register">
    <h2>Create an Account</h2>

    <!-- Register Form -->
    <form id="registerForm" method="post">
        <div class="form-group">
            <div>
                <label for="email">Email:</label>
                <input type="email" id="email" name="email" class="full-width" required>
            </div>
            <div>
                <label for="phoneNo">Phone Number:</label>
                <input type="tel" id="phoneNo" name="phone" required>
            </div>
        </div>

        <div class="form-group">
            <div>
                <label for="firstName">First Name:</label>
                <input type="text" id="firstName" name="firstName" pattern=".*\S.*" title="Field cannot be blank or contain only spaces" required>
            </div>
            <div>
                <label for="middleInit">Middle Initial (Optional):</label>
                <input type="text" id="middleInit" name="middleInit" maxlength="1">
            </div>
            <div>
                <label for="lastName">Last Name:</label>
                <input type="text" id="lastName" name="lastName" pattern=".*\S.*" title="Field cannot be blank or contain only spaces" required>
            </div>
        </div>

        <div class="form-group">
            <div>
                <label for="dob">Date of Birth:</label>
                <input type="date" id="dob" name="dob" required>
            </div>
        </div>

        <label for="password">Password:</label>
        <input type="password" id="password" name="password" class="full-width" required>

        <label for="confirmPassword">Confirm Password:</label>
        <input type="password" id="confirmPassword" name="confirmPassword" class="full-width" oninput="this.setCustomValidity(this.value !== password.value ? 'Passwords must match' : '')" required>

        <button type="submit" class="submit-btn">Register</button>
    </form>

    <p>
        <ul class="error">
            ${error}
        </ul>
    </p>

    <!-- Back to Login -->
    <p class="register-link">Already have an account? <a href="/login">Login here.</a></p>
</div>
<script src="js/script.js"></script>
</body>
</html>
