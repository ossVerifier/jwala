/** @jsx React.DOM */
var ResourceEditor = React.createClass({
    render: function() {
        var resourceTypeDropDownToolbar = React.createElement("div", {className:"resource-type-toolbar"},
                                           React.createElement(RButton, {title:"Delete Resource",
                                                                          className:"ui-state-default ui-corner-all default-icon-button-style",
                                                                          spanClassName:"ui-icon ui-icon-trash",
                                                                          onClick:this.onClickDel}),
                                           React.createElement(ResourceTypeDropDown, {resourceTypes:this.props.resourceTypes,
                                                                                      onClickAdd:this.onClickAdd}),
                                           React.createElement(ResourceList, {ref:"resourceList",
                                                                              data:this.props.resourceList,
                                                                              groupName:this.props.groupName}));

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
        this.props.onClickAdd(this.getSelectedResourceType());
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
        ServiceFactory.getResourceService().getResources(groupName, this.refreshListCallback.bind(this, groupName, resourceName));
    },
    refreshListCallback: function(groupName, resourceName, response) {
          this.setState({groupName:groupName, resourceList:response.applicationResponseContent, currentResourceItemId:resourceName});
    },
    render: function() {
        var resourceElements = [];
        var self = this;

        var i = 0;
        this.state.resourceList.forEach(function(resource){
            resourceElements.push(React.createElement(ResourceItem, {key:resource.friendlyName,
                                                                     idx:i++,
                                                                     resource:resource,
                                                                     onClick:self.onClick,
                                                                     isHighlighted:(self.state.currentResourceItemId === resource.friendlyName),
                                                                     onSelect:self.onSelect,
                                                                     editMode:(self.state.currentResourceItemId === resource.friendlyName),
                                                                     onChange:self.onChangeResourceListItem}));
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
    onChangeResourceListItem: function(idx, resourceItemName) {
        this.state.resourceList[idx].name = resourceItemName;
    },
    onClick: function(name) {
        this.setState({currentResourceItemId:name});
    },
    add: function(resourceTypeName) {
        var largestNumber = 0;

        // Get next sequence number for the generated name
        this.state.resourceList.forEach(function(resourceItem){
            var num = parseInt(resourceItem.friendlyName.substring(resourceTypeName.length + 1), 10);
            if (!isNaN(num)) {
                largestNumber = (largestNumber < num) ? num : largestNumber;
            };
        });

        var name = resourceTypeName.replace(/\s/g, '-').toLowerCase() + "-" + ++largestNumber;

        ServiceFactory.getResourceService().insertNewResource(this.state.groupName,
                                                              resourceTypeName,
                                                              name,
                                                              {},
                                                              this.insertNewResourceSuccessCallback.bind(this, name),
                                                              this.insertNewResourceErrorCallback);
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
        this.setState({showDeleteConfirmDialog:true});
    },
    confirmDeleteCallback: function() {
        var newResourceList = [];
        var self = this;
        this.state.resourceList.forEach(function(resource) {
            if (self.state.selectedResourceIds[resource.id] === undefined || !self.state.selectedResourceIds[resource.id]) {
                newResourceList.push({id:resource.id, name:resource.name});
            }
        });
        this.setState({resourceList:newResourceList, showDeleteConfirmDialog:false});
    },
    cancelDeleteCallback: function() {
        this.setState({showDeleteConfirmDialog:false});
    }
});

var ResourceItem = React.createClass({
    getInitialState: function() {
        return {
            resourceName:this.props.resource.friendlyName,
            resourceNameCopy:this.props.resource.friendlyName,
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
            this.setState({resourceNameCopy:this.state.resourceName});
            this.props.onChange(this.props.idx, this.state.resourceName);
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
        this.props.onClick(this.props.resource.friendlyName);
    },
    onCheckBoxChange: function(e) {
        this.props.onSelect(this.props.resource.id, this.refs.checkBox.getDOMNode().checked);
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