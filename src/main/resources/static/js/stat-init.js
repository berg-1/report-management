let reports = [];
let current = 0;
$(document).ready(function () {
    $("input#rating").val("0")
    // 初始化页面,选择第一个同学的实验报告(如果存在)
    let submittedStudents = $("#editable-sample tbody");
    let trList = submittedStudents.eq(0).children("tr")
    for (let i = 0; i < trList.length; i++) {
        // 将实验报告id添加到数组中
        const id = trList.eq(i).attr("id");
        reports[i] = id;
        console.log(i + " " + id);
    }

    //eq表示同类元素中的某一个，类似于数组用下标取元素；children表示子元素
    let reportURL = submittedStudents.eq(0).children("tr").eq(0).attr("id");
    if (reportURL !== undefined) {
        displayReport("inlineDisplay?rid=" + reports[current]);
    }

    // 初始化评分.
    $('#rating').rating({
        min: 0,
        max: 10,
        minThreshold: 0,
        step: 0.2,
        stars: 5,
        language: "zh",
        containerClass: 'inline-display'
    });


});

// 评论改变时自动提交
$('#comment').on(
    'change', function () {
        $.ajax({
            "type": 'post',
            "url": '/comment',
            "data": {
                "rid": reports[current],
                "comment": $(this).val()
            },
            "dataType": "text",
            "success": function (data) {
                console.log("成功提交comment" + data)
                $.gritter.add({
                    title: '成功提交!',
                    text: '该评论已自动保存',
                    sticky: false,
                    time: '2000',
                    class_name: 'success'
                });
            }
        });
    });

// 评分改变时自动提交
$('.rating-loading').on(
    'change', function () {
        const rate = $(this).val()
        console.log(reports[current], '评分:', rate);
        // 与后端同步分数
        $.ajax({
            "type": 'get',
            "url": '/rating?rid=' + reports[current] + '&rating=' + parseFloat(rate),
            "dataType": "text",
            "success": function (data) {
                // pop up something
                console.log("成功更新分数...", data)
                $.gritter.add({
                    title: '成功更新分数!',
                    text: "实验报告分数为<span class='red'>" + rate + '分</span>',
                    sticky: false,
                    time: '2000',
                    class_name: 'success'
                });
            }
        });
    });

function displayReport(reportURL) {
    const regex = new RegExp("[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}");
    let rid = regex.exec(reportURL);  // 正则表达式返回一个匹配数组,选第一个
    for (let i = 0; i < reports.length; i++) {
        let a = $("#" + reports[i]);
        a.eq(0).children("td").eq(1).children("a").eq(0).attr("class", "");
        if (rid[0] === reports[i]) {
            a.eq(0).children("td").eq(1).children("a").eq(0).attr("class", "selected");
            let previous = $("#previous");
            let next = $("#next");
            previous.attr("class", "previous");
            next.attr("class", "next");
            if (i === 0)
                previous.attr("class", "previous disabled");
            else if (i === reports.length - 1)
                next.attr("class", "next disabled");
            current = i;
        }
    }
    // 添加参数 PDF 初始页为1, 适应页面宽度 https://www.jianshu.com/p/1c91aa255259
    $("#frame").attr("src", reportURL + "#page=1&view=FitH");
    // 更新评分
    $.ajax({
        "type": 'get',
        "url": '/getRate?rid=' + reports[current],
        "dataType": "text",
        "success": function (data) {
            console.log("rating: " + data);
            $('input#rating').rating('update', data);
        }
    });
    // 更新评论
    $.ajax({
        "type": 'get',
        "url": '/getComment?rid=' + reports[current],
        "dataType": "text",
        "success": function (data) {
            console.log("comment: " + data);
            $('#comment').val(data);
        }
    });
}

// 上一个按钮
function previous() {
    if (current > 0) {
        displayReport("inlineDisplay?rid=" + reports[--current]);
    }
}

// 下一个按钮
function next() {
    if (current < reports.length - 1) {
        displayReport("inlineDisplay?rid=" + reports[++current]);
    }
}