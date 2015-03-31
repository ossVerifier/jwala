var AddEditDeleteResources = React.createClass({
    render: function() {
        var resourceTypeDropDownArea = React.createElement("div", {className:"resource-type-dropdown"},
                                           React.createElement(RButton, {title:"Delete Resource",
                                                                          className:"ui-state-default ui-corner-all default-icon-button-style",
                                                                          spanClassName:"ui-icon ui-icon-trash",
                                                                          onClick:this.onClickDel}),
                                           React.createElement(ResourceTypeDropDown, {ref:"resourceTypeDropdown",
                                                                                      resourceTypes:this.props.resourceTypes}),
                                           React.createElement(RButton, {title:"Add Resource",
                                                                         className:"ui-state-default ui-corner-all default-icon-button-style",
                                                                         spanClassName:"ui-icon ui-icon-plus",
                                                                         onClick:this.onClickAdd}),
                                           React.createElement(ResourceList, {ref:"resourceList", data:this.props.resourceList}));

        return React.createElement("div", {className:"add-edit-delete-resources-container"}, resourceTypeDropDownArea);
    },
    onClickAdd: function() {
        this.refs.resourceList.add(this.refs.resourceTypeDropdown.getSelectedResourceType());
    },
    onClickDel: function() {
        this.refs.resourceList.deleteSelectedItems();
    }
});

var ResourceTypeDropDown = React.createClass({
    render: function() {
        var options = [];
        var self = this;
        this.props.resourceTypes.forEach(function(resourceType){
            var key = resourceType.name.replace(/\s/g, '');
            options.push(React.createElement(ResourceTypeOption, {key:key, resourceType:resourceType}));
        });
        return React.createElement("select", {ref:"select", className:"resource-type-dropdown"}, options);
    },
    getSelectedResourceType: function() {
        return $(this.refs.select.getDOMNode()).find("option:selected").text();
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
        return {resourceList:ResourceList.getResourceList(this.props.data),
                currentResourceItemId:null,
                selectedResourceIds:{}};
    },
    componentWillReceiveProps : function(nextProps) {
        this.setState({resourceList:ResourceList.getResourceList(nextProps.data)});
    },
    render: function() {
        var resourceElements = [];
        var self = this;

        this.state.resourceList.forEach(function(resource){
            resourceElements.push(React.createElement(ResourceItem, {key:resource.id,
                                                                     resource:resource,
                                                                     onClick:self.onClick,
                                                                     isHighlighted:(self.state.currentResourceItemId === resource.id),
                                                                     onSelect:self.onSelect,
                                                                     editMode:(self.state.currentResourceItemId === resource.id)}));
        });

        return React.createElement("div", {className:"resource-list"}, resourceElements);
    },
    onClick: function(id) {
        this.setState({currentResourceItemId:id});
    },
    add: function(resourceTypeName) {
        var date = new Date();
        var tempId = date.getTime();
        var name = resourceTypeName.replace(/\s/g, '-').toLowerCase() + "-" + tempId;
        this.state.resourceList.push({id:tempId, name:name});
        this.setState({currentResourceItemId:tempId});
    },
    onSelect: function(id, checked) {
        this.state.selectedResourceIds[id] = checked;
    },
    deleteSelectedItems: function() {
        var newResourceList = [];
        var self = this;
        this.state.resourceList.forEach(function(resource) {
            if (self.state.selectedResourceIds[resource.id] === undefined || !self.state.selectedResourceIds[resource.id]) {
                newResourceList.push({id:resource.id, name:resource.name});
            }
        });
        this.setState({resourceList:newResourceList});
    },
    statics: {
        getResourceList: function(data) {
            var resourceList = [];

            // Let's not assume that this.props.data is this class' definition of a resource.
           data.forEach(function(item){
               resourceList.push({id:item.id, name:item.name});
           });

           return resourceList;
        }
    }
});

var ResourceItem = React.createClass({
    getInitialState: function() {
        return {resourceName:this.props.resource.name};
    },
    render: function() {
        var highlightClassName = this.props.isHighlighted ? "ui-state-highlight" : "";
        return React.createElement("div", {className:highlightClassName, onClick:this.onDivClick},
                                        React.createElement("input", {ref:"checkBox",
                                                                      type:"checkbox",
                                                                      className:"resource-item",
                                                                      onChange:this.onCheckBoxChange,
                                                                      selected:this.state.isSelected}),
                                        React.createElement("input", {ref:"textField",
                                                                      className:"resource-item no-border width-max resource-name-text-field " + highlightClassName,
                                                                      value:this.state.resourceName,
                                                                      onChange:this.onTextFieldChange,
                                                                      onKeyPress:this.onTextFieldKeyPress,
                                                                      maxLength:40}));
    },
    componentDidMount: function() {
        if (this.props.editMode) {
            this.refs.textField.getDOMNode().select();
        }
    },
    onTextFieldChange: function() {
        this.setState({resourceName:$(this.refs.textField.getDOMNode()).val()});
    },
    onTextFieldKeyPress: function(e) {
        if (e.key === ResourceItem.ENTER_KEY) {
            if (ResourceItem.isValidResourceName(this.state.resourceName)) {
                $(this.refs.textField.getDOMNode()).blur();
                ServiceFactory.getResourceService().saveResource("", this.state.resourceName);
            }
        }
    },
    onDivClick: function() {
        this.props.onClick(this.props.resource.id);
    },
    onCheckBoxChange: function(e) {
        this.props.onSelect(this.props.resource.id, this.refs.checkBox.getDOMNode().checked);
    },
    setHighlight: function(val) {
        this.setState({isHighlighted:val});
    },
    statics: {
        ENTER_KEY: "Enter",
        isValidResourceName: function(resourceName) {
            return (resourceName !== "");
        }
    }
});