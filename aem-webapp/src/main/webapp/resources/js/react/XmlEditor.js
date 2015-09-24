var XmlEditor = React.createClass({
    render: function() {
        var metaData = [{icon: "ui-icon-disk", title: "Save", onClickCallback: this.saveCallback},
			            {icon: "ui-icon-circle-arrow-n", title: "Upload", onClickCallback: this.props.uploadDialogCallback}];

        return React.createElement("div", {ref:"theContainer", className: this.props.className},
                   React.createElement(RToolbar, {ref: "theToolbar" , className: "toolbar-container",
                                                  btnClassName:"ui-button-text-only ui-button-height", metaData: metaData}),
                   React.createElement("textarea", {ref: "textArea", className: "xml-editor lined"}, this.props.content));
    },
    componentDidMount: function() {
        $(".lined").linedtextarea(
            {selectedLine: 1}
        );

        this.resize();
    },
    saveCallback: function() {
        this.props.saveCallback($(this.refs.textArea.getDOMNode()).val());
    },
    componentWillUpdate: function(nextProps, nextState) {
        // Since textarea has already been mutated via jquery-linedtextarea, we use jquery to update the content as well.
        $(this.refs.textArea.getDOMNode()).val(nextProps.content);
    },
    resize: function() {
        var textAreaHeight = $(this.refs.theContainer.getDOMNode()).height() - $(this.refs.theToolbar.getDOMNode()).height();
        $(".lines").css("height", textAreaHeight - 17);
        $(".xml-editor").css("height", textAreaHeight - 17);
        // move the scroll bar to re-populate the code lines
        $(".xml-editor").scrollTop($(".xml-editor").scrollTop() + 1);
        $(".xml-editor").scrollTop($(".xml-editor").scrollTop() - 1);
    },
    getText: function() {
        return $(this.refs.textArea.getDOMNode()).val();
    }
});