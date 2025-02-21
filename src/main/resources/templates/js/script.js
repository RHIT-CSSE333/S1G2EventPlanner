document.addEventListener("DOMContentLoaded", function () {
    document.getElementById("dateInput").max = new Date().toISOString().split("T")[0];
    
    const registerForm = document.getElementById("registerForm");

    if (registerForm) {
        registerForm.addEventListener("submit", function (event) {
            event.preventDefault(); // Prevent form submission

            let password = document.getElementById("password").value;
            let confirmPassword = document.getElementById("confirmPassword").value;

            if (password !== confirmPassword) {
                alert("Passwords do not match. Please enter again.");
                return;
            }

            registerForm.submit();
            window.location.href = "login.html"; // Redirect to login page
        });
    }
});

