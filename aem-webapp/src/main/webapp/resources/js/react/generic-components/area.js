/**
 * Area component.
 *
 * A page is suppose to be divided into areas e.g. header, main body, footer, ads etc...
 * The area component was designed with the intention of holding other components
 * in what can physically be described as an "area" while providing a way to manage
 * components as a singular group.
 *
 * An example of its intended usage is to enable, disable, or hide a group of components.
 *
 * This design specification is currently evolving as of March 2014 therefore it might be
 * removed in succeeding app releases if deemed unnecessary or a better framework/design
 * patter emerges.
 *
 * by Z003BPEJ
 */

var Area = React.createClass({
    render: function() {
        var theTheme = "area-" + this.props.theme;
        if (this.props.child !== undefined) {
            return React.DOM.div({className:theTheme}, this.props.child);
        } else {
            // This is going to be deprecated once JVM config has been refactored
            return React.DOM.div({className:theTheme});
        }
    },
    componentDidMount: function() {
        if (this.props.template !== undefined) {
            // This is going to be deprecated once JVM config has been refactored
            $(this.getDOMNode()).load(this.props.template);
        }
    }
});