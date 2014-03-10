/**
 * This is a generic "form" modal dialog box component.
 *
 * How it works:
 *
 * This component can pass data to the server via a HTML template.
 * The HTML template must contain named data elements so that it
 * can serialize form data and convert it to JSON for submission.
 * Form submission is done via AJAX using jQuery.
 *
 * This component has the following properties:
 *
 * 1. theme - a css class which defines the style of this modal dialog box.
 * 2. title - the modal dialog box's header title.
 * 3. template - an html template file that contains form data.
 * 4. action - the url or action to which the request is to be made to.
 *
 * Example Usage:
 *
 *     ModalFormDialog({theme:"my-theme", title:"Edit Group", action:"v1/groups", template:"edit-group.html"});
 *
 * by Z003BPEJ
 */

var ModalFormDialog = React.createClass({
    getInitialState: function() {
        THEME_PREFIX = "modalFormDialog";
        modalForm = React.DOM.form({action:this.props.action});
        return {
          show: false
        };
    },
    show: function() {
        this.setState({show: true});
    },
    hide: function() {
        /**
         * window.focus() is prevents following error in IE8:
         * Can't move focus to the control because it is invisible, not enabled,
         * or of a type that does not accept the focus.
         */
         window.focus();

        this.setState({show: false});
    },
    render: function() {
        var rootDivStyle = {display:"none"};
        if (this.state.show) {
            rootDivStyle = {display:""};
        }

        var btnDivStyle = {textAlign:"right", margin:"2,2,2,2"};

        return React.DOM.div({className:THEME_PREFIX + "-" + this.props.theme, style:rootDivStyle},
               React.DOM.div({className:THEME_PREFIX + "-" + this.props.theme + "-header"}, this.props.title),
               React.DOM.div({className:THEME_PREFIX + "-" + this.props.theme + "-content"}, modalForm),
               React.DOM.div({style:btnDivStyle},
                    React.DOM.input({type:"button", value:"Ok", onClick:this.okClick}),
                    React.DOM.input({type:"button", value:"Cancel", onClick:this.cancelClick})
                )
               );
    },
    componentDidMount: function() {
        $(modalForm.getDOMNode()).load(this.props.template);
    },
    okClick: function() {
        var theModalFormDialog = this;

        // The submit callback
        $(modalForm.getDOMNode()).one("submit", function(e) {
            var postData = serializedFormToJson($(this).serializeArray());
            var formURL = $(this).attr("action");
                $.ajax({
                url : formURL,
                type: "POST",
                data: postData,
                contentType: "application/json",
                dataType: "json",
                success:function(data, textStatus, jqXHR) {
                    theModalFormDialog.hide();
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    // TODO: Display error message in another modal dialog.
                    alert(textStatus);
                }
            });
            e.preventDefault(); // stop the default action
        });

        $(modalForm.getDOMNode()).submit();

    },
    cancelClick: function() {
        this.hide();
    }

});

/**
 * Form data converted to a serialized array will have a constant name-value pair i.e.
 * [{name:value}...] therefore using JSON stringify on it will not produce
 * the desired JSON format thus the need for the function serializedFormToJson
 * to correctly build the JSON data.
 *
 * Example of form data converted to a serialized array then "stringified"
 *
 * [{"name":"jvmName","value":"default-jvm-name"},{"name":"hostName","value":"default-host-name"}]
 *
 * NOTE: This is not what we want! It should be like this:
 *
 * [{"jvmName":"default-jvm-name"},{"hostName":"default-host-name"}]
 *
 */
var serializedFormToJson = function(serializedArray) {
    var json = {};
    $.each(serializedArray, function() {
        json[this.name] = this.value;
    });
    return JSON.stringify(json);
}