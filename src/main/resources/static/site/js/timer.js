function counter()
{
	var now = new Date();
	var counteroff = new Date("May,04,2018,19:00:00+0300");
	var totalRemains = (counteroff.getTime()-now.getTime());
	if (totalRemains>1)
	{
		var RemainsSec=(parseInt(totalRemains/1000));
		var RemainsFullDays=(parseInt(RemainsSec/(24*60*60)));
		var secInLastDay=RemainsSec-RemainsFullDays*24*3600;
		var RemainsFullHours=(parseInt(secInLastDay/3600));
		if (RemainsFullHours<10){RemainsFullHours="0"+RemainsFullHours};
		var secInLastHour=secInLastDay-RemainsFullHours*3600;
		var RemainsMinutes=(parseInt(secInLastHour/60));
		if (RemainsMinutes<10){RemainsMinutes="0"+RemainsMinutes};
		var lastSec=secInLastHour-RemainsMinutes*60;
		if (lastSec<10){lastSec="0"+lastSec};
		document.getElementById('counteroff').innerHTML = "<span>"+RemainsFullDays+"<p>Дней</p></span><span>"+RemainsFullHours+"<p>Часов</p></span><span>"+RemainsMinutes+"<p>Минут</p></span><span>"+lastSec+"<p>Секунд</p></span>";
		setTimeout("counter()",1000);
	} 
	else {document.getElementById("counteroff").innerHTML = "<span>00<p>Дней</p></span><span>00<p>Часов</p></span><span>00<p>Минут</p></span><span>00<p>Секунд</p></span>";}
}