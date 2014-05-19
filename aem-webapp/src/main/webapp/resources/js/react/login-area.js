/** @jsx React.DOM */
var LoginArea = React.createClass({
     render: function() {
        return <div className={this.props.className}>
                    <form method="post" action="/aem">
                        <br/>
                        <span className="title-1">Log In to</span><br/>
                        <span className="title-2">TOMCAT</span><br/>
                        <img src="public-resources/img/react/gear.gif"/>
                        <span className="title-2">PERATIONS</span><br/>
                        <span className="title-2">CENTER</span>
                        <br/>
                        <br/>
                        <TextBox id="user" name="user" className="input" hint="User Name" hintClassName="hint"/>
                        <br/>
                        <TextBox id="pwd" name="pwd" isPassword={true} className="input" hint="Password" hintClassName="hint"/>
                        <br/>
                        <input name="remember" type="checkbox"/><span className="remember">Remember me</span>
                        <br/>
                        <br/>
                        <input type="submit" value="Log In" />
                    </form>
               </div>
    }
});

$(document).ready(function(){
    React.renderComponent(<LoginArea className="login-area"/>, document.body);
});