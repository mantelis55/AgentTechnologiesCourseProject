import React, { useRef, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from 'react-bootstrap';


export default function Login() {
    const usernameInput = useRef()
    const passwordInput = useRef()
    const navigate = useNavigate();

    function processLogin(e){
        const usernameText = usernameInput.current.value
        const passwordText = passwordInput.current.value

        if(usernameText.length !== 0 && passwordText.length !== 0){
            const requestOptions = {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json'},
                body:JSON.stringify({ "performative": "LOGIN", "content": {"username": usernameText, "password": passwordText }})
            };

            fetch('http://localhost:8080/ChatServer/messages', requestOptions)
            .then(res => {if(res.status === 200) directToChatPage(usernameText)})
        }
    }

    useEffect(() => {
        sessionStorage.clear();
    });

    function directToRegister(e){
        navigate("/register")
    }

    function directToChatPage(username){
        sessionStorage.setItem('token', username)
        navigate("/chatpage")
    }

    
    return (
        <>
            <div className="centerMiddle">
                <div className="logo"><b>CHIT-CHAT</b></div>
                <div className="formBack">
                    <div className="header">Login</div>
                    <br></br>
                    <input  className="form-control inputs" ref={usernameInput} type="text" placeholder="Username" maxLength="22" required />
                    <input className="form-control inputs" ref={passwordInput} type="password" placeholder="Password" maxLength="27" required/>
                    <br></br>
                    <Button className="loginButton btn-secondary" onClick={processLogin}>Login</Button>
                    <Button className="registerButton btn-secondary" onClick={directToRegister}>Register</Button>       
                </div>       
            </div>
        </>
    )
}
