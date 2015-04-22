/** @jsx React.DOM */
var ResourcePane = React.createClass({
    render: function() {
        if (this.props.data !== null) {
            var resourceTypeDropDownToolbar = React.createElement("div", {className:"resource-type-toolbar"},
                                               React.createElement(RButton, {title:"Delete Resource",
                                                                              className:"ui-state-default ui-corner-all default-icon-button-style",
                                                                              spanClassName:"ui-icon ui-icon-trash",
                                                                              onClick:this.onClickDel}),
                                               React.createElement(ResourceTypeDropDown, {onClickAdd:this.props.onAddResource,
                                                                                          resourceTypes:this.props.resourceTypes}),
                                               React.createElement(ResourceList, {ref:"resourceList",
                                                                                  groupName:this.props.groupName,
                                                                                  data:this.props.data,
                                                                                  insertNewResourceCallback:this.props.insertNewResourceCallback,
                                                                                  deleteResourcesCallback:this.props.deleteResourcesCallback,
                                                                                  currentResourceName:this.props.currentResourceName,
                                                                                  editMode:this.props.editMode,
                                                                                  selectResourceCallback:this.props.selectResourceCallback,
                                                                                  onChangeResourceListItem:this.props.onChangeResourceListItem,
                                                                                  deleteResourcesCallback:this.props.deleteResourcesCallback}));

            return React.createElement("div", {className:"add-edit-delete-resources-container"}, resourceTypeDropDownToolbar);
        }
        return React.createElement("div", {className:"add-edit-delete-resources-container"});
    },
    onClickDel: function() {
        this.refs.resourceList.deleteSelectedItems();
    }
});

var ResourceTypeDropDown = React.createClass({
    render: function() {
        var options = [];
        var self = this;

        if (this.props.resourceTypes.length > 0) {
            this.props.resourceTypes.forEach(function(resourceType){
                var key = resourceType.name.replace(/\s/g, '');
                options.push(React.createElement(ResourceTypeOption, {key:key, resourceType:resourceType}));
            });



            return React.createElement("div", {className:"resource-type-dropdown"},
                       React.createElement("select", {ref:"select", className:"resource-type-dropdown", onChange:this.onChangeResourceType}, options),
                       React.createElement(RButton, {title:"Add Resource",
                                                     className:"ui-state-default ui-corner-all default-icon-button-style",
                                                     spanClassName:"ui-icon ui-icon-plus",
                                                     onClick:this.onClickAdd}));
        }
        return React.createElement("span", null, "Loading resource types...");
    },
    getSelectedResourceType: function() {
        return $(this.refs.select.getDOMNode()).find("option:selected").text();
    },
    onClickAdd: function() {
        this.props.onClickAdd(this.getResourceType(this.getSelectedResourceType()));
    },
    getResourceType: function(name) {
        for (var i = 0; i < this.props.resourceTypes.length; i++) {
            if (this.props.resourceTypes[i].name === name) {
                return this.props.resourceTypes[i];
            }
        }
        return null;
    }
});

var ResourceTypeOption = React.createClass({
    getInitialState: function() {
        return {isSelected: false};
    },
    render: function() {
        return React.createElement("option", {value:this.props.resourceType.name}, this.props.resourceType.name);
    }
});

var ResourceList = React.createClass({
    getInitialState: function() {
        return {
            selectedResourceNames:{},
            showDeleteConfirmDialog:false
        };
    },
    render: function() {
        var resourceElements = [];
        var self = this;

        this.props.data.forEach(function(resource){
            resourceElements.push(React.createElement(ResourceItem, {key:resource.name,
                                                                     resource:resource,
                                                                     onClick:self.onClick,
                                                                     isHighlighted:(self.props.currentResourceName === resource.name),
                                                                     onSelect:self.onSelect,
                                                                     editMode:(self.props.currentResourceName === resource.name ? self.props.editMode : false),
                                                                     onChange:self.props.onChangeResourceListItem,
                                                                     resourceTypeName:resource.resourceTypeName}));
        });

        var confirmationDlg = React.createElement(ModalDialogBox, {title:"Confirmation Dialog Box",
                                                                   show:this.state.showDeleteConfirmDialog,
                                                                   okCallback:this.confirmDeleteCallback,
                                                                   cancelCallback:this.cancelDeleteCallback,
                                                                   content:<div className="text-align-center"><br/><b>Are you sure you want to delete the selected item ?</b><br/><br/></div>,
                                                                   okLabel:"Yes",
                                                                   cancelLabel:"No"});

        return React.createElement("div", {className:"resource-list"}, resourceElements, confirmationDlg);
    },
    onClick: function(resource) {
        this.props.selectResourceCallback(resource);
    },
    onSelect: function(id, checked) {
        this.state.selectedResourceNames[id] = checked;
    },
    deleteSelectedItems: function() {
        if (!$.isEmptyObject(this.state.selectedResourceNames)) {
            this.setState({showDeleteConfirmDialog:true});
        }
    },
    confirmDeleteCallback: function() {
        var selectedResourceNames = [];
        var self = this;
        this.props.data.forEach(function(resource) {
            if (self.state.selectedResourceNames[resource.name] === true) {
                selectedResourceNames.push(resource.name);
            }
        });
        this.props.deleteResourcesCallback(selectedResourceNames);
        this.setState({showDeleteConfirmDialog:false, selectedResourceNames:{}});
    },
    cancelDeleteCallback: function() {
        this.setState({showDeleteConfirmDialog:false});
    },
    statics: {
        getAttributes: function(resourceType) {
            var attributes = [];
            resourceType.properties.forEach(function(type){
                var attribute = {};
                attribute["key"] = type.name;
                attribute["value"] = type.value !== undefined ? type.value : null;
                attributes.push(attribute);
            });
            return attributes;
        }
    }
});

var ResourceItem = React.createClass({
    getInitialState: function() {
        return {
            resourceName:this.props.resource.name,
            resourceNameCopy:this.props.resource.name,
            isValidResourceName:true
        };
    },
    render: function() {
        var highlightClassName = this.props.isHighlighted ?
                            (this.state.isValidResourceName ? "ui-state-highlight" : "ui-state-error no-border") : "";
        return React.createElement("div", {className:highlightClassName, onClick:this.onDivClick},
                                        React.createElement("div",
                                            {style:{display:(this.state.isValidResourceName ? "none" : "")},
                                             className:"error-msg-div-margin"}, "Invalid name!"),
                                        React.createElement("input", {ref:"checkBox",
                                                                      type:"checkbox",
                                                                      className:"resource-item",
                                                                      onChange:this.onCheckBoxChange,
                                                                      selected:this.state.isSelected}),
                                        React.createElement("input", {ref:"textField",
                                                                      className:"resource-item no-border width-max resource-name-text-field " + highlightClassName,
                                                                      value:this.state.resourceName,
                                                                      onChange:this.onTextFieldChange,
                                                                      onKeyDown:this.onTextFieldKeyDown,
                                                                      onBlur:this.onTextFieldBlur,
                                                                      maxLength:40}));
    },
    componentDidMount: function() {
        if (this.props.editMode) {
            this.refs.textField.getDOMNode().focus();
            this.refs.textField.getDOMNode().select();
        }
    },
    onTextFieldChange: function() {
        var resourceName = $(this.refs.textField.getDOMNode()).val();
        if (resourceName.trim() === "") {
            resourceName = "";
        }
        var newState = {resourceName:resourceName};

        if (resourceName !== "") {
            newState["isValidResourceName"] = ResourceItem.isValidResourceName(resourceName);
        }
        this.setState(newState);
    },
    onTextFieldBlur: function(e) {
        if (!ResourceItem.isValidResourceName(this.state.resourceName)) {
            this.setState({resourceName:this.state.resourceNameCopy, isValidResourceName:true});
        } else {
            this.props.onChange(this.props.resourceTypeName, this.state.resourceNameCopy, this.state.resourceName);
            this.setState({resourceNameCopy:this.state.resourceName});
        }
    },
    onTextFieldKeyDown: function(e) {
        if (e.key === ResourceItem.ENTER_KEY && ResourceItem.isValidResourceName(this.state.resourceName)) {
            $(this.refs.textField.getDOMNode()).blur();
        } else if (e.key === ResourceItem.ESCAPE_KEY) {
            this.setState({resourceName:this.state.resourceNameCopy, isValidResourceName:true});
        }
    },
    onDivClick: function() {
        this.props.onClick(this.props.resource);
    },
    onCheckBoxChange: function(e) {
        this.props.onSelect(this.props.resource.name, this.refs.checkBox.getDOMNode().checked);
    },
    statics: {
        ENTER_KEY: "Enter",
        ESCAPE_KEY: "Escape",
        isValidResourceName: function(resourceName) {
            return ((resourceName !== "") && resourceName.match(/^[a-zA-Z0-9-_. ]+$/i) !== null);
        }
    }
});