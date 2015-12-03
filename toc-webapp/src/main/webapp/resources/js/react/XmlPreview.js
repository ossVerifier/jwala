/** @jsx React.DOM */
var XmlPreview = React.createClass({
    getInitialState: function() {
        return {content: this.props.children};
    },
    render: function() {
        var metaData = [{icon: "ui-icon-refresh", title: "Deploy", onClickCallback: this.deployCallback}];

        return <div ref="theContainer" className="xml-preview-container">
                   <RToolbar ref="theToolbar" className="toolbar-container" btnClassName="ui-button-text-only ui-button-height"
                             busyBtnClassName="busy-button" metaData={metaData}/>
                   <RTextPreview ref="textPreview">{this.state.content}</RTextPreview>
               </div>
    },
    deployCallback: function(ajaxProcessDoneCallback) {
        this.props.deployCallback(ajaxProcessDoneCallback);
    },
    refresh: function(content) {
        this.setState({content: content});
    },
    resize: function() {
        var textPreviewHeight = $(this.refs.theContainer.getDOMNode()).height() -
                                $(this.refs.theToolbar.getDOMNode()).height() - XmlPreview.SPLITTER_DISTANCE_FROM_TOOLBAR;
        $(this.refs.textPreview.getDOMNode()).css("height", textPreviewHeight);
    },
    statics: {
        SPLITTER_DISTANCE_FROM_TOOLBAR: 19
    }
})