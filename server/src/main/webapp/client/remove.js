let removeBtn = document.getElementById("removeBtn");

let removerLabel = document.getElementById("removerLabel");
let removedLabel = document.getElementById("removedLabel");
let tokenLabel = document.getElementById("tokenLabel");

let removerInput = document.getElementById("remover");
let removedInput = document.getElementById("removed");
let tokenInput = document.getElementById("token");

removeBtn.addEventListener("click", remove);
removerInput.addEventListener("change", changeLabelToNormalRemover);
removedInput.addEventListener("change", changeLabelToNormalRemoved);
tokenInput.addEventListener("change", changeLabelToNormalToken);

function changeLabelToNormalRemover() {
    removerLabel.style.color = "black";
}

function changeLabelToNormalRemoved() {
    removedLabel.style.color = "black";
}

function changeLabelToNormalToken() {
    tokenLabel.style.color = "black";
}

function remove() {
    let remover = removerInput.value;
    let removed = removedInput.value;
    let token = tokenInput.value;
    if (remover === "" || removed === "" || token === "") {
        window.alert("Enter all required fields");
        if (remover === "") {
            removerLabel.style.color = "red";
        }
        if (removed === "") {
            removedLabel.style.color = "red";
        }
        if (token === "") {
            tokenLabel.style.color = "red";
        }
        return;
    }
    let request = new XMLHttpRequest();
    request.open("POST", "https://adc-demo-383221.oa.r.appspot.com/rest/remove", false);
    request.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
    request.setRequestHeader("Authorization", "Bearer " + token);
    const body = JSON.stringify({
        removerUsername: remover,
        removedUsername: removed
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