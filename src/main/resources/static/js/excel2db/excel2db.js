/**
 * Created by xufy on 2018/5/15.
 */
//js-xlsx解析代码开始
var input_dom_element = document.getElementById("inputFile");
var wb,fileType;
function process_wb(wb) {
    processBar.setProcess(75);
    var ws = wb.Sheets[wb.SheetNames[0]];
    document.getElementById("table-content").innerHTML = XLSX.utils.sheet_to_html(ws, {
        id: "data-table",
        editable: true
    });
    $("#table-content").find("tbody").find("tr:gt(1000)").remove();
    processBar.setProcess(100);
    $("#progress-container").hide(2000);
}
function handle_ie() {
    var path = input_dom_element.value;
    if (!path) {
        return;
    }
    processBar.setProcess(0);
    var data = IE_LoadFile(path);
    processBar.setProcess(25);
    wb = XLSX.read(data, {type:'binary'});
    processBar.setProcess(50);
    process_wb(wb);
}
function handle_fr(e) {
    var files = e.target.files, f = files[0];
    if (!f) {
        return;
    }
    processBar.setProcess(0);
    fileType = f.name.substr(f.name.lastIndexOf('.')+1,f.name.length);
    var reader = new FileReader();
    var rABS = !!reader.readAsBinaryString;
    processBar.setProcess(25);
    reader.onload = function(e) {
        var data = e.target.result;
        if (fileType === 'txt' || fileType === 'csv') {
            var str = null;
            var viewBuf = null;
            if (rABS) {
                str = data;
                var newArray = [];
                for (var index = 0; index < data.length; index++) {
                    newArray.push(data.charCodeAt(index));
                }
                viewBuf = new Uint8Array(newArray);
            } else {
                viewBuf = new Uint8Array(data);
                for (var index in viewBuf) {
                    str += String.fromCharCode(viewBuf[index]);
                    if (index >= 100) {
                        break;
                    }
                }
            }
            var codepage = jschardet.detect(str.substring(0, 1000)).encoding;
            if (codepage === 'GB2312' || codepage === 'GB18030') {
                codepage = 'GB18030';
            } else if (codepage === 'ascii' || codepage === 'UTF-8' || codepage === 'UTF-16BE' || codepage === 'UTF-16LE') {

            } else {
                codepage = "GBK";
            }
            data = new TextDecoder(codepage).decode(viewBuf);
        } else if (fileType === 'xls' || fileType === 'xlsx') {
            if(!rABS) data = new Uint8Array(data);
        }
        wb = XLSX.read(data, {type: rABS ? 'binary' : 'array'});
        process_wb(wb);
    };
    if(rABS) reader.readAsBinaryString(f); else reader.readAsArrayBuffer(f);
    processBar.setProcess(50);
}
var handler = typeof IE_LoadFile !== 'undefined' ? handle_ie : handle_fr;
if(input_dom_element.attachEvent) input_dom_element.attachEvent('onchange', handler);
else input_dom_element.addEventListener('change', handler, false);
function get_columns(wb, type) {   //获取head头(excel文件第一行)
    var sheet = wb.Sheets[wb.SheetNames[0]];
    var val, rowObject, range, columnHeaders, emptyRow, C;
    if (!sheet['!ref']) return [];
    range = XLSX.utils.decode_range(sheet["!ref"]);
    columnHeaders = [];
    for (C = range.s.c; C <= range.e.c; ++C) {
        val = sheet[XLSX.utils.encode_cell({ c: C, r: range.s.r })];
        if (!val){
            toastr.warning("列名不能为空");
            return;
        }
        columnHeaders[C] = type.toLowerCase() === 'xls' || type.toLowerCase() === 'xlsx' ? XLSX.utils.format_cell(val) : val.v;
    }
    return columnHeaders;
}
function get_columns_content(wb,type) {
    var sheet = wb.Sheets[wb.SheetNames[0]];
    var val, rowObject, range, C;
    if (!sheet['!ref']) return [];
    range = XLSX.utils.decode_range(sheet["!ref"]);
    var columnContents = [];
    var column;
    for(var R = range.s.r + 1; R <= range.e.r; ++R) {
        column = [];
        for (C = range.s.c; C <= range.e.c; ++C) {
            val = sheet[XLSX.utils.encode_cell({ c: C, r: R })];
            if (!val){
                column[C] = "";
            } else {
                column[C] = type.toLowerCase() === 'xls' || type.toLowerCase() === 'xlsx' ? XLSX.utils.format_cell(val) : val.v;
            }
        }
        columnContents.push(column);
    }
    return columnContents;
}
//js-xlsx解析代码结束
function columnSetting(type) {
    if (!wb) {
        return;
    }
    var tbl = document.getElementById('data-table');
    var wb1 = XLSX.utils.table_to_book(tbl);
    var columnHeaders = get_columns(wb1,fileType);
    /*var reg = new RegExp("[\\u4E00-\\u9FFF]+","g");
    var pattern = new RegExp("[`~!@#$^&*()=|{}':;',\\[\\].<>/?~！@#￥……&*（）——|{}【】‘；：”“'。，、？]")
    for (var i = 0; i < columnHeaders.length; i++) {
        if (reg.test(columnHeaders[i])) {
            toastr.warning("列名不能包含中文!");
            return false;
        } else if (pattern.test(columnHeaders[i])) {
            toastr.warning("列名不能包含特殊字符!");
            return false;
        }
    }*/
    var html = '';
    for (var i = 0; i < columnHeaders.length;i++) {
        html += '<div class="form-group">';
        html += '<label for="'+columnHeaders[i]+'" class="col-sm-2 control-label">'+columnHeaders[i]+'</label>';
        html +=     '<div class="col-sm-7">';
        if (type === "mysql") {
            html +=         '<select class="form-control" id="'+columnHeaders[i]+'">';
            html +=             '<option value="VARCHAR" selected>VARCHAR</option>';
            html +=             '<option value="CHAR">CHAR</option>';
            html +=             '<option value="INT">INT</option>';
            html +=             '<option value="FLOAT">FLOAT</option>';
            html +=             '<option value="DOUBLE">DOUBLE</option>';
            html +=             '<option value="DATE">DATE</option>';
            html +=             '<option value="TIMESTAMP">TIMESTAMP</option>';
            html +=         '</select>';
        } else if (type === "oracle") {
            html +=         '<select class="form-control" id="'+columnHeaders[i]+'">';
            html +=             '<option value="VARCHAR2" selected>VARCHAR2</option>';
            html +=             '<option value="CHAR">CHAR</option>';
            html +=             '<option value="NUMBER">NUMBER</option>';
            html +=             '<option value="LONG">LONG</option>';
            html +=             '<option value="DATE">DATE</option>';
            html +=             '<option value="TIMESTAMP">TIMESTAMP</option>';
            html +=         '</select>';
        }
        html +=     '</div>';
        html += '<div class="col-md-3">';
        html += '<input type="text" class="form-control" id="'+columnHeaders[i]+'-length" placeholder="长度/设置">';
        html += '</div></div>';
    }
    $("#cols-form").html(html);
    return true;
}
function createTableDO(){
    var fieldArr = [];
    var fieldObj;
    $("#cols-form").find("div[class='form-group']").each(function (index,ele) {
        fieldObj = {};
        var fieldName = $.trim($(this).find("label").get(0).innerHTML);
        fieldObj.name = fieldName;
        var type = $("#"+fieldName).val();
        fieldObj.type = type;
        if (type === "VARCHAR" || type === "VARCHAR2" || type === "CHAR") {
            var length = $("#" + fieldName + "-length").val();
            if (length) {
                fieldObj.length = length;
            } else {
                fieldObj.length = 200;
            }
        }
        fieldArr.push(fieldObj);
    });
    return fieldArr;
}
function getColumnList(){
    var fieldName = "";
    $("#cols-form").find("div[class='form-group']").each(function (index,ele) {
        fieldName += $.trim($(this).find("label").get(0).innerHTML);
        if (index !== $("#cols-form").find("div[class='form-group']").length - 1) {
            fieldName += ",";
        }
    });
    return fieldName;
}