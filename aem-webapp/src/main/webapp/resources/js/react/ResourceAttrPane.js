/** @jsx React.DOM */

/**
 * Shows the properties and values of a JVM, Web Server or Web Application.
 *
 * TODO: Unit tests.
 */
var ResourceAttrPane = React.createClass({
    getInitialState: function() {
        return {attributes: null};
    },
    render: function() {
        if (this.state.attributes === null ) {
            return <div className="attr-list ui-widget-content"><span>Please select a JVM, Web Server or Web Application...</span></div>;
        }

        var reactAttributeElements = [];
        for (attr in this.state.attributes) {
            if (typeof(this.state.attributes[attr]) !== "object") {
                reactAttributeElements.push(React.createElement(Attribute,
                                                    {key: attr, property: attr, value: this.state.attributes[attr]}));
            }
        }

        return <div className="attr-list ui-widget-content">
                    <table className="attr-table">
                        <thead>
                            <th>Property</th><th>Value</th>
                        </thead>
                        <tbody>{reactAttributeElements}</tbody>
                    </table>
               </div>;
    },
    showAttributes(data) {
        this.setState({attributes: data});
    }
})

var Attribute = React.createClass({
    render: function() {
        return <tr><td>{"${" + this.props.property + "}"}</td><td>{this.props.value}</td></tr>;
    }
});