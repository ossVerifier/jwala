var ModalFormDialogJqueryUi = React.createClass({
    getInitialState: function() {
    },
    render: function() {
        return React.DOM.div(null)
    },
    show: function(data, callback) {
        var modalDialog = this;
        var title = this.props.title;

        // $(this.getDOMNode()).append($("#" + this.props.template).children()[0]);
        // $(this.getDOMNode()).dialog({
        // Define the Dialog and its properties.
        $("#" + this.props.template).dialog({
            resizable: false,
            modal: true,
            title: title,
            height: 250,
            width: 400,
            buttons: {
                "Ok": function () {
                    modalDialog.updateItem(callback);
                    $(this).dialog("destroy");
                },
                    "Cancel": function () {
                    $(this).dialog("destroy");
                }
            },
            create: function() {

                $.each(data, function(i, obj) {
                    $("#" + i).val(obj);
                });

            }
        });
    },
    updateItem: function(callback) {

        var serializedArr = $("#jvmEditForm").serializeArray();
        var urlData = "";
        $.each(serializedArr, function(i, obj) {
            urlData = urlData + "/" + escape(obj.value);
        });

        // The submit callback

        // Custom code
        $("#jvmEditForm").one("submit", function(e) {
            var postData = serializedFormToJson($(this).serializeArray());
            console.log(urlData);
            var formURL = $(this).attr("action");
                $.ajax({
                url : formURL + urlData,
                type: "PUT", // Custom code (update action)
                data: postData,
                success:function(data, textStatus, jqXHR) {
                    callback
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    // TODO: Display error message in another modal dialog.
                    alert(textStatus);
                }
            });
            e.preventDefault(); // stop the default action
        });

        $("#jvmEditForm").submit();

    },
//    destroy: function(obj) {
//        $("#" + this.props.template).append($(obj).children()[0]);
//        $(obj).dialog("destroy");
//
//        // reset checkboxes
//        $("input:checkbox").each(function(i, obj) {
//            $(this).prop("checked", false);
//        });
//    }
});
