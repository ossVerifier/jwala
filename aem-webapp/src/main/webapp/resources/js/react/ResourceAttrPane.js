/** @jsx React.DOM */
var ResourceAttrPane = React.createClass({
    getInitialState: function() {
        return {showDeleteConfirmDialog:false};
    },
    render: function() {

        var toolbar = <div className="resource-attr-toolbar">
                          <RButton title="Delete attribute"
                                   className="ui-state-default ui-corner-all default-icon-button-style"
                                   spanClassName="ui-icon ui-icon-trash"
                                   onClick={this.onClickDel}/>
                          <RButton title="Add attribute"
                                   className="ui-state-default ui-corner-all default-icon-button-style"
                                   spanClassName="ui-icon ui-icon-plus"
                                   onClick={this.onClickAdd}/>
                          <RButton title="Generate XML"
                                   className="ui-state-default ui-corner-all default-icon-button-style"
                                   spanClassName="ui-icon ui-icon-play"
                                   onClick={this.props.generateXmlSnippetCallback}/>
                      </div>

        var msg = <div className="text-align-center"><br/><b>Are you sure you want to delete the selected item ?</b><br/><br/></div>;

        var confirmationDlg = <ModalDialogBox title="Confirmation Dialog Box"
                                              show={this.state.showDeleteConfirmDialog}
                                              okCallback={this.confirmDeleteCallback}
                                              cancelCallback={this.cancelDeleteCallback}
                                              content={msg}
                                              okLabel="Yes"
                                              cancelLabel="No"/>

        if (this.props.resourceData !== null) {
            var attrValTable = <AttrValTable ref="attrTable"
                                             attributes={this.props.resourceData.attributes}
                                             updateCallback={this.updateCallback}
                                             requiredAttributes={this.props.requiredAttributes}/>

            return <div className="attr-values-container">{toolbar}{attrValTable}{confirmationDlg}</div>
        }

        return <div className="attr-values-container">Please select a resource to view it&apos;s attributes</div>
    },
    confirmDeleteCallback: function() {
        var selectedAttributes = this.refs.attrTable.getSelectedAttributes();
        var tempAttrArray = [];
        for (key in this.props.resourceData.attributes) {
            if (selectedAttributes.indexOf(key) === -1) {
                var attributesForRestConsumption = {};
                attributesForRestConsumption["key"] = key;
                attributesForRestConsumption["value"] = this.props.resourceData.attributes[key];
                tempAttrArray.push(attributesForRestConsumption);
           }
        }

        this.setState({showDeleteConfirmDialog:false});

        this.props.updateAttributes(this.props.resourceData, tempAttrArray);
    },
    cancelDeleteCallback: function() {
        this.setState({showDeleteConfirmDialog:false});
    },
    onClickDel: function() {
        if (this.refs["attrTable"] !== undefined) {
            var selectedAttributes = this.refs.attrTable.getSelectedAttributes();
            if (selectedAttributes.length > 0) {
                this.setState({showDeleteConfirmDialog:true});
            }
        }
    },
    onClickAdd: function() {
        // the required attribute data transformation process to pass to the rest service
        var tempAttrArray = [];
        var largestNumber = 0;

        for (key in this.props.resourceData.attributes) {
            var attributesForRestConsumption = {};
            attributesForRestConsumption["key"] = key;
            attributesForRestConsumption["value"] = this.props.resourceData.attributes[key];
            tempAttrArray.push(attributesForRestConsumption);

            var num = parseInt(key.substring("key".length + 1), 10);
            if (!isNaN(num)) {
                largestNumber = (largestNumber < num) ? num : largestNumber;
            };
        }

        // the new attribute
        tempAttrArray.push({key:"key-" + ++largestNumber, value:null});
        this.props.updateAttributes(this.props.resourceData, tempAttrArray);
    },
    refresh: function(resource) {
        this.setState({resource:resource});
    },
    updateCallback: function(attrKey, attribute) {
        // We have to transform the attributes first to a format that the Rest API understands.
        // We might need to change the REST API to do away with this! This would do for now.
        var tempAttrArray = [];
        for (key in this.props.resourceData.attributes) {
            var attributesForRestConsumption = {};
            if (key === attrKey) {
                attributesForRestConsumption["key"] = attribute["attrName"];
                attributesForRestConsumption["value"] = attribute["attrValue"];
            } else {
                attributesForRestConsumption["key"] = key;
                attributesForRestConsumption["value"] = this.props.resourceData.attributes[key];
            }
            tempAttrArray.push(attributesForRestConsumption);
        }

        this.props.updateAttributes(this.props.resourceData, tempAttrArray);
    }
 });

 var AttrValTable = React.createClass({
    render: function() {
        var attrElements = [];

        for (key in this.props.attributes) {
            var required = (this.props.requiredAttributes.indexOf(key) > -1);
            attrElements.push(<AttrValRow key={key}
                                          ref={key}
                                          attrName={key}
                                          attrValue={this.props.attributes[key]}
                                          updateCallback={this.props.updateCallback}
                                          required={required}/>);
        }

        return <div className="attr-val-table-container">
                   <table className="attr-val-table">
                       <tbody>
                           {attrElements}
                       </tbody>
                   </table>
               </div>
    },
    getSelectedAttributes: function() {
        var selectedAttributes = [];
        for (key in this.props.attributes) {
            if (this.refs[key].isSelected()) {
                selectedAttributes.push(key);
            }
        }
        return selectedAttributes;
    }
});

var AttrValRow = React.createClass({
    getInitialState:function() {
        return {
            selected: false,
            attrName: this.props.attrName,
            attrNameCopy: this.props.attrName,
            attrValue: this.props.attrValue,
            attrValueCopy: this.props.attrValue
        };
    },
    mixins: [React.addons.LinkedStateMixin],
    render: function() {

        var checkBoxTdClassName = "name-text-field " + (this.props.required ? "required-name" : "");
        var checkBoxTd = <td>
                             <input type="checkbox" onChange={this.onCheckboxChange} checked={this.state.selected}/>
                         </td>;

        var firstCol = this.props.required ? <td className="required-symbol"><span title="Required">*</span></td> : checkBoxTd;
        var attrNameTextFieldClassName = "name-text-field " + (this.props.required ? "required-name" : "");
        return <tr>
                   {firstCol}
                   <td>
                       <input ref="attrNameTextField"
                              className={attrNameTextFieldClassName}
                              valueLink={this.linkState("attrName")}
                              onBlur={this.onAttrNameTextBoxBlur}
                              onKeyDown={this.onAttrNameTextFieldKeyDown}/>
                   </td>
                   <td>
                       <input ref="attrValTextField"
                              className="val-text-field"
                              valueLink={this.linkState("attrValue")}
                              onBlur={this.onAttrTextBoxBlur}
                              onKeyDown={this.onAttrValTextFieldKeyDown}/>
                   </td>
               </tr>;
    },
    onAttrNameTextFieldKeyDown: function(e) {
        if (!this.props.required) {
            if (e.key === ResourceItem.ENTER_KEY /* && AttrValRow.isValidResourceName(this.state.attrName) */) {
                $(this.refs.attrNameTextField.getDOMNode()).blur();
            } else if (e.key === ResourceItem.ESCAPE_KEY) {
                this.setState({attrName:this.state.attrNameCopy});
            }
        } else {
            e.preventDefault();
        }
    },
    onAttrValTextFieldKeyDown: function(e) {
        if (e.key === ResourceItem.ENTER_KEY /* && AttrValRow.isValidResourceName(this.state.attrValue) */) {
            $(this.refs.attrValTextField.getDOMNode()).blur();
        } else if (e.key === ResourceItem.ESCAPE_KEY) {
            this.setState({attrValue:this.state.attrValueCopy});
        }
    },
    onCheckboxChange: function() {
        this.setState({"selected":this.state.selected ? false : true});
    },
    onAttrNameTextBoxBlur: function() {
        this.props.updateCallback(this.props.attrName, this.getAttributeState());
    },
    onAttrTextBoxBlur: function() {
        this.props.updateCallback(this.props.attrName, this.getAttributeState());
    },
    componentWillReceiveProps: function(nextProps) {
        this.setState({attrName:nextProps.attrName, attrValue:nextProps.attrValue});
    },
    getAttributeState: function() {
        return {attrName:this.state.attrName, attrValue:this.state.attrValue};
    },
    isSelected: function() {
        return this.state.selected;
    },
    statics: {
        ENTER_KEY: "Enter",
        ESCAPE_KEY: "Escape"
    }
});