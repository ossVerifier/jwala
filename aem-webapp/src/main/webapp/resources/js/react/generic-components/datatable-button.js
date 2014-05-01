/**
 * A basic button has it's onclick event attached by JQuery so that it can be used by
 * the DataTable component. The DataTable component renders a button to string which
 * nullifies React's event handling/binding hence the JQuery binding approach.
 *
 * Properties:
 *
 * 1. id - the button's id
 * 2. callback - method that gets called to do a specific action when the button is clicked
 * 3. label - the button's label i.e. Ok, Cancel, Save etc...
 * 4. isToggleBtn - if the button is a on/off or a switch button
 * 5. label2 - the label to display for the 2nd toggle state
 * 6. callback2 - the callback to execute for the 2nd toggle state
 *
 */
var DataTableButton = React.createClass({
    /**
     * Note: Since this button was designed to be fully compatible with renderComponentToString
     * and renderComponentToStaticMarkup, we can't use React state management since if we so
     * component re-rendering after state change will result to the error
     * "Cannot read property 'firstChild' of undefined"
     */
    toggleStatus: 0,
    render: function() {
        $("#" + this.props.id).off("click");
        $("#" + this.props.id).on("click", this.handleClick.bind(this, this.props.itemId));
        return React.DOM.div({}, React.DOM.input({id:this.props.id,
                                                  type:"button",
                                                  value:this.toggleStatus === 0 ? this.props.label: this.props.label2}));
    },
    handleClick: function(id) {
        if (this.props.isToggleBtn === true) {
            if (this.toggleStatus === 0) {
                this.props.callback(id);
                this.toggleStatus = 1;
                $("#" + this.props.id).val(this.props.label2);
            } else {
                this.props.callback2(id);
                this.toggleStatus = 0;
                $("#" + this.props.id).val(this.props.label);
            }
        } else {
            this.props.callback(id);
        }
    }
});