var serviceFoundation = {

    get : function (url, dataType, caughtCallback) {
        return Promise.cast($.ajax({url: url,
                                    dataType: dataType,
                                    type: 'GET',
                                    ache: false
                                })).then(function(response){
                                    if ($.isFunction(caughtCallback)) {
                                        caughtCallback(response);
                                    }
                                }).caught(function(response){
                                    if (response.message !== undefined) {
                                        $.errorAlert(response.message, "Error");
                                    } else if (response.status !== 200) {
                                        $.errorAlert(JSON.stringify(response), "Error");
                                    }
                                });
    },

    post : function(url, dataType, content, thenCallback, caughtCallback) {
        return Promise.cast($.ajax({
                                       url: url,
                                       dataType: dataType,
                                       type: 'POST',
                                       data: content,
                                       contentType: 'application/json',
                                       cache: false
                                   })).then(function(){
                                        if ($.isFunction(thenCallback)) {
                                            thenCallback();
                                        }
                                   }).caught(function(e) {
                                        if ($.isFunction(caughtCallback)) {
                                            caughtCallback(JSON.parse(e.responseText).applicationResponseContent);
                                        }
                                   });
            },

    del : function(url, dataType, caughtCallback) {
        return Promise.cast($.ajax({
            url: url,
            dataType: dataType,
            type: 'DELETE',
            cache: false
        })).caught(function(e){
           if (e.status !== 200) {
               $.errorAlert(JSON.stringify(e), "Error");
           }

           if ($.isFunction(caughtCallback)) {
                caughtCallback();
           }
        });
    },

    put : function(url, dataType, content, thenCallback, caughtCallback) {
            return Promise.cast($.ajax({url: url,
                                        dataType: dataType,
                                        type: 'PUT',
                                        data: content,
                                        contentType: 'application/json',
                                        cache: false
                                    })).then(function(){
                                        if ($.isFunction(thenCallback)) {
                                            thenCallback();
                                        }
                                    }).caught(function(e) {
                                        if ($.isFunction(caughtCallback)) {
                                            caughtCallback(JSON.parse(e.responseText).applicationResponseContent);
                                        }
                                    });
            },

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
    serializedFormToJsonNoId: function(serializedArray) {
        var json = {};
        $.each(serializedArray, function() {
            json[this.name] = this.value;
        });
        return JSON.stringify(json);
    }

};