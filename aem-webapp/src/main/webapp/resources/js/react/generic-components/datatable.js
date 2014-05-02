/** @jsx React.DOM */
var TocDataTable = React.createClass({
    dataTable: null,
    render: function() {
        if (this.props.className !== undefined) {
            $.fn.dataTableExt.oStdClasses = {sTable:this.props.className,
                                             sSortAsc:$.fn.dataTableExt.oStdClasses.sSortAsc,
                                             sSortDesc:$.fn.dataTableExt.oStdClasses.sSortDesc};
        }
        return <div>
                    <table id={this.props.tableId}/>
               </div>
    },
    componentDidUpdate: function() {
        if (this.dataTable === null) {
            this.dataTable = decorateTableAsDataTable(this.props.tableId,
                                                      this.props.tableDef,
                                                      this.props.applyThemeRoller,
                                                      true,
                                                      this.props.editCallback,
                                                      this.rowSelectCallback,
                                                      this.props.expandIcon,
                                                      this.props.collapseIcon,
                                                      this.getDataTable,
                                                      this.props.childTableDetails);
        }

        if (this.dataTable !== null) {
            this.dataTable.fnClearTable(this.props.data);
            this.dataTable.fnAddData(this.props.data);
            this.dataTable.fnDraw();
        }
    },
    rowSelectCallback: function() {
        var self = this;
        var dataTable = this.dataTable;
        $(dataTable).find("tr").off("click").on("click", function(e) {
            if ($(this).hasClass("row_selected") ) {
                $(this).removeClass("row_selected");
            } else {
                $(dataTable).find("tr").removeClass("row_selected");
                $(this).addClass("row_selected");

                var cell = dataTable.find("tbody tr.row_selected")[0];
                if (cell !== undefined) {
                    var i = dataTable.fnGetPosition(cell);
                    var item = dataTable.fnGetData(i);
                    self.props.selectItemCallback(item);
                }
            }
        });
    },
    getDataTable: function() {
        return this.dataTable;
    }
});