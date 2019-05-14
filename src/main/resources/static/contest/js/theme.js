/**
 * @file
 * Defines the Javascript for the tournament switcher content type.
 *
 */

(function ($) {
  Drupal.behaviors.tourneyLocalTime = {
    attach: function(context, settings) {
      $("time").each(function() {
        $this = $(this);
        if ($this.hasClass('short-date')) {
          var timeFormat = 'ddd MM/DD';
        }
        else if ($this.hasClass('date')) {
          var timeFormat = 'dddd MM/DD';
        }
        else if ($this.hasClass('local-time')) {
          d = new Date();
          offset = d.getTimezoneOffset() / 60;
          var timeFormat = (offset >= 4 && offset <= 8) ? 'LT' : 'HH:mm';
        }
        var localDate = moment($this.attr('datetime'), 'YYYY-MM-DDTHH:mm:ssZ').format(timeFormat).replace(/(:00|) (?=(AM|PM))/, '');
        $this.html(localDate);
      });
    }
  }
})(jQuery);

;

/**
 * @file
 * Esports Display - Hide Spoilers
 */

(function ($) {

  Drupal.behaviors.esportsHideSpoilers = {
    attach: function(context) {
      var self = this;

      $('#hide-spoilers .slider-frame').toggle(
        function() {
          $('.slider-button', this).removeClass('off').html('ON');
          $.cookie("spoilers", 'hidespoilers', { expires: 10000, path: '/' });
          self.hideWinners();
        },
        function() {
          $('.slider-button', this).addClass('off').html('OFF');
          $.cookie("spoilers", 'showspoilers', { expires: 10000, path: '/' });
          self.showWinners();
        }
      );

      if ($.cookie("spoilers") == 'hidespoilers') {
        $('#hide-spoilers .slider-frame').click();
      }
      $('html').addClass('js-processed');
    },
    hideWinners: function() {
      $('.winner').removeClass('winner').addClass('no-winner');
      // First hide all the panes.
      $('body.post-match .pane-page-content .panel-display > div').addClass('hide').removeAttr('style');
      // We do want to see the head to head display
      $('body.post-match .pane-page-content .panel-col-featured').removeClass('hide');
    },
    showWinners: function() {
      $('.no-winner').removeClass('no-winner').addClass('winner');
      $('body.post-match .pane-page-content .panel-display > div').fadeIn().removeClass('hide');
    }
  };

})(jQuery);

/**
 * @file
 * A JavaScript file for the theme.
 *
 * In order for this JavaScript to be loaded on pages, see the instructions in
 * the README.txt next to this file.
 */

// JavaScript should be made compatible with libraries other than jQuery by
// wrapping it with an "anonymous closure". See:
// - http://drupal.org/node/1446420
// - http://www.adequatelygood.com/2010/3/JavaScript-Module-Pattern-In-Depth
(function ($, Drupal, window, document, undefined) {
  Drupal.behaviors.carousel_versus = {
    attach : function(context, settings) {
      $(".match-title span:not('.processed')").each(function(i, e) {
        $(e).addClass('processed').html($(e).text().replace(/ vs /, ' <span style="display:inline" class="accent">vs</span><br /> '));
      });
      $(".next-match-teaser .match-label span").each(function(i, e) {
        $(e).addClass('processed').html($(e).text().replace(/ vs /, ' <span style="display:inline" class="accent">vs</span> '));
      });
    }
  }
})(jQuery, Drupal, this, this.document);


(function ($) {

  Drupal.behaviors.esportsMenu = {
    attach: function(context) {


    }
  };
})(jQuery);

(function ($) {

  Drupal.behaviors.esportsMenu = {
    attach: function(context) {
		$('#main-menu li').each(function () {
		if (this.getElementsByTagName("a")[0].href == location.href){
			this.getElementsByTagName("a")[0].className = "active";
			}
		});
    }
  };
})(jQuery);
