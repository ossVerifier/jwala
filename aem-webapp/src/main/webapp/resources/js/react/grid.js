/** @jsx React.DOM */

var Grid = React.createClass({

    render: function() {
        return <table>
                    <tr>
                        <td><input type='button' value='-'/></td><td colSpan='4'>Group 1</td>
                    </tr>
                    <tr>
                        <td></td><td><input type='button' value='-'/></td><td>CTO_HC_SRN012_4</td><td>SRN012</td><td>8080</td>
                    </tr>
                    <tr>
                        <td></td><td></td>
                        <td colSpan='3'>
                            <table>
                                <th>Web Apps</th><th>Directory</th><th>Operation</th>
                                <tr>
                                    <td>/analytics</td><td>[Directory]</td><td><input type='button' value='Undeploy' /></td>
                                </tr>
                                <tr>
                                    <td>/hello-world</td><td>[Directory]</td><td><input type='button' value='Undeploy' /></td>
                                </tr>
                            </table>
                            <table>
                                <th>Resources</th><th>Type</th><th><input type='button' value='Add' /><input type='button' value='Synchronize' /></th>
                                    <tr>
                                        <td><link href='#'>JAVA_OPTS</link></td><td>Environmental Variable</td><td></td>
                                    </tr>
                                    <tr>
                                        <td><link href='#'>CATALINA</link></td><td>Environmental Variable</td><td></td>
                                    </tr>
                                    <tr>
                                        <td><link href='#'>JMS/ConnectionFactory</link></td><td>JMS Connection Factory</td><td></td>
                                    </tr>
                                    <tr>
                                        <td><link href='#'>JMS/XaConnectionFactory</link></td><td>JMS Xa Connection Factory</td><td></td>
                                    </tr>
                                    <tr>
                                        <td><link href='#'>LDAP</link></td><td>LDAP Realm</td><td></td>
                                    </tr>
                            </table>
                        </td>
                    </tr>
               </table>;
    }

});