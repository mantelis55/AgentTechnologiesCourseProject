import { Route, Routes } from "react-router-dom";
import ChatPage from "./ChatPage";
import Login from "./Login"
import Register from "./Register";
import React from 'react';

function App() {

  return (
    <>
    <Routes>
      <Route path='' element={<Login/>}/>
      <Route path='/register' element={<Register/>}/>
      <Route path='/chatpage' element={<ChatPage/>}/>
    </Routes>
    </>

  )
}

export default App;
