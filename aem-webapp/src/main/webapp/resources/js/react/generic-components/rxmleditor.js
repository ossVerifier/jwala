/**
 * Created by Z003BPEJ on 12/23/2014.
 */
var RXmlEditor = React.createClass({
    colorClassName: "",
    getInitialState: function() {
        var content = this.props.content === undefined ? [null] : this.props.content.split('');
        return {onFocus:false,
                content:content,
                cursorIdx:0,
                cursorLineIdx:0,
                ctrlDown:false,
                shiftDown:false,
                selectRange:null,
                clipboard:[]}
    },
    shouldComponentUpdate: function(nextProps, nextState) {
        if ((this.state.ctrlDown !== nextState.ctrlDown) || (this.state.shiftDown !== nextState.shiftDown)) {
            return false;
        }
        return true;
    },
    refresh: function(content, cursorPosition) {
        if (content !== undefined) {
            var contentArray = content === undefined ? [null] : content.split('');
            if (cursorPosition === undefined) {
                cursorPosition = 0;
            }
            this.setState({content:contentArray, cursorIdx:cursorPosition});
        }
    },
    render: function() {
        var elements = [];

        elements = this.formatContent(this.state.content);

        return React.createElement("div", {className:"xml-editor",
                                           tabIndex:0,
                                           onKeyPress:this.keyPressHandler,
                                           onKeyDown:this.keyDownHandler,
                                           onKeyUp:this.keyUpHandler,
                                           onFocus:this.onFocusHandler,
                                           onBlur:this.onBlurHandler, onPaste:this.onPasteHandler}, elements);
    },

    onPasteHandler: function(e) {
        var self = this;

        var pastedText = undefined;
        if (window.clipboardData && window.clipboardData.getData) { // IE
            pastedText = window.clipboardData.getData('Text');
        } else if (e.clipboardData && e.clipboardData.getData) {
            pastedText = e.clipboardData.getData('text/plain');
        }

        var idx = this.state.cursorIdx;
        if (pastedText !== undefined && pastedText !== "") {
            pastedText.split('').forEach(function(char){
                self.state.content.splice(++idx, 0, char);
            });
            this.setState({cursorIdx:idx, cursorLineIdx:RXmlEditor.getCursorLineIdx(this.state.content, idx)});
        }

        return false; // Prevent the default handler from running.
    },

    /**
     * Gets an array of lower and upper indexes that defines a comment range.
     */
    getCommentRangeArray: function(content) {
        var rangeArr = [];
        var lower = 0;
        var upper;
        var str = " " + content.join(""); // Content has a null character at the beginning which is omitted by join
                                          // thus the need to put a space in front of str to make str and content
                                          // lengths and indexes the same.
        var startIdx = 0;
        var UPPER_RANGE_OFFSET = 2;

        while (lower > -1) {
            lower = str.indexOf("<!--", startIdx);
            upper = str.indexOf("-->", lower);
            if (lower > -1) {
                rangeArr.push({lower:lower, upper: upper === -1 ? str.length - 1 : upper + UPPER_RANGE_OFFSET});
            }

            if (lower < upper) {
                startIdx = upper + UPPER_RANGE_OFFSET + 1;
            } else {
                break;
            }
        }
        return rangeArr;
    },
    /**
     * Checks if the specified index is in a comment tag.
     *
     * @param commentRangeArray - contains the lower and upper indexes that defines a comment range.
     * @param idx - the index
     */
    isInCommentTag: function(commentRangeArray, idx) {
         for (var i in commentRangeArray) {
            if ((idx >= commentRangeArray[i].lower) && (idx <= commentRangeArray[i].upper)) {
                return true;
            }
        };
        return false;
    },

    /**
     * Creates Characters with their corresponding classNames.
     */
    formatContent: function(content) {
        var self = this;
        var inElement = false;
        var inQuote = false;
        var elements = [];
        var idx = 0;

        var commentRangeArray = this.getCommentRangeArray(this.state.content);

        var lineCtr = 1;

        content.forEach(function(ch) {


            if (ch === "\n" || ch === null) {
                if (ch === "\n") {
                    elements.push(React.createElement("br"));
                }

                elements.push(React.createElement(Character, {parent:self,
                                                              className:"lineNumber",
                                                              char:lineCtr++,
                                                              idx:idx++,
                                                              charCustomClassName:"lineNumberChar",
                                                              customCursorClassName:"lineNumberCursor"}));
            } else {

                if (self.isInCommentTag(commentRangeArray, idx)) {
                    self.colorClassName = "comment";
                } else {
                    if (!inQuote) {
                        if (ch === "<") {
                            inElement = true;
                        } else if (ch === "%") { // TODO: Make a better way to identify groovy code inserts
                            // Don't format groovy code
                            inElement = !inElement;
                        }

                        if (inElement) {
                            if (ch === "<" || ch === "/") {
                                self.colorClassName = "element";
                            } else if (ch === " ") {
                                self.colorClassName = "attribute";
                            } else if (ch === ">") {
                               self.colorClassName = "element";
                               inElement = false;
                            }
                        } else {
                            self.colorClassName = "text";
                        }
                    }

                    if (inElement) {
                        if (ch === "'" || ch === '"') {
                            self.colorClassName = "value";
                            inQuote = !inQuote;
                        }
                    }
                }

                var selectedClassName = "";
                if (self.state.selectRange !== null) {
                    if ((idx >= self.state.selectRange.lower && idx <= self.state.selectRange.upper) ||
                        (idx >= self.state.selectRange.upper && idx <= self.state.selectRange.lower)){
                        selectedClassName = " selected";
                    }
                }

                elements.push(React.createElement(Character, {parent:self,
                                                              className:self.colorClassName + selectedClassName,
                                                              char:ch,
                                                              idx:idx++}));

            }
         });

        return elements;
    },
    /**
     * Checks if cursor is at the end of the file.
     */
    eof: function() {
        return (this.state.cursorIdx === (this.state.content.length - 1));
    },
    /**
     * Checks if everything is selected.
     */
    isAllSelected: function() {
        return (this.state.selectRange !== null &&
                this.state.selectRange.lower === 0 &&
                this.state.selectRange.upper === (this.state.content.length - 1));
    },
    /**
     * Check if a range of characters are selected.
     */
    hasSelection: function() {
        return this.state.selectRange !== null;
    },

    getSelectionRange: function() {
        return Math.abs(this.state.selectRange.upper - this.state.selectRange.lower) + 1;
    },

    getSelectionLowestIdx: function() {
        if (this.state.selectRange.upper > this.state.selectRange.lower) {
            return this.state.selectRange.lower;
        }
        return this.state.selectRange.upper;
    },

    /**
     * Handler for the key press event.
     */
    keyPressHandler: function(e) {
        var nextCursorIdx;

        if (this.hasSelection()) {
            nextCursorIdx = this.getSelectionLowestIdx();
            this.state.content.splice(nextCursorIdx, this.getSelectionRange(), e.key);
        } else {
            nextCursorIdx = this.state.cursorIdx + 1;
            if (this.eof()) {
                this.state.content.push(e.key);
            } else {
                this.state.content.splice(nextCursorIdx, 0, e.key); // insert after the cursor
            }

            try {
                $.parseXML(this.state.content.join(""));
            } catch (e) {
                console.log(e);
            }

        }

        var cursorLineIdx = RXmlEditor.getCursorLineIdx(this.state.content, nextCursorIdx);
        this.setState({cursorIdx:nextCursorIdx,
                       cursorLineIdx:cursorLineIdx,
                       selectRange:null});
    },
    /**
     * Handler for the key down event (e.g. ENTER, ARROW DOWN, BACKSPACE etc...)
     */
    keyDownHandler: function(e) {
        console.log("key down");
        if (this.state.ctrlDown) {
            return this.processCtrlPlusKeyCode(e.keyCode);
        } else if (this.state.shiftDown) {
            return this.processShiftPlusKeyCode(e.keyCode);
        } else {
            return this.processKeyCode(e.keyCode);
        }
    },
    /**
     * Process pure keys. Pure meaning no key inputted directly without CTRL, SHIFT etc...
     */
    processKeyCode: function(keyCode) {

        switch (keyCode) {
            case RXmlEditor.key.BACK_SPACE:
                if (this.hasSelection()) {
                    this.delSelectedChars();
                } else {
                    if (this.state.cursorIdx > 0) {
                        this.state.content.splice(this.state.cursorIdx, 1);
                        this.setState({cursorIdx:this.state.cursorIdx - 1});
                    }
                }
                return false;
            case RXmlEditor.key.LEFT_ARROW:
                if (this.state.cursorIdx > 0) {
                    var cursorLineIdx = RXmlEditor.getCursorLineIdx(this.state.content, this.state.cursorIdx - 1);
                    this.setState({cursorIdx: this.state.cursorIdx - 1, cursorLineIdx:cursorLineIdx, selectRange:null});
                } else {
                    this.setState({selectRange:null});
                }
                return false;
            case RXmlEditor.key.RIGHT_ARROW:
                if (this.state.cursorIdx < (this.state.content.length - 1)) {
                    var cursorLineIdx = RXmlEditor.getCursorLineIdx(this.state.content, this.state.cursorIdx + 1);
                    this.setState({cursorIdx: this.state.cursorIdx + 1, cursorLineIdx:cursorLineIdx, selectRange:null});
                } else {
                    this.setState({selectRange:null});
                }
                return false;
            case RXmlEditor.key.DELETE:

                if (this.hasSelection()) {
                    this.delSelectedChars();
                } else {
                    if (this.state.cursorIdx < (this.state.content.length - 1)) {
                        this.state.content.splice(this.state.cursorIdx + 1, 1);
                        this.setState({cursorIdx:this.state.cursorIdx});
                    }
                }

                return false;
            case RXmlEditor.key.ENTER:
                var nextCursorIdx = this.state.cursorIdx + 1;
                if (this.eof()) {
                    this.state.content.push("\n");
                } else {
                    this.state.content.splice(nextCursorIdx, 0, "\n"); // insert after the cursor
                }
                this.setState({cursorIdx:nextCursorIdx});
                return false;
            case RXmlEditor.key.UP_ARROW:
                var cursorIdxBeforeLineChar = this.state.content.lastIndexOf("\n", this.state.cursorIdx);

                if (cursorIdxBeforeLineChar > -1) {
                    --cursorIdxBeforeLineChar;
                    var prevLineCharIdx = RXmlEditor.getLineCharIndexReverse(this.state.content, cursorIdxBeforeLineChar);
                    var lineLength =  cursorIdxBeforeLineChar - prevLineCharIdx;
                    var cursorIdx = cursorIdxBeforeLineChar;
                    if (lineLength >= this.state.cursorLineIdx) {
                        cursorIdx = prevLineCharIdx + this.state.cursorLineIdx;
                    }

                    this.setState({cursorIdx:cursorIdx, selectRange:null});
                }
                return false;

            case RXmlEditor.key.DOWN_ARROW:
                if (this.state.cursorIdx < this.state.content.length) {
                    var nextCursorIdx = this.state.content.indexOf("\n", this.state.cursorIdx + 1);

                    if (nextCursorIdx > -1) {

                        var nextLineLength = RXmlEditor.getNextLineLength(this.state.content, nextCursorIdx);

                        var cursorLineIdx;
                        if (this.state.cursorLineIdx > nextLineLength) {
                            cursorLineIdx = nextLineLength;
                        } else {
                            cursorLineIdx = this.state.cursorLineIdx;
                        }

                        this.setState({cursorIdx:nextCursorIdx + cursorLineIdx, selectRange:null});
                    }
                }
                return false;

            case RXmlEditor.key.TAB:
                var nextCursorIdx = this.state.cursorIdx;
                for (var i = 1; i <= RXmlEditor.NUMBER_OF_SPACES_PER_TAB; i++) {
                    this.state.content.splice(++nextCursorIdx, 0, " ");
                }
                var cursorLineIdx = RXmlEditor.getCursorLineIdx(this.state.content, nextCursorIdx);

                this.setState({content:this.state.content,
                               cursorIdx:nextCursorIdx,
                               cursorLineIdx:cursorLineIdx});
                return false;

            case RXmlEditor.key.CTRL:
                if (!this.state.ctrlDown) {
                    this.setState({ctrlDown:true});
                }
                return false;

            case RXmlEditor.key.ESC:
                this.setState({selectRange:null});
                return false;

            case RXmlEditor.key.SHIFT:
                if (!this.state.shiftDown) {
                    this.setState({shiftDown:true});
                }
                return false;
        }
        return true;
    },

    /**
     * Delete selected characters.
     */
    delSelectedChars: function() {
        var idx1;
        var idx2;
        var nextCursorIdx;
        if (this.hasSelection()) {
            if (this.state.selectRange.lower < this.state.selectRange.upper) {
                idx1 = this.state.selectRange.lower;
                idx2 = this.state.selectRange.upper + 1;
            } else {
                idx1 = this.state.selectRange.upper;
                idx2 = this.state.selectRange.lower + 1;
            }
            nextCursorIdx = idx1 - 1;
            this.state.content.splice(idx1, idx2 - idx1);

            this.setState({cursorIdx:nextCursorIdx,
                           cursorLineIdx:RXmlEditor.getCursorLineIdx(this.state.content, nextCursorIdx),
                           selectRange:null});
        }
    },

    /**
     * Copy selected characters to clipboard.
     */
    copyContentSelectionToClipboard:function() {
        var clipboard;
        if (this.hasSelection()) {
            if (this.state.selectRange.lower < this.state.selectRange.upper) {
                clipboard = this.state.content.slice(this.state.selectRange.lower, this.state.selectRange.upper + 1);
            } else {
                clipboard = this.state.content.slice(this.state.selectRange.upper, this.state.selectRange.lower + 1);
            }
            this.setState({clipboard:clipboard});
        }
    },
    /**
     * Copy selected characters to clipboard and deletes the selected characters.
     */
    cutContentSelectionToClipboard:function() {
        var clipboard;
        var nextCursorIdx;
        if (this.hasSelection()) {
            if (this.state.selectRange.lower < this.state.selectRange.upper) {
                // Note: splicing the content state directly is an anti-pattern since were supposed to update
                // the state using setState. The "splice" command changes the content.
                // TODO: Refactor due to the above notes:
                clipboard = this.state.content.splice(this.state.selectRange.lower, this.state.selectRange.upper);
                nextCursorIdx = this.state.selectRange.lower - 1;
            } else {
                // TODO: Refactor, please see above notes re anti-pattern.
                clipboard = this.state.content.splice(this.state.selectRange.upper, this.state.selectRange.lower);
                nextCursorIdx = this.state.selectRange.upper - 1;
            }
            this.setState({clipboard:clipboard,
                           cursorIdx:nextCursorIdx,
                           cursorLineIdx:RXmlEditor.getCursorLineIdx(this.state.content, nextCursorIdx)});
        }
    },
    /**
     * Checks if clipboard is empty or not.
     */
    isClipboardEmpty: function() {
        return (this.state.clipboard.length === 0);
    },
    /**
     * Process CTRL+ keys e.g. CTRL+A etc...
     *
     * @param keyCode the key code to process
     */
    processCtrlPlusKeyCode: function(keyCode) {
        switch (keyCode) {
            case RXmlEditor.key.A:
                this.setState({selectRange:{lower:1, upper:this.state.content.length - 1}});
                return false;
            case RXmlEditor.key.C:
                this.copyContentSelectionToClipboard();
                return false;
            case RXmlEditor.key.X:
                this.cutContentSelectionToClipboard();
                return false;
        }
        return true;
    },
    /**
     * Process SHIFT+ keys e.g. SHIFT+{RIGHT ARROW} etc...
     *
     * @param keyCode the key code to process
     */
    processShiftPlusKeyCode: function(keyCode) {
        switch (keyCode) {
            case RXmlEditor.key.RIGHT_ARROW:

                if (this.state.cursorIdx < (this.state.content.length - 1)) {
                    var lower;
                    var upper;
                    if (this.state.selectRange === null  || this.isAllSelected()) {
                        lower = this.state.cursorIdx + 1;
                        upper = lower;
                    } else {
                        lower = this.state.selectRange.lower;
                        upper = this.state.selectRange.upper + 1;

                        if (((lower + 1) === upper) && (this.state.cursorIdx === (lower - 1))) {
                            this.setState({selectRange:null, cursorIdx:this.state.cursorIdx +  1});
                            return false;
                        }
                    }

                    this.setState({selectRange:{lower:lower, upper:upper}, cursorIdx:this.state.cursorIdx + 1});
                }
                return false;

            case RXmlEditor.key.LEFT_ARROW:
                if (this.state.cursorIdx > 0) {
                    var lower;
                    var upper;
                    if (this.state.selectRange === null || this.isAllSelected()) {
                        lower = this.state.cursorIdx;
                        upper = lower;
                    } else {
                        lower = this.state.selectRange.lower;
                        upper = this.state.selectRange.upper - 1;

                        if (((lower - 1) === upper) && (this.state.cursorIdx === lower)) {
                            this.setState({selectRange:null, cursorIdx:this.state.cursorIdx -  1});
                            return false;
                        }
                    }

                    this.setState({selectRange:{lower:lower, upper:upper}, cursorIdx:this.state.cursorIdx -  1});
                }
                return false;

        }
        return true;
    },
    keyUpHandler: function(e) {
        if (e.keyCode === RXmlEditor.key.CTRL) {
            this.setState({ctrlDown:false});
        } else if (e.keyCode === RXmlEditor.key.SHIFT) {
            this.setState({shiftDown:false});
        }
    },
    onFocusHandler: function() {
        this.render();
    },
    statics: {
        NUMBER_OF_SPACES_PER_TAB: 4,
        key: {
            BACK_SPACE:   8,
            LEFT_ARROW:  37,
            RIGHT_ARROW: 39,
            DELETE: 46,
            ENTER: 13,
            UP_ARROW: 38,
            DOWN_ARROW: 40,
            TAB: 9,
            CTRL: 17,
            A: 65,
            ESC: 27,
            SHIFT: 16,
            C: 67,
            X: 88,
            V: 86
        },
        getCursorLineIdx: function(arr, cursorIdx) {
            var cursorLineIdx = arr.lastIndexOf("\n", cursorIdx);
            if (cursorLineIdx === -1) {
                return cursorIdx;
            }
            return cursorIdx - cursorLineIdx;
        },
        getNextLineLength: function(arr, cursorIdx) {
            var lineCharIdx1 = arr.indexOf("\n", cursorIdx);
            var lineCharIdx2;

            if (lineCharIdx1 > -1) {
                lineCharIdx2 = arr.indexOf("\n", lineCharIdx1 + 1);
                if (lineCharIdx2 === -1) {
                    lineCharIdx2 = arr.length;
                }
                return lineCharIdx2 - lineCharIdx1 - 1;
            }
            return 0;
        },
        /**
         * Get the line char index starting from an index specified by startIdx in reverse.
         * If line character is not found, return zero since that means it has reached the start of the array.
         *
         * @param arr - the array
         * @param startIdx - the start index
         */
        getLineCharIndexReverse: function(arr, startIdx) {
            var idx = arr.lastIndexOf("\n", startIdx);
            if (idx > -1) {
                return idx;
            }
            return 0;
        }
    }
});

/**
 * A character component.
 *
 * Properties
 *
 * 1. char - the character to display.
 * 2. idx - character index.
 */
var Character = React.createClass({
    render: function() {
        if (this.props.char === " ") {
            this.props.char = String.fromCharCode(160); // not breaking space
        }

        var className = this.props.className === undefined ? "character" : "character" + " " + this.props.className;

        var showCursor = (this.props.idx === this.props.parent.state.cursorIdx);

        return React.createElement("div", {className:className, onClick:this.clickHandler},
                                   React.createElement("span", {className:this.props.charCustomClassName}, this.props.char),
                                   React.createElement("span", {className:"cursor" +
                                   (showCursor ? " blink" : " hide-cursor") + " " + this.props.customCursorClassName}));

    },
    clickHandler: function() {
        this.props.parent.setState({cursorIdx:this.props.idx, selectRange:null});
    }
});