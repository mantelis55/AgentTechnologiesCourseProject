import React, { useEffect, useRef, useState} from "react";
import { useNavigate } from "react-router-dom";
import { Button } from 'react-bootstrap';

export default function ChatPage() {
    const navigate = useNavigate();
    const webSocket = useRef(null);
    const chatInput = useRef()
    const recieverInput = useRef()
    const receiversInput = useRef()
    const usernameInput = useRef()
    const passwordInput = useRef()
    const AIDInput = useRef()
    const TypeInput = useRef()
    const NameInput = useRef()

    const performativeInput = useRef()
    const messageInput = useRef()
    const [leftContent, setLeftContent] = useState([]);
    const [rightContent, setRightContent] = useState([]);
    const [usersContent, setUsersContent] = useState([]);
    const [agentsContent, setAgentsContent] = useState([]);
    const [resultContent, setResultContent] = useState([]);
    const [otherResultContent, setOtherResultContent] = useState([]);
    const [typeContent, setTypeContent] = useState([]);
    const [aclContent, setAclContent] = useState([]);
    const [sessionID, setSessionID] = useState("");
    let rightContentRef = useRef();
    let leftContentRef = useRef();

    useEffect(() => {
        if(sessionStorage.getItem("loggedIn") !== null) {
            navigate("/");
        }
        else{
            sessionStorage.setItem("loggedIn", "true");

            webSocket.current = new WebSocket('ws://localhost:8080/ChatServer/ws');
            webSocket.current.onopen = () => {
                webSocket.current.send(sessionStorage.getItem("token"));
                console.log('WebSocket Client Connected');
            };
        
            webSocket.current.onmessage = (message) => {
                const json = JSON.parse(message.data);
                if(json.purpose === "SESSIONID"){
                    setSessionID(json.message);
                }
                else if (json.purpose === "USERLIST"){
                    updateUserList();
                }
                else if (json.purpose === "AGENTLIST"){
                    setAgentsContent(json.agents);
                }
                else if (json.purpose === "ACLLIST"){
                    setAclContent(json.acl);
                }
                else if (json.purpose === "TYPELIST"){
                    setTypeContent(json.types);
                }
                else if (json.purpose === "LOGOUT"){
                    navigate("/")
                }
                else{
                    setRightContent(rightContentRef.current + json.message + "\n");
                    setLeftContent(leftContentRef.current + getNewLineCount(json.message));
                }
            };
            webSocket.current.onerror = function() {
                console.log('Connection Error');
            };
        }
        // eslint-disable-next-line
    }, []);

    useEffect(() => {
        rightContentRef.current = rightContent;
    }, [rightContent])

    useEffect(() => {
        leftContentRef.current = leftContent;
        var chatDiv = document.getElementById("chatForm");
        chatDiv.scrollTo(0,chatDiv.scrollHeight)
    }, [leftContent])

    function messageToAll(e){
        const messageText = chatInput.current.value
        setLeftContent(leftContentRef.current + "YOU: " + messageText + "\n");
        setRightContent(rightContentRef.current + getNewLineCount(messageText));
        chatInput.current.value = "";
        const requestOptions = {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json'},
            body:JSON.stringify({ "performative": "MESSAGE", "content": {"message":  sessionStorage.getItem('token') + ": " + messageText, "sessionID": sessionID, "sender": sessionStorage.getItem('token') }})
        };
        fetch('http://localhost:8080/ChatServer/messages', requestOptions)
        .then(res => res.text())
        .then(res => console.log(res))
    }

    function updateUserList(e){
        const requestOptions = {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json'},
            body:JSON.stringify({ "performative": "LOGGED_IN"})
        };
        fetch('http://localhost:8080/ChatServer/messages', requestOptions)
        .then(res => res.json())
        .then(data => setUsersContent(data.message));
    }

    function privateMessage(e){
        const messageText = chatInput.current.value
        chatInput.current.value = "";
        const requestOptions = {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json'},
            body:JSON.stringify({ "performative": "MESSAGE", "content": { "message":  sessionStorage.getItem('token') + ": " + messageText, "sessionID": sessionID, "sender": sessionStorage.getItem('token'), "receiver": recieverInput.current.value}})
        };

        fetch('http://localhost:8080/ChatServer/messages', requestOptions)
        .then(res => {if(res.status === 200) {setLeftContent(leftContentRef.current + "YOU: " + messageText + "\n");
        setRightContent(rightContentRef.current + getNewLineCount(messageText));} else {setLeftContent(leftContentRef.current +  "Cannot send message to user - " + recieverInput.current.value + "\n");
        setRightContent(rightContentRef.current + getNewLineCount("Cannot send message to user - " + recieverInput.current.value));}})
    }

    function registerMessage(e){
        if(recieverInput.current.value === ""){
            messageToAll(e);
        }
        else privateMessage(e);
    }

    function getPerformatives(e){
        const requestOptions = {
            method: 'GET',
            headers: { 
                'Content-Type': 'application/json'}
        };
        fetch('http://localhost:8080/ChatServer/messages', requestOptions)
        .then(res => res.text())
        .then(res => setOtherResultContent(res))
    }
    function getTypes(e){
        const requestOptions = {
            method: 'GET',
            headers: { 
                'Content-Type': 'application/json'}
        };
        fetch('http://localhost:8080/ChatServer/agents/classes', requestOptions)
        .then(res => res.text())
        .then(res => setOtherResultContent(res))
    }
    function getRunning(e){
        const requestOptions = {
            method: 'GET',
            headers: { 
                'Content-Type': 'application/json'}
        };
        fetch('http://localhost:8080/ChatServer/agents/running', requestOptions)
        .then(res => res.text())
        .then(res => setOtherResultContent(res))
    }

    function deleteAgent(e){
        const AIDText = AIDInput.current.value;
        if(AIDText === "") return
        const requestOptions = {
            method: 'DELETE',
            headers: { 
                'Content-Type': 'application/json'}
        };
        fetch('http://localhost:8080/ChatServer/agents/running/' + AIDText, requestOptions)
        .then(res => res.text())
        .then(res => setOtherResultContent(res))
    }

    function putAgent(e){
        const TypeText = TypeInput.current.value;
        const NameText = NameInput.current.value;
        if(TypeText === "" || NameText === "") return

        const requestOptions = {
            method: 'PUT',
            headers: { 
                'Content-Type': 'application/json'}
        };
        fetch('http://localhost:8080/ChatServer/agents/running/' + TypeText + "/" + NameText, requestOptions)
        .then(res => res.text())
        .then(res => setOtherResultContent(res))
    }

    function sendACLMessage(e){
        const performativeText = performativeInput.current.value;
        const messageText = messageInput.current.value;
        const usernameText = usernameInput.current.value;
        let receiversText = receiversInput.current.value;
        const passwordText = passwordInput.current.value;
        let x = {};
        if(receiversText !== "" && performativeText !== "MESSAGE") receiversText = receiversText.split(',')
        if(performativeText === "MESSAGE" && receiversText !== "") x = JSON.stringify({ "performative": performativeText, "content": { "username": usernameText, "password": passwordText,"message": messageText, "receiver": receiversText}})
        else x = JSON.stringify({ "performative": performativeText, "content": { "username": usernameText, "password": passwordText,"message": messageText, "receivers": receiversText}})
        const requestOptions = {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json'},
            body:x
        };
        fetch('http://localhost:8080/ChatServer/messages', requestOptions)
        .then(res => res.text())
        .then(res => setResultContent(res))
    }

    function logOut(e){
        const requestOptions = {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json'},
            body:JSON.stringify({"performative": "LOG_OUT", "content": {"sessionID": sessionID, "username": sessionStorage.getItem("token")}})
        };
        fetch('http://localhost:8080/ChatServer/messages', requestOptions)
        .then(res => {if(res.status === 200) navigate("/")});
    }

    function getNewLineCount(text){
        let newLineCount = text.length / 25;
        var newlines = "";
        for(var i = 0; i < newLineCount; i++){
            newlines += "\n"
        }
        return newlines;
    }

    return (
        <>
            <div className="centerLeft">
                <div className="logo"><b>ONLINE</b></div>
                <div className="usersForm" id="usersForm">
                    <b><p className="textUsers">{usersContent}</p></b>
                </div>
                <Button className="loginButton btn-secondary" onClick={logOut}>Logout</Button>
            </div>

            <div className="centerRight">
                <div className="logo"><b>INFO</b></div>
                <div className="agentsForm" id="agentsForm">
                    <b><p className="textUsers">{agentsContent}</p></b>
                </div>
                <div className="aclForm" id="aclForm">
                    <b><p className="textUsers">{aclContent}</p></b>
                </div>
                <div className="typesForm" id="typesForm">
                    <b><p className="textUsers">{typeContent}</p></b>
                </div>
            </div>
            <div className="centerMiddle">
                <div className="logo"><b>CHAT</b></div>
                <div className="chatForm" id="chatForm">
                    <b><p className="textLeft">{leftContent}</p></b>
                    <b><p className="textRight">{rightContent}</p></b>
                </div>    
            </div>
            <div className="chattingFunctionalities">
                <div className="sideBySide">
                    <input id="chatInput" className="form-control inputs inputChat" ref={chatInput} placeholder="Chat here!" required/> 
                    <input className="form-control inputs inputReciever" ref={recieverInput} placeholder="All" required/> 
                </div>
                <Button className="loginButton btn-secondary" onClick={registerMessage}>Send</Button>
            </div>  
            <div className="aclMessageFunctionalities">
                <div className="sideBySide">
                    <b className="logo">ACL MESSAGE</b>
                </div>
                <div className="sideBySide">
                    <input id="performative" className="form-control inputs inputPerformative" ref={performativeInput} placeholder="Performative" required/> 
                    <input className="form-control inputs inputPerformative" ref={messageInput} placeholder="Message" required/> 
                </div>
                <div className="sideBySide">
                    <input id="receivers" className="form-control inputs inputPerformative" ref={receiversInput} placeholder="Receivers" required/> 
                    <input className="form-control inputs inputPerformative" ref={usernameInput} placeholder="Username" required/> 
                </div>
                <div className="sideBySide">
                    <input className="form-control inputs inputPerformative" ref={passwordInput} placeholder="Password" required/> 
                </div>
                <div>
                    <div className="otherForm" id="aclForm">
                        <b><p className="textUsers">{resultContent}</p></b>
                    </div>
                </div>
                <Button className="loginButton btn-secondary" onClick={sendACLMessage}>Send ACL MESSAGE</Button>
            </div>  

            <div className="otherMessageFunctionalities">
            <div className="sideBySide">
                    <b className="logo">CALLS</b>
                </div>
                <div className="sideBySide">
                    <Button className="loginButton btn-secondary" onClick={getPerformatives}>GET Performatives</Button>
                    <Button className="loginButton btn-secondary" onClick={getRunning}>GET running Agents</Button>
                    <Button className="loginButton btn-secondary" onClick={getTypes}>GET Agent types</Button>
                </div>
                <div className="sideBySide">
                    <input id="aid" className="form-control inputs inputPerformative" ref={AIDInput} placeholder="AID" required/> 
                    <Button className="loginButton btn-secondary" onClick={deleteAgent}>DELETE Agent</Button>
                </div>
                <div className="sideBySide">
                    <input  className="form-control inputs inputPerformative" ref={TypeInput} placeholder="Type" required/> 
                    <input className="form-control inputs inputPerformative" ref={NameInput} placeholder="Name" required/>
                    <Button className="loginButton btn-secondary" onClick={putAgent}>Create Agent</Button>
                </div>
                <div>
                    <div className="otherForm" id="otherForm">
                        <b><p className="textUsers">{otherResultContent}</p></b>
                    </div>
                </div>
            </div>  
        </>
    )
}




