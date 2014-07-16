/**
 * A basic button has it's onclick event attached by JQuery so that it can be used by
 * the DataTable component. The DataTable component renders a button to string which
 * nullifies React's event handling/binding hence the JQuery binding approach.
 *
 * Properties:
 *
 *  1. id - the button's id
 *  2. callback - method that gets called to do a specific action when the button is clicked
 *  3. label - the button's label i.e. Ok, Cancel, Save etc...
 *  4. isToggleBtn - if the button is a on/off or a switch button
 *  5. label2 - the label to display for the 2nd toggle state
 *  6. callback2 - the callback to execute for the 2nd toggle state
 *  7. className - container div css style
 *  8. customBtnClassName - for custom button look and feel
 *  9. clickedStateClassName - css style when button is in a "clicked" state
 * 10. clickedStateTimeout - duration in which to show the button clicked state style,
 *                           default is 10 seconds if this is not set
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

        var btnClassName = this.props.customBtnClassName;
        var spanClassName = "";
        if (this.props.customBtnClassName === undefined) {
            btnClassName = "ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only ui-state-hover";
            spanClassName = "ui-button-text";
        }

        return React.DOM.div({className: this.props.className},
                             React.DOM.button({id:this.props.id,
                                               type:"button",
                                               role:"button",
                                               ariaDisabled:false,
                                               className:btnClassName},
                                               React.DOM.span({className:spanClassName}, this.toggleStatus === 0 ?
                                                                                         this.props.label:
                                                                                         this.props.label2)));
    },
    handleClick: function(id) {

        if (this.props.clickedStateClassName !== undefined) {
            $("#" + this.props.id).attr("class", this.props.clickedStateClassName);
            var self = this;
            var timeout = (this.props.clickedStateTimeout === undefined ? 10000 : this.props.clickedStateTimeout);
            setTimeout(function(){$("#" + self.props.id).attr("class", self.props.customBtnClassName)}, timeout);
        }

        if (this.props.isToggleBtn) {
            if (this.toggleStatus === 0) {
                if (this.props.callback(id)) {
                    this.toggleStatus = 1;
                }
            } else {
                if (this.props.callback2(id)) {
                    this.toggleStatus = 0;
                }
            }

            $("#" + this.props.id).val(this.toggleStatus === 1 ?
                                       this.props.label2 :
                                       this.props.label);
        } else {
            this.props.callback(id);
        }
    }
});