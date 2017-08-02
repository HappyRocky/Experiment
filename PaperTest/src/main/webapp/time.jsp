<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>jquery数字读秒的倒计时代码 - 站长素材</title>

<style type="text/css">
h1{font-family:"微软雅黑";font-size:40px;margin:20px 0;;border-bottom:solid 1px #ccc;padding-bottom:20px;letter-spacing:2px;}

.time-item strong{background:#C71C60;color:#fff;line-height:49px;font-size:36px;font-family:Arial;padding:0 10px;margin-right:10px;border-radius:5px;box-shadow:1px 1px 3px rgba(0,0,0,0.2);}
#day_show{float:left;line-height:49px;color:#c71c60;font-size:32px;margin:0 10px;font-family:Arial, Helvetica, sans-serif;}
.item-title .unit{background:none;line-height:49px;font-size:24px;padding:0 10px;float:left;}
</style>

<script type="text/javascript" src="/test/js/jquery.min.js"></script>
<script type="text/javascript">

var intDiff = parseInt(60);//倒计时总秒数量

function timer(intDiff){
	window.setInterval(function(){
	var day=0,
		hour=0,
		minute=0,
		second=0;//时间默认值		
	if(intDiff > 0){
		day = Math.floor(intDiff / (60 * 60 * 24));
		hour = Math.floor(intDiff / (60 * 60)) - (day * 24);
		minute = Math.floor(intDiff / 60) - (day * 24 * 60) - (hour * 60);
		second = Math.floor(intDiff) - (day * 24 * 60 * 60) - (hour * 60 * 60) - (minute * 60);
	}
	if (minute <= 9) minute = '0' + minute;
	if (second <= 9) second = '0' + second;
	$('#day_show').html(day+"天");
	$('#hour_show').html('<s id="h"></s>'+hour+'时');
	$('#minute_show').html('<s></s>'+minute+'分');
	$('#second_show').html('<s></s>'+second+'秒');
	intDiff--;
	}, 1000);
} 

$(function(){
	timer(intDiff);
});	
</script>

</head>
<body>
<div align="center" style="width:416px; margin:0 auto">
<h1>网页上的倒计时</h1>

<div class="time-item">
	<span id="day_show">0天</span>
	<strong id="hour_show">0时</strong>
	<strong id="minute_show">0分</strong>
	<strong id="second_show">0秒</strong>
</div><!--倒计时模块-->
</div>
</body>
</html>