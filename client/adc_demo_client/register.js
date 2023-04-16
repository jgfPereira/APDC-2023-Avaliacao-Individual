let registerBtn = document.getElementById("registerBtn");

let usernameLabel = document.getElementById("usernameLabel");
let emailLabel = document.getElementById("emailLabel");
let nameLabel = document.getElementById("nameLabel");
let passwordLabel = document.getElementById("passwordLabel");
let passConfLabel = document.getElementById("passConfLabel");

let usernameInput = document.getElementById("username");
let emailInput = document.getElementById("email");
let nameInput = document.getElementById("name");
let passwordInput = document.getElementById("password");
let passConfInput = document.getElementById("passConf");
let visibilitySelect = document.getElementById("visibility");
let homePhoneNumInput = document.getElementById("homePhoneNum");
let phoneNumInput = document.getElementById("phoneNum");
let occupationInput = document.getElementById("occupation");
let placeOfWorkInput = document.getElementById("placeOfWork");
let nifInput = document.getElementById("nif");
let streetInput = document.getElementById("street");
let localeInput = document.getElementById("locale");
let zipCodeInput = document.getElementById("zipCode");
let photoInput = document.getElementById("photo");

registerBtn.addEventListener('click', register);
usernameInput.addEventListener("change", changeLabelToNormalUserName);
emailInput.addEventListener("change", changeLabelToNormalEmail);
nameInput.addEventListener("change", changeLabelToNormalName);
passwordInput.addEventListener("change", changeLabelToNormalPassword);
passConfInput.addEventListener("change", changeLabelToNormalPassConf);

function changeLabelToNormalUserName() {
    usernameLabel.style.color = "black";
}

function changeLabelToNormalEmail() {
    emailLabel.style.color = "black";
}

function changeLabelToNormalName() {
    nameLabel.style.color = "black";
}

function changeLabelToNormalPassword() {
    passwordLabel.style.color = "black";
}

function changeLabelToNormalPassConf() {
    passConfLabel.style.color = "black";
}

function jsonSetEmptyToNull(s) {
    if (s === "") {
        return null;
    } else {
        return s;
    }
}

function serializeVisibilityToStr() {
    let option = visibilitySelect.selectedIndex;
    if (option == -1) {
        return null;
    }
    return visibilitySelect.options[option].text;
}

function serializePhotoToStr(username) {
    if (photoInput.files.length === 0) {
        return null;
    }
    let photoFile = photoInput.files[0];
    let photoFilename = photoFile.name;
    let extension = "." + photoFilename.split('.').pop();
    const newFileName = "photo_" + username + extension;
    return newFileName;
}

function register() {
    let username = usernameInput.value;
    let email = emailInput.value;
    let name = nameInput.value;
    let password = passwordInput.value;
    let passConf = passConfInput.value;
    let visibility = serializeVisibilityToStr();
    let homePhoneNum = homePhoneNumInput.value;
    let phoneNum = phoneNumInput.value;
    let occupation = occupationInput.value;
    let placeOfWork = placeOfWorkInput.value;
    let nif = nifInput.value;
    let street = streetInput.value;
    let locale = localeInput.value;
    let zipcode = zipCodeInput.value;
    let photo = serializePhotoToStr(username); // change photo association to user on server side
    if (username === "" || email === "" || name === "" || password === "" || passConf === "") {
        window.alert("Enter all required fields");
        if (username === "") {
            usernameLabel.style.color = "red";
        }
        if (email === "") {
            emailLabel.style.color = "red";
        }
        if (name === "") {
            nameLabel.style.color = "red";
        }
        if (password === "") {
            passwordLabel.style.color = "red";
        }
        if (passConf === "") {
            passConfLabel.style.color = "red";
        }
        return;
    }
    let request = new XMLHttpRequest();
    request.open("POST", "https://adc-demo-383221.oa.r.appspot.com/rest/register", false);
    request.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
    const body = JSON.stringify({
        username: username,
        email: email,
        name: name,
        password: password,
        passConf: passConf,
        visibility: jsonSetEmptyToNull(visibility),
        homePhoneNum: jsonSetEmptyToNull(homePhoneNum),
        phoneNum: jsonSetEmptyToNull(phoneNum),
        occupation: jsonSetEmptyToNull(occupation),
        placeOfWork: jsonSetEmptyToNull(placeOfWork),
        nif: jsonSetEmptyToNull(nif),
        street: jsonSetEmptyToNull(street),
        locale: jsonSetEmptyToNull(locale),
        zipcode: jsonSetEmptyToNull(zipcode),
        photo: jsonSetEmptyToNull(photo)
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





