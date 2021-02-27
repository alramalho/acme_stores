import React, {useEffect, useState} from 'react'
import axios from "axios";
import Table from "../components/table";

import {cloneDeep} from 'lodash'

const Home = () => {
  const [data, setData] = useState([])

  useEffect(() => {
    axios.get('/stores').then(response => {
      setData(response.data)
    })
  }, [])

  const handleNameChange = (entry, newName) =>
    axios.put(`/update_store_name/${entry.id}`, {
      newName: newName
    }).then(() => {
      const entryIndex = data.findIndex((elem) => elem.id === entry.id)
      const newData = cloneDeep(data)
      newData[entryIndex] = {...entry, name: newName}
      setData(newData)
    })

  useEffect(() => {
    console.log('DATA CHANGED')
    console.log(data ? data[0] : null)
  }, [data])


  return (
    <div style={{padding: '5%'}} data-testid="wrapper">
      <Table data={data} onNameChange={handleNameChange}/>
      <div className={'credits'}>
        Made with ❤️ by <a href={'https://alramalho.com'}>Al</a>
      </div>
    </div>
  )
}

export default Home;

