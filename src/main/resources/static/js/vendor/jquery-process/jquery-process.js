/**
 * Created by xufy on 2018/5/18.
 */
!function($){
    var $container;
    var process = {
        setProcess:function (num) {
            var obj = $("#progress-container");
            if (obj.length <= 0) {
                initContainer();
            }
            obj.find(".progress-bar").css("width",num + "%");
            obj.find(".progress-bar").html(num + "%");
            if (num === 100) {
                obj.hide(2000);
            } else {
                obj.show(200);
            }
        }
    };
    function initContainer() {
        var html = '';
        html += ' <div class="progress">';
        html += '  <div class="progress-bar progress-bar-success progress-bar-striped active" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%">';
        //html += '   <span class="sr-only">0</span>';
        html += '   0';
        html += '  </div>';
        html += ' </div>';
        $container = $("<div/>").prop("id","progress-container").append(html);
        $container.appendTo($("body"));
    }
    window.processBar = process;
}(jQuery);

