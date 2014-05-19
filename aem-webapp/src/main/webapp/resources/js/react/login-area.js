/** @jsx React.DOM */
var LoginArea = React.createClass({
     render: function() {
        return <div className={this.props.className}>
                    <br/>
                    <span className="title-1">Log In to</span><br/>
                    <span className="title-2">TOMCAT</span><br/>
                    <img src="public-resources/img/react/gear.gif"/>
                    <span className="title-2">PERATIONS</span><br/>
                    <span className="title-2">CENTER</span>
                    <br/>
                    <br/>
                    <TextBox hint="User Name" hintClassName="hint"/>
                    <br/>
                    <TextBox isPassword={true} hint="Password" hintClassName="hint"/>
                    <br/>
                    <input type="checkbox"/><span className="remember">Remember me</span>
                    <br/>
                    <br/>
                    <input type="button" value="Log In" />
               </div>
    }
});

$(document).ready(function(){
    React.renderComponent(<LoginArea className="login-area"/>, document.body);
});