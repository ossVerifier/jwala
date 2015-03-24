var AddEditDeleteResources = React.createClass({
    getInitialState: function() {
        var selectedResourceType = this.props.resourceTypes.length > 0 ? this.props.resourceTypes[0] : "";

        return {
            selectedResourceType: selectedResourceType
        }
    },
    render: function() {
        var resourceTypeDropDownArea = React.createElement("div", {className:"resource-type-dropdown"},
                                           React.createElement(ResourceTypeDropDown, {resourceTypes:this.props.resourceTypes,
                                                                                      onChange:this.onChangeResourceType}),
                                           React.createElement(RButton, {title:"Add Resource",
                                                                         className:"ui-state-default ui-corner-all default-icon-button-style",
                                                                         spanClassName:"ui-icon ui-icon-plus"}),
                                           React.createElement(RButton, {title:"Delete Resource",
                                                                         className:"ui-state-default ui-corner-all default-icon-button-style",
                                                                         spanClassName:"ui-icon ui-icon-trash"}));

        return React.createElement("div", {className:"container"}, resourceTypeDropDownArea);
    },
    onChangeResourceType: function(resourceType) {
        this.setState({selectedResourceType:resourceType});
    }
});

var ResourceTypeDropDown = React.createClass({
    render: function() {
        var options = [];
        this.props.resourceTypes.forEach(function(resourceType){
            var key = resourceType.name.replace(/\s/g, '');
            options.push(React.createElement("option", {key:key, value:resourceType.name}, resourceType.name));
        });
        return React.createElement("select", {ref:"select", className:"resource-type-dropdown", onChange:this.onChange}, options);
    },
    onChange: function(e) {
        this.props.onChange($(this.refs.select.getDOMNode()).find("option:selected").text());
    }
});