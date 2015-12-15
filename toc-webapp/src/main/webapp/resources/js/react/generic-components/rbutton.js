/**
 * A button meant purely to be used in a React way (e.g. don't use by rendering this component to string then using jQuery to update the DOM) since it maintains state.
 * This component will eventually replace the GenericButton.
 *
 * Properties
 *
 * 1. className - the class applied to the button. If not specified jQuery UI theme classes for regular buttons are used.
 * 2. hoverClassName - the hover class applied to the button on mouse over. If not specified jQuery UI theme classes for regular buttons are used.
 * 3. spanClassName - the class applied to the span component of the button that contains the label. If not specified the jQuery UI theme classes for regular buttons are used.
 * 4. onClick - the callback that is executed when the button is clicked.
 * 5. label - the label of the button.
 * 6. title - the title of the button that is shown in a tooltip when the user hovers the mouse pointer over it.
 * 7. busyClassName - the class that shows a spinner or busy indicator.
 */
var RButton = React.createClass({
    getInitialState: function() {
        return {
            hover: false,
            busy: false // Only used when busyClassName is defined.
        }
    },
    render: function() {
        var className;
        var spanClassName;

        if (this.state.busy && this.props.busyClassName !== undefined) {
            className = this.props.busyClassName;
            spanClassName = "";
        } else {
            className = (this.props.className !== undefined) ? this.props.className : "ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only";
            spanClassName = (this.props.spanClassName !== undefined) ? this.props.spanClassName : "ui-button-text";
        }

        var theHoverClassName = (this.props.hoverClassName !== undefined) ? this.props.hoverClassName : "ui-state-hover";
        var hoverClassName = this.state.hover ? theHoverClassName : "";
        return React.DOM.button({className: className + " " + hoverClassName,
                                 title:this.props.title,
                                 type:"button",
                                 role:"button",
                                 ariaDisabled:false,
                                 onMouseOver:this.mouseOverHandler,
                                 onMouseOut:this.mouseOutHandler},
                                 React.DOM.span({className:spanClassName}, this.props.label));
    },
    componentDidMount: function() {
        // This is to fix the bug wherein the containing parent's click event is fired first when defining the onClick
        // event via react property eg {onClick: clickHandler}.
        // TODO: Define the click event handler as a React property when the table which contains it is already written in React.
        // TODO: Check with React if this is a known issue, see if it is resolved in the latest version. If not try to resolve and contribute.
        $(this.getDOMNode()).click(this.handleClick);
    },
    handleClick: function(e) {
        if (!this.state.busy) {
            if (this.props.busyClassName !== undefined) {
                this.setState({busy: true});
            }
            this.props.onClick(this.doneCallback);
        }
        return false;
    },
    mouseOverHandler: function() {
        if (!this.state.busy) {
            this.setState({hover:true});
        }
    },
    mouseOutHandler: function() {
        this.setState({hover:false});
    },

    /**
     * Used in conjunction with busy state to stop the spinner/busy indicator.
     * This method gets passed to the onClick event and in turn should be called by the calling
     * component to remove the spinner/busy indicator.
     */
    doneCallback: function() {
        this.setState({busy: false});
    }
});