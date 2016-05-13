var XmlEditor = React.createClass({
    render: function() {
        var metaData = [{icon: "ui-icon-disk", title: "Save", onClickCallback: this.saveCallback},
			            {icon: "ui-icon-circle-arrow-n", title: "Upload", onClickCallback: this.props.uploadDialogCallback}];

        return React.createElement("div", {ref:"theContainer", className: this.props.className},
                   React.createElement(RToolbar, {ref: "theToolbar" , className: "toolbar-container",
                                                  btnClassName:"ui-button-text-only ui-button-height", metaData: metaData}),
                   React.createElement("textarea", {ref: "textArea", className: "xml-editor lined",
                                                    onChange: this.props.onChange}, this.props.content));
    },
    componentDidMount: function() {

//        !!! This is causing the browser to crash when scrolling up and down when pulling the slider !!!
//        TODO: Fix this!!!
//        $(".lined").linedtextarea(
//            {selectedLine: 1}
//        );
//
        // For some reason when the content is placed in the textarea, the content does not match the val from the
        // DOM probably because of some hidden characters that got converted/stripped when placed in the textArea.
        // So we store the value in a variable to compare later.
        this.origVal = $(this.refs.textArea.getDOMNode()).val();
    },
    origVal: null,
    isContentChanged: function() {
        return this.origVal !== $(this.refs.textArea.getDOMNode()).val();
    },
    saveCallback: function() {
        this.props.saveCallback($(this.refs.textArea.getDOMNode()).val());

        // TODO: Verify what happens when save is successful and if it is not. We need to know so we can set origVal.
    },
    componentWillUpdate: function(nextProps, nextState) {
        // Since textarea has already been mutated via jquery-linedtextarea, we use jquery to update the content as well.
        $(this.refs.textArea.getDOMNode()).val(nextProps.content);

        // For some reason when the content is placed in the textarea, the content does not match the val from the
        // DOM probably because of some hidden characters that got converted/stripped when placed in the textArea.
        // So we store the value in a variable to compare later.
        this.origVal = $(this.refs.textArea.getDOMNode()).val();
    },
    resize: function() {
        var textAreaHeight = $(this.refs.theContainer.getDOMNode()).height() -
            $(this.refs.theToolbar.getDOMNode()).height() - XmlEditor.SPLITTER_DISTANCE_FROM_TOOLBAR;
        $(".lines").css("height", textAreaHeight);
        $(".xml-editor").css("height", textAreaHeight);
        // move the scroll bar to re-populate the code lines
        $(".xml-editor").scrollTop($(".xml-editor").scrollTop() + 1);
        $(".xml-editor").scrollTop($(".xml-editor").scrollTop() - 1);
    },
    getText: function() {
        return $(this.refs.textArea.getDOMNode()).val();
    },
    statics: {
        SPLITTER_DISTANCE_FROM_TOOLBAR: 19
    }
});