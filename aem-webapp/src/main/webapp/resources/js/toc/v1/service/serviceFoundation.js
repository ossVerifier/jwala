var serviceFoundation = {

    get : function (url, dataType) {
        return Promise.cast($.ajax({
                                       url: url,
                                       dataType: dataType,
                                       type: 'GET',
                                       cache: false
                                   }));
    },

    post : function(url, dataType, content) {
        return Promise.cast($.ajax({
                                       url: url,
                                       dataType: dataType,
                                       type: 'POST',
                                       data: content,
                                       contentType: 'application/json',
                                       cache: false
                                   }))}
};