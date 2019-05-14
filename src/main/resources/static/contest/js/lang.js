(function ($) {
  Drupal.behaviors.esportsChangeLang = {
    attach: function(context) {
      $('#page-wrap div.button').click(function() {

		$clicked = $(this);

		if($clicked.attr("id") == "en-button"){
            if($.cookie('lang') != "en") {
                $.cookie('lang', "en", {
                        expires: 5,
                        path: '/'
                });
                location.reload();
            }
        }else if($clicked.attr("id") == "rus-button"){
            if($.cookie('lang') != "ru"){
                $.cookie('lang', "ru", {
                        expires: 5,
                        path: '/'
                });
                location.reload();
            }
        }
      });
    }
  };
})(jQuery);