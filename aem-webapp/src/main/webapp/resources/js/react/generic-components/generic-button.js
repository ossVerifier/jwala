/**
 * A very basic button.
 *
 * Properties:
 *
 * 1. callback - method that gets called to do a specific action when the button is clicked
 * 2. label - the button's label i.e. Ok, Cancel, Save etc...
 *
 * Note: React's recommended way of doing component interaction is through callbacks
 */
var GenericButton = React.createClass({
    render: function() {
        return React.DOM.input({type:"button",
                                onClick:this.handleClick,
                                value:this.props.label});
    },
    handleClick: function() {
        this.props.callback();
    }
});