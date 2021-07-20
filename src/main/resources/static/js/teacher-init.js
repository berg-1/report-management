$(document).ready(function () {
    $.ajax({
        "type": 'get',
        "url": '/nodes',
        "dataType": "json",
        "success": function (data) {
            console.log(document.URL)
            $.each(data, function (idx, obj) {
                let info = obj.href.split('@')
                let classId = getQueryVariable("classId");
                let courseId = getQueryVariable("courseId");
                if (classId === info[0] && courseId === info[1]) {  // 选择的那个 课程-班级
                    $("#treeTable").append("<tr data-tt-id=\"" + obj.nid + "\" data-tt-parent-id=\"" +
                        obj.pid + "\"><td class='selected'>" + obj.text + "</td></tr>");
                } else {
                    $("#treeTable").append("<tr data-tt-id=\"" + obj.nid + "\" data-tt-parent-id=\"" +
                        obj.pid + "\"><td><a href=\"/mainTeacher?classId=" + info[0] + "&courseId=" + info[1] + "\">" +
                        obj.text + "</a></td></tr>");
                }
            });
            $("#treeTable").treetable({
                expandable: true,
                initialState: "expanded", /*expanded collapsed*/
                clickableNodeNames: true,
                indent: 30
            });
        }
    });
    $("#home").attr("class", "active");

    window.setTimeout(function () {
        $(".alert").fadeTo(1000, 0).slideUp(1000, function () {
            $(this).remove();
        });
    }, 5000);
});

/**
 * 获取当前URL参数
 * @param variable 要获取的参数名
 * @returns {string|boolean} 存在:返回参数的值;不存在:false
 */
function getQueryVariable(variable) {
    let query = window.location.search.substring(1);
    let vars = query.split("&");
    for (let i = 0; i < vars.length; i++) {
        let pair = vars[i].split("=");
        if (pair[0] === variable) {
            return pair[1];
        }
    }
    return false;
}

function getValues(templateId, templateName, Deadline, classId, courseId) {
    let d = new Date(Deadline);
    $("#templateIdUpdate").val(templateId);
    $("#templateNameUpdate").val(templateName);
    $("#deadlineUpdate").val(d.format("yyyy年MM月dd日 hh:mm"));

}

function getData(info) {
    if (info !== '#') {
        info = info.split('@')
        $("#pb").html("");
        const d = document.createElement('div');
        d.classList.add("adv-table", "editable-table")
        const t = document.createElement("table")
        t.classList.add("table", "table-striped", "table-hover", "table-bordered")
        t.innerHTML = "<thead>\n" +
            "<tr>\n" +
            "    <th>#</th>\n" +
            "    <th>实验</th>\n" +
            "    <th>提交人数</th>\n" +
            "    <th>截至日期</th>\n" +
            "    <th>操作\n" +
            "        <a data-original-title=\"编辑实验 | 删除实验\" type=\"button\"\n" +
            "           class=\"fa fa-question-circle tooltips\" data-toggle=\"tooltip\" data-placement=\"top\"\n" +
            "           href=\"#\">\n" +
            "        </a>\n" +
            "    </th>\n" +
            "</tr>\n" +
            "</thead>"
        const tb = document.createElement("tbody");
        t.append(tb)
        t.id = "editable-sample"
        d.append(t)
        document.getElementById('pb').append(d);
        $.ajax({
            "type": 'get',
            "url": '/templates?classId=' + info[0] + '&courseId=' + info[1],
            "dataType": "json",
            "success": function (data) {
                console.log(data.templates)
                const ttt = JSON.parse('{"trda": 1, "2": 2, "3": {"4": 4, "5": {"6": 6}}}');
                console.log(Object.keys(data.templates).length);
                let count = 1;
                $.each(data.templates, function (templateId, templateData) {
                    const d = new Date(templateData.deadline);
                    const year = d.getFullYear()
                    const date = d.getDate()
                    const hour = ("" + d.getHours()).padEnd(2, "0")
                    const minutes = ("" + d.getMinutes()).padEnd(2, "0")
                    tb.innerHTML += "<tr>" +
                        "<td>" + count++ + "</td>" +
                        "<td>" + templateData.name + "</td>" +
                        "<td>1/35</td>" +
                        //yyyy-MM-dd HH:mm
                        "<td>" + year + "-" + d.getMonth() + "-" + date + " " + hour + ":" + minutes + "</td>" +
                        "<td>add | del</td></tr>"
                });
                EditableTable.init();
            }
        });
    }
}