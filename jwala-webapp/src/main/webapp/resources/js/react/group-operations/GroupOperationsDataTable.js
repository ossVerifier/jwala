/** @jsx React.DOM */
var GroupOperationsDataTable = React.createClass({
    dataTable: null,
    getInitialState: function() {
        return {data: null};
    },
    render: function() {
        let self = this;

        if (!this.state.data) {
            return <div>Loading data...</div>;
        }

        let rows = [];
        this.state.data.forEach(function(group){
            rows.push(<tr className={"groupTr" + group.id}>
                         <td><span className="ui-icon ui-icon-triangle-1-e" onClick={self.onClickRow}/></td>
                         <td>{group.name}</td>
                     </tr>);
        });

        return <div className="GroupOperationsDataTable">
                   <table className="table">
                       <thead>
                           <th/>
                           <th>Group Name</th>
                       </thead>
                       <tbody>
                           {rows}
                       </tbody>
                   </table>
               </div>
    },
    componentDidMount: function() {
        let self = this;
        ServiceFactory.getGroupService().getGroups().then(function(response){
            self.setState({data: response.applicationResponseContent});
        }).caught(function(response){
            $.errorAlert("Failed to load data! Please see the browser's console for details.", null, true);
            console.log(response);
        });
    },
    componentDidUpdate: function(prevProps, prevState) {
        if (this.dataTable) {
            return;
        }

        // transform to DataTable
        this.dataTable = $(".GroupOperationsDataTable table").dataTable({bJQueryUI: true, iDisplayLength: 25,
            aLengthMenu: [[25, 50, 100, 200, -1], [25, 50, 100, 200, "All"]], aaSorting: [[1, "asc"]]});
    },
    onClickRow: function(e) {
        let tr = e.target.parentNode.parentNode;
        let trOpen = this.dataTable.fnOpen(tr);

        // mount something to trOpen
        React.renderComponent(<button>Click me!</button>, trOpen);
    }
});
