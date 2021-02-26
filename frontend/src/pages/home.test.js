import React, {useEffect} from 'react'

import Home from "./home";
import { act } from 'react-dom/test-utils'
import {mount} from "enzyme";
import Enzyme from 'enzyme';
import Adapter from '@wojtekmaj/enzyme-adapter-react-17';
import MaterialTable from "material-table";
import axios from "axios";
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
  it('should initialize the datatable', () => {
    expect(true).toBe(true)
    const wrapper = mount(<Home/>)

    let columns = wrapper.find(MaterialTable).props().columns;
    let columnsFiltered = columns.map(column => _.pick(column, ['title', 'field', 'type']));

    expect(columnsFiltered).toEqual([
        {title: 'Id', field: 'id', type: 'numeric'},
        {title: 'Name', field: 'name'},
        {title: 'Code', field: 'code'},
        {title: 'Opening Date', field: 'openingDate', type: "date"},
        {title: 'Special field 1', field: 'specialField1'},
        {title: 'Special field 2', field: 'specialField2'}
      ])
  })

  it('should render the datable data as data incoming from the props', async () => {
    axios.get.mockImplementation(() => Promise.resolve({data: mockData}))
    const wrapper = mount(<Home/>)

    await(waitForComponentToPaint(wrapper))

    expect(wrapper.find(MaterialTable).prop("data")).toBe(mockData)
  })
})