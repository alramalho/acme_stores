import axios from "axios";
import Enzyme, {mount} from "enzyme";
import Home from "../pages/home";
import MaterialTable from "material-table";
import React from "react";
import {act} from "react-dom/test-utils";
import Adapter from "@wojtekmaj/enzyme-adapter-react-17";
import Table from "./table";

const _ = require("lodash")

Enzyme.configure({adapter: new Adapter()});

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


describe('when rendering the datatable', () => {
  it('should initialize it', () => {
    const datatable = mount(<Table/>).find(MaterialTable)
    let titleFieldTypeColumns = datatable.props().columns.map(column => _.pick(column, ['title', 'field', 'type']));

    expect(titleFieldTypeColumns).toEqual([
      {title: 'Id', field: 'id', type: 'numeric'},
      {title: 'Name', field: 'name'},
      {title: 'Code', field: 'code'},
      {title: 'Description', field: 'description'},
      {title: 'Opening Date', field: 'openingDate', type: "date"},
      {title: 'Special field 1', field: 'specialField1'},
      {title: 'Special field 2', field: 'specialField2'}
    ])
    expect(datatable.prop("options")).toEqual(expect.objectContaining({
      exportButton: true,
      exportAllData: true
    }))
  })

  it('should render the datable data', async () => {
    const wrapper = mount(<Table data={mockData}/>)

    expect(wrapper.find(MaterialTable).prop("data")).toBe(mockData)
  })

  it('should be able to only edit the name', async () => {
    const datatable = mount(<Table/>).find(MaterialTable)
    let titleFieldTypeColumns = datatable.props().columns.map(column => _.pick(column, ['title', 'editable']));

    datatable.prop('cellEditable').onCellEditApproved('new name', 'old name', {
      "id": 1,
      "code": "code1",
      "description": "desc1",
      "name": "nam1",
      "openingDate": "2021-02-07",
      "specialField1": "",
      "specialField2": "sp1"
    })

    expect(titleFieldTypeColumns).toEqual([
      {title: 'Id', editable: 'never'},
      {title: 'Name'},
      {title: 'Code', editable: 'never'},
      {title: 'Description', editable: 'never'},
      {title: 'Opening Date', editable: 'never'},
      {title: 'Special field 1', editable: 'never'},
      {title: 'Special field 2', editable: 'never'}
    ])
  })

})