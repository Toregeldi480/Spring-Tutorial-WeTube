const loginUsername = document.querySelector("#login_username");
const loginPassword = document.querySelector("#login_password");
const loginButton = document.querySelector("#login_button");

const signupUsername = document.querySelector("#signup_username");
const signupEmail = document.querySelector("#signup_email");
const signupPassword = document.querySelector("#signup_password");
const signupButton = document.querySelector("#signup_button");

const loginErrorMessage = document.querySelector("#login_error-message");
const signupErrorMessage = document.querySelector("#signup_error-message");

const backendUrl = "http://localhost:8080";
const authUrl = `${backendUrl}/auth`;
const userUrl = `${backendUrl}/user`;

loginButton.addEventListener("click", async function sendLoginData() {
  fetch(`${authUrl}/login`, {
    method: "POST",
    credentials: "include",
    body: JSON.stringify({
      username: `${loginUsername.value}`,
      password: `${loginPassword.value}` 
    }),
    headers: {
      "Content-Type": "application/json;",
    }
  })
  .then(response => {
    if (response.ok) {
      let json = response.json()
      console.log(json);
      localStorage.setItem("isLogged", "true");
      window.location = "./profile.html";
    }
  })
});

signupButton.addEventListener("click", async function sendSignupData() {
  fetch(`${authUrl}/register`, {
    method: "POST",
    body: JSON.stringify({
      username: `${signupUsername.value}`,
      email: `${signupEmail.value}`,
      password: `${signupPassword.value}`,
    }),
    headers: {
      "Content-Type": "application/json;",
    }
  })
  .then(response => {
    console.log(response)
  })
});

signupUsername.onfocus = function() {
  signupErrorMessage.innerHTML = "";
}
signupPassword.onfocus = function() {
  signupErrorMessage.innerHTML = "";
}

loginUsername.onfocus = function() {
  loginErrorMessage.innerHTML = "";
}
loginPassword.onfocus = function() {
  loginErrorMessage.innerHTML = "";
}

const signupToLogin = document.querySelector("#signup_to_login");
const loginToSignup = document.querySelector("#login_to_signup");

const loginBlock = document.querySelector("#login_block");
const signupBlock = document.querySelector("#signup_block");

const block = document.querySelector(".auth");

signupToLogin.addEventListener("click", function() {
  signupBlock.style.display = "none";

  block.animate(
     [
        { transform: 'rotateY(0deg)' },
        { transform: 'rotateY(360deg)' }
    ],
    {
        duration: 400,
        iterations: 1 
    }
  )

  loginBlock.style.display = "flex";
})

loginToSignup.addEventListener("click", function() {
  loginBlock.style.display = "none";

  block.animate(
     [
        { transform: 'rotateY(0deg)' },
        { transform: 'rotateY(360deg)' }
    ],
    {
        duration: 400,
        iterations: 1
    }
  );
  
  signupBlock.style.display = "flex";
})