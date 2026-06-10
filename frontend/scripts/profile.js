const username = document.querySelector("#profile_username");
const email = document.querySelector("#profile_email");

const backendUrl = "http://localhost:8080";
const userUrl = `${backendUrl}/user`;
const authUrl = `${backendUrl}/auth`;

async function getUserDetails() {
  fetch(`${userUrl}/me`, {
    method: "GET",
    credentials: "include",
  })
  .then(response => response.json())
  .then(json => {
    console.log(json);
    mainBlockContentType(!!localStorage.getItem("isLogged"));
    username.innerHTML = json.username;
    email.innerHTML = json.email;
  })
}

async function logout() {
  fetch(`${authUrl}/logout`, {
    method: "POST",
  })
  .then(json => {
    console.log(json);
    if (json.status == 200) {
      localStorage.clear();
      mainBlockContentType(false);
    }
  });
}

document.addEventListener("DOMContentLoaded", async () => {
  if (!!localStorage.getItem("isLogged")) await getUserDetails();
});

function mainBlockContentType(isLogged) {
  document.getElementById("logged").style.display = isLogged ? "block" : "none";
  document.getElementById("not-logged").style.display = isLogged ? "none" : "block";
}

document.getElementById("logout").addEventListener("click", async () => await logout());