/** @jsx React.DOM */
var GroupForm = React.createClass({
    render: function() {
        return  <form action={this.props.url}>
                    <p>change again 2</p>
                    <input type="hidden" />
                    <table>
                        <tr>
                            <td>Name</td>
                        </tr>
                        <tr>
                            <td>
                                <input/>
                            </td>
                        </tr>
                    </table>
                </form>
    }
});