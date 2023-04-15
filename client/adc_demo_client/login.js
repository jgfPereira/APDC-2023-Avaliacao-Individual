let loginBtn = document.getElementById("loginBtn");
loginBtn.addEventListener("click", login);

function login() {
    let username = document.getElementById("username").value;
    let password = document.getElementById("password").value;
    if (username == "" || password == "") {
        window.alert("Enter all required fields");
        return;
    }
    console.log(username);
    console.log(password);
    let request = new XMLHttpRequest();
    request.open("POST", "https://adc-demo-383221.oa.r.appspot.com/rest/login", false);
    request.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
    const body = JSON.stringify({
        username: username,
        password: password
    });
    request.onload = () => {
        if (request.readyState == 4 && request.status == 200) {
            console.log(JSON.parse(request.responseText));
        } else if(request.status == 401){
            console.log(JSON.parse(request.responseText));
        } else {
            console.log(`Error: ${request.status}`);
        }
    };
    request.send(body);
}
