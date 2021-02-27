import React from 'react'
import {tableIcons} from "../utils/styles";
import MaterialTable from "material-table";

const Table = ({data}) => {
  return (
    <MaterialTable
      icons={tableIcons}
      columns={[
        {
          title: 'Id', field: 'id', editable: 'never', type: 'numeric', cellStyle: {
            backgroundColor: '#fcfcfc',
          },
        },
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
      ]}
      title="ACME® Corporation Stores Viewer"
      options={{
        draggable: false,
        pageSize: 20,
        exportButton: true,
        exportAllData: true,
        headerStyle: {
          backgroundColor: '#eff5fa',
          color: '#2a6ea7'
        }
      }}
      data={data}
      cellEditable={{
        onCellEditApproved: (newValue, oldValue, rowData, columnDef) => {
          return new Promise((resolve, reject) => {
            console.log('newValue: ' + newValue);
            console.log('oldValue: ' + oldValue);
            console.log(rowData);
            console.log(columnDef);
            setTimeout(resolve, 1000);
          });
        }
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