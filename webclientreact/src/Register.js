import React, { useRef } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from 'react-bootstrap';

export default function Register() {
    const usernameInput = useRef()
    const passwordInput = useRef()
    const passwordInputTwo = useRef()
    const navigate = useNavigate();

    function processRegister(e){
        const usernameText = usernameInput.current.value
        const passwordText = passwordInput.current.value
        const passwordTextTwo = passwordInputTwo.current.value
        
        if(usernameText.length !== 0 && passwordText.length !== 0 && passwordTextTwo.length !== 0){
            if(passwordText === passwordTextTwo && passwordText.length >= 8){
                const requestOptions = {
                    method: 'POST',
                    headers: { 
                        'Content-Type': 'application/json'},
                    body:JSON.stringify({ "performative": "REGISTER", "content": {"username": usernameText, "password": passwordText }})
                };
                fetch('http://localhost:8080/ChatServer/messages', requestOptions)
                .then(res => res.text())
                .then(res => console.log(res))
                directToLogin();
            }
        }

    }

    function directToLogin(e){
        navigate("/")
    }

    return (
        <>
            <div className="centerMiddle">
                <div className="logo"><b>CHIT-CHAT</b></div>
                <div className="formBack">
                    <div className="header">Register</div>
                    <br></br>
                    <input className="form-control inputs" ref={usernameInput} type="text" placeholder="Username" maxLength="22" required/>
                    <br></br>
                    <input className="form-control inputs" ref={passwordInput} type="password" placeholder="Password" maxLength="27" required/>
                    <br></br>
                    <input className="form-control inputs" ref={passwordInputTwo} type="password" placeholder="Re-type Password" maxLength="27" required/>
                    <br></br>
                    <Button className="loginButton btn-secondary" onClick={processRegister}>Register</Button>
                    <Button className="registerButton btn-secondary" onClick={directToLogin}>Login</Button>       
                </div>       
            </div>
        </>
    )
}
