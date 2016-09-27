/** @jsx React.DOM */
var MetaDataPreview = React.createClass({
    getInitialState: function() {
        return {content: this.props.children};
    },
    render: function() {
        return <div ref="theContainer" className="xml-preview-container">
                   <RTextPreview ref="textPreview">{this.state.content}</RTextPreview>
               </div>
    },
    refresh: function(content) {
        this.setState({content: content});
    },
    resize: function() {
        var textPreviewHeight = $(this.refs.theContainer.getDOMNode()).height() - MetaDataPreview.SPLITTER_DISTANCE_FROM_PREVIEW_COMPONENT;
        $(this.refs.textPreview.getDOMNode()).css("height", textPreviewHeight);
    },
    statics: {
        SPLITTER_DISTANCE_FROM_PREVIEW_COMPONENT: 19
    }
})