(function ($) {

  Drupal.behaviors.esportsSchedule = {
    attach: function(context) {
		$('ul.week-nav li:first a').addClass('active');
		$('.match-schedule').hide();
		var activeTab = $('ul.week-nav li:first a').attr('href');
		$(activeTab).show();
		$('ul.week-nav li a').click(function(){
			$('ul.week-nav li a').removeClass('active');
			$(this).addClass('active');
			$('.match-schedule').hide();
			var activeTab = $(this).attr('href');
			$(activeTab).show();
			return false;
		});
    }
  };
})(jQuery);
