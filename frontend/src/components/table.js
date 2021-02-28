import React from 'react'
import {tableIcons} from "../utils/styles";
import MaterialTable from "material-table";

let tableColumns = [
  {title: 'Id', field: 'id', editable: 'never', type: 'numeric', width: 50, cellStyle: {backgroundColor: '#fafbfc'}},
  {title: 'Name', field: 'name'},
  {title: 'Code', field: 'code', editable: 'never'},
  {
    title: 'Description',
    field: 'description',
    editable: 'never',
    render: rowData =>
      <span>{rowData.description ? rowData.description.substr(1, 25).concat('...') : null}</span>
  },
  {title: 'Opening Date', field: 'openingDate', editable: 'never', type: "date"},
  {title: 'Special field 1', field: 'specialField1', editable: 'never'},
  {title: 'Special field 2', field: 'specialField2', editable: 'never'}
];

const Table = ({data, onNameChange}) => {
  return (
    <MaterialTable
      icons={tableIcons}
      columns={tableColumns}
      title="ACME® Corporation Stores Viewer"
      options={{
        draggable: false,
        pageSize: 20,
        exportButton: true,
        exportAllData: true,
        rowStyle: {
          backgroundColor: '#fdfdfd',
          color: '#1c1c1c'
        },
        headerStyle: {
          fontFamily: '\'Domine\', serif',
          fontWeight: 700,
          backgroundColor: '#fff3eb',
          color: '#ff5700'
        }
      }}
      data={data}
      cellEditable={{
        onCellEditApproved: (newValue, oldValue, rowData, columnDef) => onNameChange(rowData, newValue)
      }}
      detailPanel={[
        {
          tooltip: 'Show full description',
          render: rowData => <span>{rowData.description ? rowData.description : null}</span>
        },
      ]}
    />
  )
}

export default Table