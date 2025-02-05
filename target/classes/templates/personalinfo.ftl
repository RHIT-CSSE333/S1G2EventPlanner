<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Event Planner - Personal Info</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<div class="container container-login">
    <h2>Welcome to Event Planner</h2>

    <!-- Personal Info -->
    <#if user??>
        <div>
            <p>${user.FirstName}</p>
            <p>${user.Email}</p>
        </div>
    <#else>
        <p>${message}</p>
    </#if>
    
</div>
</body>
</html>