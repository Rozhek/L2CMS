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
		document.getElementById('counteroff').innerHTML = "<span>"+RemainsFullDays+"<p>Days</p></span><span>"+RemainsFullHours+"<p>Hours</p></span><span>"+RemainsMinutes+"<p>Mins</p></span><span>"+lastSec+"<p>Secs</p></span>";
		setTimeout("counter()",1000);
	} 
	else {document.getElementById("counteroff").innerHTML = "<span>00<p>Days</p></span><span>00<p>Hours</p></span><span>00<p>Mins</p></span><span>00<p>Secs</p></span>";}
}