import React from 'react'
import Home from "./pages/home";
import axios from "axios";
import './App.css'

axios.defaults.baseURL = 'http://localhost:7000';

const App = () => <Home/>

export default App;
