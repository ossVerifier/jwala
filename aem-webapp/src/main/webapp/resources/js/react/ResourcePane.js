/** @jsx React.DOM */
var ResourcePane = React.createClass({
    render: function() {
        var resourceTypeDropDownToolbar = React.createElement("div", {className:"resource-type-toolbar"},
                                           React.createElement(RButton, {title:"Delete Resource",
                                                                          className:"ui-state-default ui-corner-all default-icon-button-style",
                                                                          spanClassName:"ui-icon ui-icon-trash",
                                                                          onClick:this.onClickDel}),
                                           React.createElement(ResourceTypeDropDown, {onClickAdd:this.onClickAdd}),
                                           React.createElement(ResourceList, {ref:"resourceList",
                                                                              data:this.props.resourceList,
                                                                              groupName:this.props.groupName,
                                                                              selectResourceCallback:this.props.selectResourceCallback,
                                                                              refreshListResponseCallback:this.props.refreshResourceListResponseCallback}));

        return React.createElement("div", {className:"add-edit-delete-resources-container"}, resourceTypeDropDownToolbar);
    },
    onClickAdd: function(resourceType) {
        this.refs.resourceList.add(resourceType);
    },
    onClickDel: function() {
        this.refs.resourceList.deleteSelectedItems();
    },
    refreshResourceList: function(groupName) {
        this.refs.resourceList.refresh(groupName);
    }
});

var ResourceTypeDropDown = React.createClass({
    getInitialState: function() {
        return {resourceTypes:[]}
    },
    render: function() {
        var options = [];
        var self = this;

        if (this.state.resourceTypes.length > 0) {
            this.state.resourceTypes.forEach(function(resourceType){
                var key = resourceType.name.replace(/\s/g, '');
                options.push(React.createElement(ResourceTypeOption, {key:key, resourceType:resourceType}));
            });
            return React.createElement("div", {className:"resource-type-dropdown"},
                       React.createElement("select", {ref:"select", className:"resource-type-dropdown"}, options),
                       React.createElement(RButton, {title:"Add Resource",
                                                     className:"ui-state-default ui-corner-all default-icon-button-style",
                                                     spanClassName:"ui-icon ui-icon-plus",
                                                     onClick:this.onClickAdd}));
        }
        return React.createElement("span", null, "Loading resource types...");
    },
    componentDidMount: function() {
        ServiceFactory.getResourceService().getResourceTypes(this.getResourceTypesCallback);
    },
    getResourceTypesCallback: function(response) {
        this.setState({resourceTypes:response.applicationResponseContent});
    },
    getSelectedResourceType: function() {
        return $(this.refs.select.getDOMNode()).find("option:selected").text();
    },
    onClickAdd: function() {
        this.props.onClickAdd(this.getResourceType(this.getSelectedResourceType()));
    },
    getResourceType: function(name) {
        for (var i = 0; i < this.state.resourceTypes.length; i++) {
            if (this.state.resourceTypes[i].name === name) {
                return this.state.resourceTypes[i];
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
            groupName:null,
            resourceList:[],
            currentResourceItemId:null,
            selectedResourceIds:{},
            showDeleteConfirmDialog:false
        };
    },
    refresh: function(groupName, resourceName) {
        ServiceFactory.getResourceService().getResources(groupName, this.refreshListResponseCallback.bind(this, groupName, resourceName));
    },
    refreshListResponseCallback: function(groupName, resourceName, response) {
        this.setState({groupName:groupName, resourceList:response.applicationResponseContent, currentResourceItemId:resourceName});
        this.props.refreshListResponseCallback(ResourceList.getResourceByName(resourceName, response.applicationResponseContent));
    },
    render: function() {
        var resourceElements = [];
        var self = this;

        this.state.resourceList.forEach(function(resource){
            resourceElements.push(React.createElement(ResourceItem, {key:resource.name,
                                                                     resource:resource,
                                                                     onClick:self.onClick,
                                                                     isHighlighted:(self.state.currentResourceItemId === resource.name),
                                                                     onSelect:self.onSelect,
                                                                     editMode:(self.state.currentResourceItemId === resource.name),
                                                                     onChange:self.onChangeResourceListItem,
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
    onChangeResourceListItem: function(resourceTypeName, resourceName, newResourceName) {
        ServiceFactory.getResourceService().updateResourceName(this.state.groupName,
                                                               resourceTypeName,
                                                               resourceName,
                                                               newResourceName,
                                                               this.updateResourceSuccessCallback.bind(resourceName),
                                                               this.updateResourceErrorCallback);
    },
    updateResourceSuccessCallback: function(resourceName) {
        this.refresh(this.state.groupName, resourceName);
    },
    updateResourceErrorCallback: function(errMsg) {
         $.errorAlert(errMsg, "Error");
    },
    onClick: function(resource) {
        this.props.selectResourceCallback(resource);
        this.setState({currentResourceItemId:resource.name});
    },
    add: function(resourceType) {
        var largestNumber = 0;

        // Get next sequence number for the generated name
        this.state.resourceList.forEach(function(resourceItem){
            var num = parseInt(resourceItem.name.substring(resourceType.name.length + 1), 10);
            if (!isNaN(num)) {
                largestNumber = (largestNumber < num) ? num : largestNumber;
            };
        });

        var name = resourceType.name.replace(/\s/g, '-').toLowerCase() + "-" + ++largestNumber;

        ServiceFactory.getResourceService().insertNewResource(this.state.groupName,
                                                              resourceType.name,
                                                              name,
                                                              this.getAttributes(resourceType),
                                                              this.insertNewResourceSuccessCallback.bind(this, name),
                                                              this.insertNewResourceErrorCallback);
    },
    getAttributes: function(resourceType) {
        var attributes = [];
        resourceType.properties.forEach(function(type){
            var attribute = {};
            attribute["key"] = type.name;
            attribute["value"] = type.value !== undefined ? type.value : null;
            attributes.push(attribute);
        });
        return attributes;
    },
    insertNewResourceSuccessCallback: function(resourceName) {
        this.refresh(this.state.groupName, resourceName);
    },
    insertNewResourceErrorCallback: function(errMsg) {
        $.errorAlert(errMsg, "Error");
    },
    onSelect: function(id, checked) {
        this.state.selectedResourceIds[id] = checked;
    },
    deleteSelectedItems: function() {
        if (!$.isEmptyObject(this.state.selectedResourceIds)) {
            this.setState({showDeleteConfirmDialog:true});
        }
    },
    confirmDeleteCallback: function() {
        var selectedResourceNames = [];
        var self = this;
        this.state.resourceList.forEach(function(resource) {
            if (self.state.selectedResourceIds[resource.name] === true) {
                selectedResourceNames.push(resource.name);
            }
        });
        ServiceFactory.getResourceService().deleteResources(this.state.groupName,
                                                            selectedResourceNames,
                                                            this.deleteSuccessCallback,
                                                            this.deleteErrorCallback);
        this.setState({showDeleteConfirmDialog:false, selectedResourceIds:{}});
    },
    cancelDeleteCallback: function() {
        this.setState({showDeleteConfirmDialog:false});
    },
    deleteSuccessCallback: function() {
        this.refresh(this.state.groupName, null);
    },
    deleteErrorCallback: function(errMsg) {
        // TODO: Delete should not throw an exception with an empty error message. Check why!
        if (errMsg !== undefined && errMsg !== null && errMsg !== "") {
            $.errorAlert(errMsg, "Error");
        }
    },

    statics: {
        getResourceByName: function(name, resourceList) {
            for (var i = 0; i < resourceList.length; i++) {
                if (name === resourceList[i].name) {
                    return resourceList[i];
                }
            }
            return null;
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
    setHighlight: function(val) {
        this.setState({isHighlighted:val});
    },
    statics: {
        ENTER_KEY: "Enter",
        ESCAPE_KEY: "Escape",
        isValidResourceName: function(resourceName) {
            return ((resourceName !== "") && resourceName.match(/^[a-zA-Z0-9-_. ]+$/i) !== null);
        }
    }
});