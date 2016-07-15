/**
 * A react component that encapsulates and manages a CodeMirror object (the template editor).
 * http://codemirror.net/
 */
var CodeMirrorComponent = React.createClass({
    codeMirror: null,
    getInitialState: function() {
        return {data: null};
    },
    render: function() {
        var metaData = [{icon: "ui-icon-disk", title: "Save", onClickCallback: this.saveCallback}];
        return React.createElement("div", {ref:"theContainer", className: this.props.className},
                   React.createElement(RToolbar, {ref: "theToolbar" , className: "toolbar-container",
                                                  btnClassName:"ui-button-text-only ui-button-height", metaData: metaData}),
                   React.createElement("div", {ref: "codeMirrorHost"}));
    },
    componentDidMount: function() {
        var val = this.props.content ? this.props.content : "";
        this.codeMirror = CodeMirror(this.refs.codeMirrorHost.getDOMNode(), {value: val, lineNumbers: true,
                                     mode:  "xml"});
        this.state.data = this.codeMirror.getValue();
        this.codeMirror.on("change", this.props.onChange);
        this.resize();
    },
    componentWillUpdate: function(nextProps, nextState) {
        this.setData(nextProps.content);
    },
    saveCallback: function() {
        this.props.saveCallback(this.codeMirror.getValue());
    },
    getText: function() {
        return this.codeMirror.getValue();
    },
    isContentChanged: function() {
        return this.state.data !== this.getText()
    },
    resize: function() {
        var textAreaHeight = $(this.refs.theContainer.getDOMNode()).height() - $(this.refs.theToolbar.getDOMNode()).height() -
                    CodeMirrorComponent.SPLITTER_DISTANCE_FROM_TOOLBAR;
        console.log(textAreaHeight);
        $(".CodeMirror.cm-s-default").css("height", textAreaHeight);
    },
    setData: function(data) {
        this.codeMirror.off("change", this.props.onChange);
        this.codeMirror.setValue(data);
        this.codeMirror.on("change", this.props.onChange);
        this.state.data = this.codeMirror.getValue();
    },
    statics: {
        SPLITTER_DISTANCE_FROM_TOOLBAR: 19
    }
});