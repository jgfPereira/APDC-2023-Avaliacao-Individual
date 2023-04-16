let loginBtn = document.getElementById("loginBtn");

let usernameLabel = document.getElementById("usernameLabel");
let passwordLabel = document.getElementById("passwordLabel");

let usernameInput = document.getElementById("username");
let passwordInput = document.getElementById("password");

loginBtn.addEventListener("click", login);
usernameInput.addEventListener("change", changeLabelToNormalUserName);
passwordInput.addEventListener("change", changeLabelToNormalPassword);

function changeLabelToNormalUserName() {
    usernameLabel.style.color = "black";
}

function changeLabelToNormalPassword() {
    passwordLabel.style.color = "black";
}

function login() {
    let username = usernameInput.value;
    let password = passwordInput.value;
    if (username === "" || password === "") {
        window.alert("Enter all required fields");
        if (username === "") {
            usernameLabel.style.color = "red";
        }
        if (password === "") {
            passwordLabel.style.color = "red";
        }
        return;
    }
    let request = new XMLHttpRequest();
    request.open("POST", "https://adc-demo-383221.oa.r.appspot.com/rest/login", false);
    request.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
    const body = JSON.stringify({
        username: username,
        password: password
    });
    request.onload = () => {
        const respText = JSON.parse(request.responseText);
        if (request.readyState == 4 && request.status == 200) {
            console.log(respText);
        } else {
            window.alert(respText);
        }
    };
    request.send(body);
}
