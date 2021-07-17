//datetime picker start
let now = new Date()
$(".form_datetime").datetimepicker({
    language: 'zh-CN',
    format: "yyyy年mm月dd日 hh:ii",
    clearBtn: 1,
    weekStart: 0,
    todayBtn: 1,
    autoclose: 1,
    todayHighlight: 1,
    startView: 2,
    forceParse: 0,
    startDate: now.toLocaleString(),
    minuteStep: 10
});
//datetime picker end