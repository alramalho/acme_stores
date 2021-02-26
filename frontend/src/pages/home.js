import React, {forwardRef, useEffect, useState} from 'react'
// import MaterialTable from "material-table";
import axios from "axios";
import MaterialTable from "material-table";
import {tableIcons} from "../utils/icons";


const Home = () => {
  const [data, setData] = useState([])

  useEffect(() => {
    axios.get('/stores').then(response => {
      setData(response.data)
    })
  }, [])

  return (
    <div style={{padding: '5%'}} data-testid="wrapper">
      <MaterialTable
        icons={tableIcons}
        columns={[
          {title: 'Id', field: 'id', type: 'numeric'},
          {title: 'Name', field: 'name'},
          {title: 'Code', field: 'code'},
          {title: 'Opening Date', field: 'openingDate', type: "date"},
          {title: 'Special field 1', field: 'specialField1'},
          {title: 'Special field 2', field: 'spercialField2'}
        ]}
        title="Store viewer"
        options={{
          draggable: false,
          pageSize: 15,
        }}
        data={data}
      />
    </div>
  )
}

export default Home;

