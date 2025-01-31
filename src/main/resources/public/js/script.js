// document.addEventListener("DOMContentLoaded", function () {
//     const registerForm = document.getElementById("registerForm");

//     if (registerForm) {
//         registerForm.addEventListener("submit", function (event) {
//             event.preventDefault(); // Prevent form submission

//             let password = document.getElementById("password").value;
//             let confirmPassword = document.getElementById("confirmPassword").value;

//             if (password !== confirmPassword) {
//                 alert("Passwords do not match. Please enter again.");
//                 return;
//             }

//             registerForm.submit();
//             window.location.href = "login.html"; // Redirect to login page
//         });
//     }
// });

// // document.addEventListener("DOMContentLoaded", function () {
// //     const loginForm = document.getElementById("loginForm");

// //     loginForm.addEventListener("submit", function (event) {
// //         event.preventDefault();

// //         const email = document.getElementById("email").value;
// //         const password = document.getElementById("password").value;

// //         // TODO: validation check
// //         if (email && password) {
// //             window.location.href = "index.ftl"; // Redirect to index.ftl
// //         } else {
// //             alert("Please fill out both fields.");
// //         }
// //     });
// // });