let registerBtn = document.getElementById("registerBtn");

let usernameLabel = document.getElementById("usernameLabel");
let emailLabel = document.getElementById("emailLabel");
let nameLabel = document.getElementById("nameLabel");
let passwordLabel = document.getElementById("passwordLabel");
let passConfLabel = document.getElementById("passConfLabel");

let nifLabel = document.getElementById("nifLabel");
let homePhoneNumLabel = document.getElementById("homePhoneNumLabel");
let phoneNumLabel = document.getElementById("phoneNumLabel");
let zipCodeLabel = document.getElementById("zipCodeLabel");

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
nifInput.addEventListener("change", changeLabelToNormalNIF);

let shouldReturn = false;


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

function changeLabelToNormalNIF() {
    nifLabel.style.color = "black";
}

function jsonSetEmptyToNull(s) {
    if (s === "") {
        return null;
    } else {
        return s;
    }
}

function serializeVisibilityToStr() {
    let option = visibilitySelect.options[visibilitySelect.selectedIndex].value;
    return option === "" ? null : option;
}

function serializePhotoToStr(username) {
    if (photoInput.files.length === 0 || username === "") {
        return null;
    }
    let photoFile = photoInput.files[0];
    let photoFilename = photoFile.name;
    let extension = "." + photoFilename.split('.').pop();
    const newFileName = "photo_" + username + extension;
    return [photoFile, newFileName];
}

function verifyField(fieldLabel, filter, errorMsg) {
    if (filter) {
        window.alert(errorMsg)
        fieldLabel.style.color = "red";
        shouldReturn = true;
    }
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
    let serializePhoto = serializePhotoToStr(username);
    let photoFile = null;
    let photo = null;
    if (serializePhoto != null) {
        photoFile = serializePhoto[0];
        photo = serializePhoto[1];
    }
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
        shouldReturn = true;
    }
    verifyField(nifLabel, !new RegExp("^[0-9]*$").test(nif), "Only digits allowed on NIF");
    verifyField(homePhoneNumLabel, !new RegExp("^[0-9]*$").test(homePhoneNum), "Only digits allowed on home phone number");
    verifyField(phoneNumLabel, !new RegExp("^[0-9]*$").test(phoneNum), "Only digits allowed on phone number");
    verifyField(zipCodeLabel, !new RegExp("^[0-9]{4}-[0-9]{3}$").test(zipcode) && zipcode !== "", "Zip code format is XXXX-XXX with digits");
    if (shouldReturn) {
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
        zipCode: jsonSetEmptyToNull(zipcode),
        photo: jsonSetEmptyToNull(photo)
    });
    request.onload = () => {
        const respText = JSON.parse(request.responseText);
        if (request.readyState == 4 && request.status == 200) {
            console.log(respText);
            uploadFileGCS(photoFile, photo);
        } else {
            window.alert(respText);
        }
    };
    request.send(body);
}

function uploadFileGCS(file, fileName) {
    let bucket = "adc-demo-383221.appspot.com";
    let request = new XMLHttpRequest();
    request.open("POST", "/gcs/" + bucket + "/" + fileName, false);
    request.setRequestHeader("Content-Type", file.type);
    request.send(file);
}





