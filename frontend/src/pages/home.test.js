import React from 'react'

import Home from "./home";
import {act} from 'react-dom/test-utils'
import Enzyme, {mount} from "enzyme";
import Adapter from '@wojtekmaj/enzyme-adapter-react-17';
import MaterialTable from "material-table";
import axios from "axios";
import Table from "../components/table";

const _ = require("lodash")

Enzyme.configure({adapter: new Adapter()});

jest.mock("axios")

const waitForComponentToPaint = async (wrapper) => {
  await act(async () => {
    await new Promise(resolve => setTimeout(resolve, 0));
    wrapper.update();
  });
};

const mockData = [
  {
    "id": 1,
    "code": "code1",
    "description": "desc1",
    "name": "nam1",
    "openingDate": "2021-02-07",
    "specialField1": "",
    "specialField2": "sp1"
  },
  {
    "id": 2,
    "code": "code2",
    "name": "nam2",
    "openingDate": "2021-02-08",
    "specialField1": "sp2",
    "specialField2": ""
  },
];

describe('when testing home', () => {

  it('should render the datable data with the table coming from backend', async () => {
    const mockAxios = jest.fn().mockImplementation(() => Promise.resolve({data: mockData}))
    axios.get = mockAxios
    const wrapper = mount(<Home/>)

    await (waitForComponentToPaint(wrapper))

    expect(mockAxios).toHaveBeenCalledWith('/stores')
    expect(wrapper.find(Table).prop("data")).toBe(mockData)
  })
  //
  // it('should be able to only edit the name and request the change to the backend', async () => {
  //   const mockAxios = jest.fn()
  //   mockAxios.mockImplementation(() => Promise.resolve({data: mockData}))
  //   const datatable = mount(<Home/>).find(Table)
  //
  //   await (waitForComponentToPaint(datatable))
  //   datatable.prop('cellEditable').onCellEditApproved('new name', 'old name', {
  //     "id": 1,
  //     "code": "code1",
  //     "description": "desc1",
  //     "name": "nam1",
  //     "openingDate": "2021-02-07",
  //     "specialField1": "",
  //     "specialField2": "sp1"
  //   })
  //
  //   expect(mockAxios).toHaveBeenCalledWith({
  //     url: '/update_store_name/1',
  //     method: 'put',
  //     data: {
  //       newName: 'new name'
  //     }
  //   })
  // })
})