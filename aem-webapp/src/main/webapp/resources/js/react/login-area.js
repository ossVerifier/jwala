/** @jsx React.DOM */
var LoginArea = React.createClass({
     render: function() {
        var errorMsg = this.props.status === "error" ? "Username/password combination are incorrect." : "";
        return <div className={this.props.className}>
                    <form method="post" action="/aem">
                        <br/>
                        <MessageLabel msg={errorMsg} className="login-error-msg"/>
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
                        <input type="submit" value="Log In" />
                    </form>
               </div>
    }
});

$(document).ready(function(){

    /**
     * Get url query parameter
     * see http://www.designchemical.com/blog/index.php/jquery/8-useful-jquery-snippets-for-urls-querystrings/
     */
    var urlQueryParams = [], hash;
        var q = document.URL.split('?')[1];
        if(q != undefined){
            q = q.split('&');
            for(var i = 0; i < q.length; i++){
                hash = q[i].split('=');
                urlQueryParams.push(hash[1]);
                urlQueryParams[hash[0]] = hash[1];
            }
    }

    React.renderComponent(<LoginArea className="login-area" status={urlQueryParams['status']}/>, document.body);
});