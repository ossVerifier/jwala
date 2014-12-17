/** @jsx React.DOM */
var ResourcesConfig = React.createClass({

    render: function() {

        var rColDef = [{key:"id.id"},
                       {title:"Name", key:"name"},
                       {title:"Type", key:"type"}];

        var rData = [{"id":{"id":1},
                      "name":"jdbc/TestDB",
                      "type":"javax.sql.DataSource"}];

        return <div><RDataTable colDefinitions={rColDef} data={rData} /></div>
    }

})