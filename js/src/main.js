"use strict";

console.log("hello world");

const name = () => document.getElementById("name").value
const height = () => document.getElementById("height").value
const age = () => document.getElementById("age").value
const place = () => document.getElementById("place").value
const hobby = () => document.getElementById("hobby").value
const isMen = () => document.getElementById("isMen").checked

const putAge = (age, pref = "l") => {document.getElementById(pref + "Age").innerHTML = age }
const putHeight = (height, pref = "l") => {document.getElementById(pref + "Height").innerHTML = height }
const putPlace = (place, pref = "l") => {document.getElementById(pref + "Place").innerHTML = place }
const putHobby = (hobby, pref = "l") => {document.getElementById(pref + "Hobby").innerHTML = hobby }
const putName = (name) => {document.getElementById("mName").innerHTML = name }

const showQuestions = () => {document.getElementById("questionC").style.visibility = null}
const showMatchText = () => {document.getElementById("matchText").style.visibility = null}
const showMatch = () => {document.getElementById("match").style.visibility = null}

const nameValid = () => {
    if (name().length === 0) {
        alert("Name field must be nonEmpty");
        return false
    } else return true
}
const heightValid = () => {
    if (!(height() >= 140 && height() <= 200)) {
        alert("Height must be (140, 200)")
        return false
    } else return true
}
const ageValid = () => {
    if (!(age() >= 18 && age() <= 50)) {
        alert("Age must be [18, 50]")
        return false
    } else return true
}
const modelValid = () => {
    return nameValid() && heightValid() && ageValid();
}

const host = "http://localhost:9001"
const request = async (data, path) => {
    const response = await fetch(`${host}/${path}`, {
        method: 'POST',
        body: JSON.stringify(data),
        headers: {
            'Content-Type': 'application/json; charset=utf-8',
            'Accept-Encoding': '*'
        }
    })
    return await response.json();
}

const session = () => {
    if (!modelValid()) return false
    return {
        name: name(),
        isMen: isMen(),
        model: {place: place(), hobby: hobby(), age: age(), height: height()}
    }
}

async function register() {
    if (!modelValid()) return false
    const response = await request(session(), "register")
    console.log("submit", name(), height(), age(), place(), hobby());
    console.log(response);
    return response;
}

const displayQuestion = ({"res": [[l, lModel], [r, rModel]]}) => {
    putAge(lModel.age.value, "l")
    putHeight(lModel.height.value, "l")
    putHobby(lModel.hobby, "l")
    putPlace(lModel.place, "l")
    putAge(rModel.age.value, "r")
    putHeight(rModel.height.value, "r")
    putHobby(rModel.hobby, "r")
    putPlace(rModel.place, "r")
    showQuestions()
}

async function question() {
    if (!modelValid()) return false
    await request(session(), "question").then(resp => displayQuestion(resp))
}

async function order(sign) {
    if (!modelValid()) return false
    await request({"name": name(), "isMen": isMen(), "answer": sign}, "answer").then(_ => showMatchText())
}


const displayMate = ({"res": [name, model]}) => {
    putName(name)
    putAge(model.age.value, "m")
    putHeight(model.height.value, "m")
    putHobby(model.hobby, "m")
    putPlace(model.place, "m")
}

async function myMate() {
    if (!modelValid()) return false
    await request({"name": name(), "isMen": isMen()}, "myResults").then(resp => {
        console.log(resp);
        displayMate(resp);
        showMatch();
    })
}
