/**
 * A list box component.
 *
 * TODO: Unit tests
 *
 */
var RListBox = React.createClass({
    getInitialState: function() {
        return {selectedValue: null};
    },
    render: function() {
        return React.createElement("ul", {className: "rlistbox no-border ui-menu ui-widget ui-widget-content"}, this.createResourceList());
    },
    createResourceList: function() {
        var self = this;
        var resourceList = [];
        var i = 0;
        this.props.options.forEach(function(option) {
            resourceList.push(React.createElement(Option, {key: i++,
                                                           value: option.value,
                                                           selectedValue: self.state.selectedValue,
                                                           label: option.label,
                                                           onClick: self.onOptionClick}));
        });
        return resourceList;
    },
    componentWillReceiveProps: function(nextProps) {
      this.setState({selectedValue: null});
    },
    onOptionClick: function(value) {
        if (this.state.selectedValue !== value) {
            if (this.props.selectCallback !== undefined) {
                if (!this.props.selectCallback(value)) {
                    return;
                }
            }

            this.setState({selectedValue: value});
        }
    },
    getSelectedValue: function() {
        return this.state.selectedValue;
    }
});

var Option = React.createClass({
    getInitialState: function() {
        return {mouseOver: false};
    },
    isSelected: function() {
        return this.props.selectedValue === this.props.value;
    },
    render: function() {
        var stateClassName = "";
        if (this.isSelected()) {
            stateClassName = " ui-state-active";
        } else if (this.state.mouseOver) {
            stateClassName = " ui-state-focus";
        }

        var className = "ui-menu-item " + stateClassName;

        return React.createElement("li", {className: className,
                                          onClick: this.props.onClick.bind(this, this.props.value),
                                          onMouseOver: this.onMouseOver,
                                          onMouseOut: this.onMouseOut}, this.props.label);
    },
    onMouseOver: function() {
        this.setState({mouseOver: true});
    },
    onMouseOut: function() {
        this.setState({mouseOver: false});
    }
});
