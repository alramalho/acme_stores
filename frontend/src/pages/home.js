import React, {useEffect, useState} from 'react'
import axios from "axios";
import MaterialTable from "material-table";
import {tableIcons} from "../utils/styles";
import {TextField} from "@material-ui/core";
import Table from "../components/table";

const Home = () => {
  const [data, setData] = useState([])

  useEffect(() => {
    axios.get('/stores').then(response => {
      setData(response.data)
    })
  }, [])

  // const updateStoreName = (storeId) => axios({
  //   method: 'put',
  //   url: '/update_store_name/'
  // })

  return (
    <div style={{padding: '5%'}} data-testid="wrapper">
      <Table data={data}/>
    </div>
  )
}

export default Home;

