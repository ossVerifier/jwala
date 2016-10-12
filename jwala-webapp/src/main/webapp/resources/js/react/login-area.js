/** @jsx React.DOM */
var LoginArea = React.createClass({
    getInitialState: function () {
        return {
            error: "", showLoginBusy: false
        }
    },

    render: function() {
         var loginBusyImg = this.state.showLoginBusy ? <img src="public-resources/img/busy-circular.gif"/> : null;
         return <div className={"LoginDialogBox " + this.props.className}>
                   <form id="logInForm">
                      <br/>
                      <span className="title">TOMCAT</span><br/>
                      <div className="gear-position">
                           <img src="public-resources/img/react/gear.gif"/>
                      </div>
                      <span className="title margin-left-17px">PERATIONS</span><br/>
                      <span className="title">CENTER</span>
                      <br/>
                      <br/>
                      <TextBox ref="userName" id="userName" name="userName" className="input" hint="User Name" hintClassName="hint"
                               onKeyPress={this.userNameTextKeyPress}/>
                      <br/>
                      <TextBox id="password" name="password" isPassword={true} className="input" hint="Password"
                              hintClassName="hint" onKeyPress={this.passwordTextKeyPress}/>
                      <div className="status">
                          {loginBusyImg}
                          <MessageLabel msg={this.state.error} className="login-error-msg"/>
                      </div>
                      <input type="button" value="Log In" onClick={this.logIn} />
                  </form>
              </div>
    },

    componentDidMount: function () {
        // Set initial focus on the user name text field
        $(this.refs.userName.getDOMNode()).children().focus();
    },
    userNameTextKeyPress: function () {
        if (event.charCode === 13) {
            return false; // prevent beep in IE8
        }
        return true;
    },
    passwordTextKeyPress: function (event) {
        if (event.charCode === 13) {
            this.logIn();
            return false;
        }
        return true;
    },
    logIn: function () {
        // TODO: Refactor to use dynamic state update to make this more inline with React.
        // NOTE! You might have to modify TextBox component for to Reactify this.
        if (!$("#userName").val().trim() || !$("#password").val()) {
            this.setState({ error: "User name and password are required." });
        } else {
            this.setState({showLoginBusy: true, error: ""});
        }
    },
    componentDidUpdate: function() {
        if (this.state.showLoginBusy) {
            // The timeout makes sure that post is done after the UI has been redrawn.
            // If login is just called directly, for some reason the page already goes into submission mode without the
            // busy indicator being displayed.
            var self = this;
            setTimeout(function(){userService.login($("#logInForm").serialize(), self.successCallback, self.errorCallback);}, 500);
        }
    },
    successCallback: function() {
        document.cookie = "userName=" + $("#userName").val(); // This is a quick fix to get the user id to the diagnose and resolve status.
        window.location = window.location.href.replace("/login", "");
    },

    errorCallback: function (e) {
        var state = {showLoginBusy: false};
        if (e !== undefined && e !== null && e.indexOf("error code 49") > -1) {
            state["error"] = "Your user name or password is incorrect.";
        } else {
            state["error"] = e;
        }
        this.setState(state);
    },
    statics: {
        isAdminRole: false
    }
});

$(document).ready(function () {
    var errorMessage = jwalaVars.loginStatus === "error" ? "Your user name or password is incorrect." : "";
    React.renderComponent(LoginArea({ className: "login-area", error: errorMessage }), document.body);
});