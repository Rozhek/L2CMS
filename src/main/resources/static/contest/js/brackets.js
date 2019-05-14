/**
 * @file
 * Adds the proper width to the brackets to stretch full width.
 */

(function ($) {
  Drupal.behaviors.esportsBracketFitWidth = {
    attach : function (context, settings) {
      var self = this;
      this.$bracketView = $('.esports-bracket-fit-width');
      if (this.$bracketView.length > 0) {
        this.fitBracketWidth(this.$bracketView);
      }
    },

    // Function to change the width of the matches to give the bracket full width.
    fitBracketWidth: function(bracket) {
      // Find the last round, get the classes, and then return the number of rounds
      // from the last match's class.
      var lastRound = bracket.find('.tourney-match:last').find("div[class*='round-']"),
        matchClasses = lastRound.attr('class').split(/ +/),
        roundClass = _.find(matchClasses, function(classFind) {
          return (classFind.indexOf("round-") >= 0);
        });

      // Adding a last-round class for theme ease.
      lastRound.parents('.parent').addClass('last-round');
      // Giving a complete class for more ease.
      if (lastRound.hasClass('complete')) {
        lastRound.parents('.parent').addClass('last-round-complete');
      }

      // Get just the number from the class and find the proper width.
      var numRounds = roundClass.substring(roundClass.indexOf('-')+1),
          // Bracket width divided by number of rounds, accounting for margins.
          matchWidth = bracket.width() / numRounds - numRounds * 6;

      // Set the new width.
      bracket.find('.match').width(matchWidth);
      $('.bracket-main-3rd').removeAttr("style");

    },
  };
})(jQuery);


;
(function($) {
  Drupal.behaviors.tourneySetWidth = {
    attach: function(context, settings) {
      $(".tourney", context).each(function(){
        $rounds = $(".tourney-single .round, .bracket-top .round, .bracket-champion .round:not(:empty)", context);
        rw = $($rounds[0]).css('width');
        if (!rw) return;
        $('.tourney-inner', this).css('width', (parseInt(rw)+parseInt(rw)*$rounds.length)+'px');
      });
    }
  };
  Drupal.behaviors.tourneyDisableWin = {
    attach: function(context, settings) {
      $(".win-button", context).click(function() {
        $(".win-button", context).each(function() {
          $(this).hide();
          $('<div class="ajax-progress"><span class="throbber"></span></div>').insertAfter(this);
        })
      });
    }
  };
})(jQuery);;
(function($) {

/**
 * Preselectors
 */
var $tournament = $('.tourney-tournament-tree'),
  $teams = {},
  $activeTeam = $();

/**
 * This rebalances all bracket children connectors, so that they line up correctly
 * with the parent connector. The CSS cannot get this "quite" correct alone.
 */
Drupal.behaviors.tourneyFixConnectors = { attach: function(context, settings) {
  $('.tree-node', context).each(function() {
    var $this = $(this);
    var $children = $this.children('.children').children('.tree-node');
    var $parent   = $this.children('.parent');

    if ($children.length == 0) return;

    var $connectors = $('.connector .path', $children.children('.parent'));

    var $connectParent = $('.connector', $parent);
    var $connectBottom = $connectors.last();
    var $connectTop    = null;
    if ($connectors.length > 1) 
      $connectTop      = $connectors.first(); 

    var connectTarget  = $connectParent.offset().top + $connectParent.height();
    var connectOffset  = $connectBottom.offset().top - connectTarget;

    if (connectOffset <= 1) return;    

    if (connectOffset) {
      $connectBottom.css({
        height: $connectBottom.height() + connectOffset,
        top: -connectOffset
      });
      if ($connectTop)
        $connectTop.css('height', $connectTop.height() - connectOffset);
    }
  });
} }

Drupal.behaviors.tourneyFixHeight = { attach: function(context, settings) {
  var $tree   = $('.tourney-tournament-tree', context);
  var current = $tree.offset().top + $tree.height();
  var $match  = $('.tree-node', $tree).last();
  var target  = $match.offset().top + $match.height() + 50;
  $tree.css('padding-bottom', target-current);
} }

Drupal.behaviors.tourneyHighlight = { attach: function(context, settings) {
  $('.contestant', context).hover(function() {
    var eid = '.' + $(this).attr('class').match(/(eid-[^ "']+)/)[0];
    $(eid).addClass('tourney-highlight');
    $('.contestant' + eid).closest('.match').addClass('tourney-highlight');
    $('.connector' + eid).each(function(){
      $parent = $(this).closest('.parent').closest('.children').closest('.tree-node');
      if ($(this).hasClass('winner-bottom')) {
        $('.connector.to-children', $(this).parent()).addClass('winner-bottom');
      }
      $('.connector.to-children', $parent.children('.parent')).addClass('tourney-highlight');
    });
    $notnecessary = $('.match.not-necessary');
    if ($notnecessary) {
      $('.connector', $notnecessary).removeClass('tourney-highlight');
      //$('.top-node > .children > .tree-node > .parent > .connector').removeClass('tourney-highlight');
    }
  }, function() {
    $('.tourney-highlight').removeClass('tourney-highlight');
  });
} }

})(jQuery);