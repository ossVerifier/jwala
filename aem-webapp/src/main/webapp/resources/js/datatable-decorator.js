/**
 * Creates an id delimited by dash "-"
 * e.g. ["btn", "1"] returns btn-1
 */
var createDashDelimitedId = function(idFragments) {
    var tmpId;
    for (var i = 0; i < idFragments.length; i++) {
        if (tmpId === undefined) {
            tmpId = idFragments[i];
        } else {
            tmpId = tmpId + "-" + idFragments[i]
        }
    }
    return tmpId;
}

/**
 * This method transforms a table to a JQuery DataTable.
 */
var decorateTableAsDataTable = function(tableId,
                                        tableDef,
                                        applyThemeRoller,
                                        hideHeaderAndFooter,
                                        editCallback,
                                        rowSelectCallback,
                                        expandIcon,
                                        collapseIcon,
                                        childTableDetails,
                                        parentItemId /* e.g. group id. This is used to retrieve child data via the data callback method when the expand-collapse control is clicked */,
                                        rootId /* This is the first element id in a hierarchy */ ){

    var self = this;

    // build column definitions based on props
    var aoColumnDefs = [];
    var aaSorting = [];

    $(tableDef).each(function(itemIndex, item, itemArray) {
            aoColumnDefs[itemIndex] = {"sTitle": item.sTitle,
                                       "mData": item.mData,
                                       "aTargets": [itemIndex]};

            if(item.bVisible !== undefined) {
                aoColumnDefs[itemIndex].bVisible = item.bVisible;
            }

            if(item.tocType === "link") {
                aoColumnDefs[itemIndex].mRender = function(data, type, full) {
                    return React.renderComponentToStaticMarkup(new Anchor({id:createDashDelimitedId([tableId,
                                                                                                     "link",
                                                                                                     full.id.id]),
                                                                   valueId:full.id.id,
                                                                   value:data,
                                                                   callback:editCallback}));
                };

            } else if (item.tocType === "control") {

                self.expandCollapseEnabled = true;
                aoColumnDefs[itemIndex].mDataProp = null;
                aoColumnDefs[itemIndex].sClass = "control center";
                aoColumnDefs[itemIndex].sWidth = "20px";
                aoColumnDefs[itemIndex].bSortable = false;

                aoColumnDefs[itemIndex].mRender = function (data, type, full) {

                        var parentItemId = (parentItemId === undefined ? full.id.id : parentItemId);

                        if(Object.prototype.toString.call(childTableDetails) === "[object Array]") {
                            for (var i = 0; i < childTableDetails.length; i++) {
                                childTableDetails[i]["data"] = data;
                            }
                        } else {
                            childTableDetails["data"] = data;
                        }

                        var theRootId = (rootId === undefined ? full.id.id : rootId);
                        return React.renderComponentToStaticMarkup(
                                    new ExpandCollapseControl({id:createDashDelimitedId([tableId,
                                                                                         "ctrl-expand-collapse",
                                                                                         full.id.id]),
                                                               expandIcon:expandIcon,
                                                               collapseIcon:collapseIcon,
                                                               childTableDetails:childTableDetails,
                                                               rowSubComponentContainerClassName:"row-sub-component-container",
                                                               parentItemId:full.id.id,
                                                               dataTable:$("#" + tableId).dataTable(),
                                                               rootId:theRootId}));

                }

            } else if (item.tocType === "array") {
                aoColumnDefs[itemIndex].mRender = function (data, type, full) {
                    var str = "";
                    /* would be better with _Underscore.js : */
                    for (var idx = 0; idx < data.length; idx=idx+1) {
                        str = str + (str === "" ? "" : ", ") + data[idx][item.displayProperty];
                    }
                return str;
                }
            } else if (item.tocType === "button") {

                aoColumnDefs[itemIndex].sClass = aoColumnDefs[itemIndex].sClass + " control center";
                aoColumnDefs[itemIndex].sWidth = "90px";
                aoColumnDefs[itemIndex].bSortable = false;

                aoColumnDefs[itemIndex].mRender = function (data, type, full) {
                    var id = tableId + "btn" + item.btnLabel.replace(/\s+/g, '') +  full.id.id;
                    return React.renderComponentToStaticMarkup(new DataTableButton({id:id,
                                                                                    itemId:full.id.id,
                                                                                    label:item.btnLabel,
                                                                                    callback:item.btnCallback,
                                                                                    isToggleBtn:item.isToggleBtn,
                                                                                    label2:item.label2,
                                                                                    callback2:item.callback2}));
                }
            }

            aaSorting[itemIndex] = [itemIndex, 'asc'];

        });

        var dataTableProperties = {"aaSorting": aaSorting,
                                   "aoColumnDefs": aoColumnDefs,
                                   "bJQueryUI": applyThemeRoller === undefined ? true : applyThemeRoller,
                                   "bAutoWidth": false,
                                   "bStateSave": true,
                                   "aLengthMenu": [[25, 50, 100, 200, -1],
                                                   [25, 50, 100, 200, "All"]],
                                   "iDisplayLength": 25,
                                   "fnDrawCallback": rowSelectCallback};

        if (hideHeaderAndFooter === false) {
            dataTableProperties["sDom"] = "t";
        }

        return $("#" + tableId).dataTable(dataTableProperties);
}