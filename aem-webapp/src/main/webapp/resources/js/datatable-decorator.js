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
            if (!isArray(item)) {
                aoColumnDefs[itemIndex] = {"sTitle": item.sTitle,
                                           "mData": item.mData,
                                           "aTargets": [itemIndex]};

                if(item.bVisible !== undefined) {
                    aoColumnDefs[itemIndex].bVisible = item.bVisible;
                }

                if (item.tocType === "control") {
                    self.expandCollapseEnabled = true;
                    aoColumnDefs[itemIndex].mDataProp = null;
                    aoColumnDefs[itemIndex].sClass = "control center";
                    aoColumnDefs[itemIndex].sWidth = "20px";
                    aoColumnDefs[itemIndex].bSortable = false;
                } else if (item.tocType === "button") {
                        aoColumnDefs[itemIndex].bSortable = false;
                }
            } else {

                /**
                 * Only 1 field related to data can be merged to one cell!
                 * And that includes the ExpandCollapseControl which has an mData!
                 * This is because every cell can only have one mData.
                 */
                var theItem = {"sTitle": null, "mData": null};
                // look for the item with the mData
                for (var i = 0; i < item.length; i++) {
                    if (item[i].mData !== undefined && item[i].tocType === undefined) {
                        theItem = item[i];
                        break;
                    }
                }

                aoColumnDefs[itemIndex] = {"sTitle": theItem.sTitle,
                                           "mData": theItem.mData,
                                           "aTargets": [itemIndex]};

                aoColumnDefs[itemIndex].sClass = "nowrap";
                aoColumnDefs[itemIndex].bSortable = false;
            }

            if (!isArray(item) && item.mRender !== undefined) {
                // If mRender is set to a function
                aoColumnDefs[itemIndex].mRender = item.mRender;
            } else if (!isArray(item) && item.tocType === "custom") {
                if(item.tocRenderer == 'undefined') {
                    alert('You set tocType to custom, but you did not set tocRenderCfgFn to a function(dataTable, data, aoColumnDefs, i) { aoColumnDefs[i].mRender = function(data, type, full){}}!');
                } return item.tocRenderCfgFn(self, item, aoColumnDefs, itemIndex);
            } else {
                aoColumnDefs[itemIndex].mRender = function(data, type, full) {
                   var renderStr = "";
                   if (isArray(item)) {
                        for (var i = 0; i < item.length; i++) {
                            renderStr += renderComponents(tableId,
                                                          parentItemId,
                                                          rootId,
                                                          childTableDetails,
                                                          item[i],
                                                          data,
                                                          type,
                                                          full,
                                                          expandIcon,
                                                          collapseIcon,
                                                          editCallback);
                        }
                    } else {
                        renderStr = renderComponents(tableId,
                                                     parentItemId,
                                                     rootId,
                                                     childTableDetails,
                                                     item,
                                                     data,
                                                     type,
                                                     full,
                                                     expandIcon,
                                                     collapseIcon,
                                                     editCallback);
                        return renderStr;
                    }
                    return "<div style='overflow:hidden;text-align:right'>" + renderStr + "</div>"
                }
            }
        });

        var dataTableProperties = {"aaSorting": [],
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

var renderComponents = function(tableId,
                                parentItemId,
                                rootId,
                                childTableDetails,
                                item,
                                data,
                                type,
                                full,
                                expandIcon,
                                collapseIcon,
                                editCallback) {
    var renderedComponent;

    if (item.tocType === "link") {
        renderedComponent = renderLink(item, tableId, data, type, full, editCallback);
    } else if (item.tocType === "control") {
        renderedComponent = renderExpandCollapseControl(tableId,
                                                        parentItemId,
                                                        rootId,
                                                        childTableDetails,
                                                        data,
                                                        type,
                                                        full,
                                                        expandIcon,
                                                        collapseIcon);
    } else if (item.tocType === "array") {
        renderedComponent = renderArray(item, data);
    } else if (item.tocType === "button") {
        renderedComponent = renderButton(tableId, item, data, type, full);
    } else {
        renderedComponent = data;
    }
    return renderedComponent;
}

var isArray = function(val) {
    return (val instanceof Array)
}

var renderButton = function(tableId, item, data, type, full) {
    var id = tableId + "btn" + item.btnLabel.replace(/\s+/g, '') +  full.id.id;
    return React.renderComponentToStaticMarkup(new DataTableButton({id:id,
                                                   className:item.className,
                                                   customBtnClassName:item.customBtnClassName,
                                                   itemId:full.id.id,
                                                   label:item.btnLabel,
                                                   callback:item.btnCallback,
                                                   isToggleBtn:item.isToggleBtn,
                                                   label2:item.label2,
                                                   callback2:item.callback2}));
}

var renderLink = function(item, tableId, data, type, full, editCallback) {
    if (item.hRefCallback === undefined) {
        return React.renderComponentToStaticMarkup(new Anchor({id:createDashDelimitedId([tableId,
                                                               "link",
                                                               full.id.id]),
                                                               valueId:full.id.id,
                                                               value:item.linkLabel !== undefined ?
                                                                     item.linkLabel :
                                                                     data,
                                                               callback:editCallback}));
    } else {
        return "<a href='" + item.hRefCallback(full) + "' target='_blank'>" + item.linkLabel + "</a>";
    }
}

var renderExpandCollapseControl = function(tableId, parentItemId, rootId, childTableDetails, data, type, full, expandIcon, collapseIcon) {
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

var renderArray = function(item, data) {

    var str = "";
    /* would be better with _Underscore.js : */
    for (var idx = 0; idx < data.length; idx=idx+1) {
        str = str + (str === "" ? "" : ", ") + data[idx][item.displayProperty];
    }
    return str;
}