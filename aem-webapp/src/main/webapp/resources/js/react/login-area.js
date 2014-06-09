/** @jsx React.DOM */
var LoginArea = React.createClass({
    render: function() {
        return <div className={this.props.className}>
            <form method="post" action="j_security_check">
                <br/>
                <MessageLabel msg={this.props.error} className="login-error-msg"/>
                <span className="title-2">TOMCAT</span><br/>
                <img src="public-resources/img/react/gear.gif"/>
                <span className="title-2">PERATIONS</span><br/>
                <span className="title-2">CENTER</span>
                <br/>
                <br/>
                <TextBox id="user" name="j_username" className="input" hint="User Name" hintClassName="hint"/>
                <br/>
                <TextBox id="pwd" name="j_password" isPassword={true} className="input" hint="Password" hintClassName="hint"/>
                <br/>
                <input type="submit" value="Log In" />
            </form>
        </div>
    }
});

$(document).ready(function(){
    var errorMessage = loginStatus === "error" ? "Your user name or password is incorrect.": "";
    React.renderComponent(<LoginArea className="login-area" error={errorMessage}/>, document.body);
});