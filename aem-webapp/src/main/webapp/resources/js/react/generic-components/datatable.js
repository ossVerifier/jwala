/** @jsx React.DOM */
var TocDataTable = React.createClass({
    dataTable: null,
    render: function() {
        if (this.props.className !== undefined) {
            $.fn.dataTableExt.oStdClasses = {sTable:this.props.className /* This will not be set if applyThemeRoller is true */,
                                             sSortAsc:$.fn.dataTableExt.oStdClasses.sSortAsc,
                                             sSortDesc:$.fn.dataTableExt.oStdClasses.sSortDesc};
        }

        var headerComponents = [];
        var header;
        if (this.props.headerComponents !== undefined) {
            for (var i = 0; i < this.props.headerComponents.length; i++) {
                var obj = this.props.headerComponents[i];
                if (obj.tocType === "button") {
                    headerComponents.push(new DataTableButton({id:this.props.tableId + "_" + obj.id,
                                                               itemId:"whatever",
                                                               label:obj.btnLabel,
                                                               className:"inline-block",
                                                               callback:obj.btnCallback}));
                } else if (obj.tocType === "label") {
                    // TODO: Modify so it can display changing status. For now it would act as a shim to align the buttons
                    headerComponents.push(React.DOM.div({className:obj.className}, obj.text));
                } else if (obj.tocType === "space") {
                    headerComponents.push(" ");
                }
            }
            headerComponents.push(React.DOM.span({className:"accordion-title-text-position"}, this.props.title));
            header = React.DOM.div({className:"accordion-title nowrap text-align-right", style: this.props.title !== undefined  ? {} : {display:'none'}},
                                                                  headerComponents)
        } else {
            header = this.props.title === undefined ? "" : new React.DOM.h3(null, this.props.title);
        }

        return React.DOM.div({style:this.props.hide === true ? {display:"none"} : {}},
                             header,
                             React.DOM.table({id: this.props.tableId}))
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
                                                      this.props.childTableDetails,
                                                      this.props.initialSortColumn);
        }

        if (this.dataTable !== null) {
            this.dataTable.fnClearTable(this.props.data);
            this.dataTable.fnAddData(this.props.data);
            this.dataTable.fnDraw();
            this.dataTable.fnSort(this.props.initialSortColumn);
        }

        if (this.props.applyThemeRoller !== false && this.props.className !== "") {
            $("#" + this.props.tableId).attr("class", this.props.className);
        }
    },
    rowSelectCallback: function() {

        if (this.props.selectItemCallback === undefined) {
            return;
        }

        var self = this;
        var dataTable = this.dataTable;
        $(dataTable).find("thead > tr, tbody > tr, > tr").off("click").on("click", function(e) {
            if ($(this).hasClass("row_selected") ) {
                $(this).removeClass("row_selected");
            } else {
                $(dataTable).find("thead > tr, tbody > tr, > tr").removeClass("row_selected");
                $(this).addClass("row_selected");

                var cell = dataTable.find("tbody > tr.row_selected")[0];
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
    },
    getDefaultProps: function() {
        return {
            initialSortColumn : []
        };
    }
});