function checkAccount(){
    var account = $('#account').val();
    if (!account.match(/[A-Za-z0-9]{4,11}/)){
		$('#acc-error').html('The account must contains from 4 to 11 letters or numbers');
		$('#acc-error').show();
	}
}

function checkPassword(){
    var pass = $('#password').val();
    if ( pass.length < 8 ) {
        $('#pass-error').html('Password min size 8 characters');
        $('#pass-error').show();
    }
}

function checkConfirmPassword(){
    var pass = $('#password').val();
    var confirm_pass = $('#password_confirm').val();
    if ( pass !=  confirm_pass ) {
        $('#cpass-error').html('Password confirm must be as same password');
        $('#cpass-error').show();
    }
}

$(document).ready(function () {  $("#password").passStrength({  userid: "#account" }); });

(function($){
        $.fn.shortPass = 'Password min size 8 characters';
        $.fn.badPass = 'Weak strenght';
        $.fn.averagePass = 'Average strenght';
        $.fn.goodPass = 'Good strenght';
        $.fn.strongPass = 'Strong strenght';
        $.fn.samePassword = 'Username and Password identical';
        $.fn.resultStyle = "";

         $.fn.passStrength = function(options) {

                 var defaults = {
                                shortPass:              "shortPass",    //optional
                                badPass:                "badPass",              //optional
                                goodPass:               "goodPass",             //optional
                                strongPass:             "strongPass",   //optional
                                baseStyle:              "pass-error",   //optional
                                userid:                 "",                             //required override
                                messageloc:             1                               //before == 0 or after == 1
                        };
                        var opts = $.extend(defaults, options);

                        return this.each(function() {
                                 var obj = $(this);

                                $(obj).unbind().keyup(function()
                                {

                                        var results = $.fn.teststrength($(this).val(),$(opts.userid).val(),opts);
										//var progress = 390 / 100 * parseInt(results);
                                        //$(".strength-progress").width(progress);
										var message = $(this).badPass;
										if(parseInt(results) < 0) {
											message = $(this).samePassword;
										} else if(parseInt(results) < 8) {
											message = $(this).shortPass;
										} else if(parseInt(results) < 34) {
											message = $(this).badPass;
										} else if(parseInt(results) < 62) {
											message = $(this).averagePass;
										} else if(parseInt(results) < 85) {
											message = $(this).goodPass;
										} else {
											message = $(this).strongPass;
										}
										$("#" + opts.baseStyle).html(message);
                                        $("#" + opts.baseStyle).show();
                                 });

                                //FUNCTIONS
                                $.fn.teststrength = function(password,username,option){
                                                var score = 0;

                                            //password < 4
                                            if (password.length < 4 ) { return score; }

                                            //password == user name
                                            if (password.toLowerCase()==username.toLowerCase()){return -1;}

                                            //password length
                                            score += password.length * 4;
                                            score += ( $.fn.checkRepetition(1,password).length - password.length ) * 1;
                                            score += ( $.fn.checkRepetition(2,password).length - password.length ) * 1;
                                            score += ( $.fn.checkRepetition(3,password).length - password.length ) * 1;
                                            score += ( $.fn.checkRepetition(4,password).length - password.length ) * 1;

                                            //password has 3 numbers
                                            if (password.match(/(.*[0-9].*[0-9].*[0-9])/)){ score += 5;}

                                            //password has 2 symbols
                                            if (password.match(/(.*[!,@,#,$,%,^,&,*,?,_,~].*[!,@,#,$,%,^,&,*,?,_,~])/)){ score += 5 ;}

                                            //password has Upper and Lower chars
                                            if (password.match(/([a-z].*[A-Z])|([A-Z].*[a-z])/)){  score += 10;}

                                            //password has number and chars
                                            if (password.match(/([a-zA-Z])/) && password.match(/([0-9])/)){  score += 15;}
                                            //
                                            //password has number and symbol
                                            if (password.match(/([!,@,#,$,%,^,&,*,?,_,~])/) && password.match(/([0-9])/)){  score += 15;}

                                            //password has char and symbol
                                            if (password.match(/([!,@,#,$,%,^,&,*,?,_,~])/) && password.match(/([a-zA-Z])/)){score += 15;}

                                            //password is just a numbers or chars
                                            if (password.match(/^\w+$/) || password.match(/^\d+$/) ){ score -= 10;}

                                            //verifying 0 < score < 100
                                            if ( score < 0 ){score = 0;}
                                            if ( score > 100 ){  score = 100;}

                                            if (score < 34 ){return score;}
                                            if (score < 68 ){return score;}

                                            return score;

                                };

                  });
         };
})(jQuery);


$.fn.checkRepetition = function(pLen,str) {
        var res = "";
     for (var i=0; i<str.length ; i++ )
     {
         var repeated=true;

         for (var j=0;j < pLen && (j+i+pLen) < str.length;j++){
             repeated=repeated && (str.charAt(j+i)==str.charAt(j+i+pLen));
             }
         if (j<pLen){repeated=false;}
         if (repeated) {
             i+=pLen-1;
             repeated=false;
         }
         else {
             res+=str.charAt(i);
         }
     }
     return res;
        };

$(document).ready(function() {

	$('.btn-generate').click(function(){
		$.get("/data/prefix/get", function (data) {
		$('#prefixval').html('Prefix: '+data);
		$('#prefixinput').val(data);
		});
	});

	$('body').on("click", '.submit-btn', function(){
		var el = $(this);
		var response_loc = el.parents('form').attr('name');
		var action = el.parents('form').attr('action');
		if(action === '')
		action = "/data/controllers";

		$.ajax({
			url: action,
			data: $(el).parents('form').serialize(),
			type: "POST",
			cache: false,
			dataType: 'json',
			beforeSend: function () {
				el.attr("disabled","disabled");
				bSpinner('play');
			},
			error: function (xhr, ajaxOptions, thrownError) {
				console.log("Result : " + xhr.responseText);
				bSpinner('stop');
			}
			}).done(function (msg) {
				if(msg.type == 'msg'){
					$.notij(msg.text , {'type': msg.status});
				}else{
					if (response_loc === undefined)
						$('.show').html(msg.text);
					else
						$('.show_' + response_loc).html(msg.text);
				}

				if(msg.location){
					setTimeout("document.location.href='/"+msg.location+"'", msg.time);
				}

				if(msg.eval){
					jQuery.globalEval( msg.eval );
				}

			}).always(function () {
				bSpinner('stop');
				setTimeout(function () {
					el.removeAttr("disabled");
				}, 500);
			});
		});
	});
	var bSpinner = function(state){
            var $window, position, showSpinnerClass, spinner;

            spinner = $('.bSpinner');
            showSpinnerClass = 'bSpinner__mType_visible';
            position = {
                top: -40,
                left: -40
            };
            spinner.css(position);

            $window = $(window);
            if (state === 'play') {
                spinner.addClass(showSpinnerClass);
                $window.on("mousemove.bSpinnerPosition", function (evt) {
                    position.top = evt.clientY + 8;
                    position.left = evt.clientX + 8;
                    spinner.css(position);
                });
            }else{
                $window.off("mousemove.bSpinnerPosition");
                spinner.removeClass(showSpinnerClass);
            }

        };

(function( $ )
	{
		$.notij=function(msg, options, callback) { return $.fn.notij(msg, options, callback); };

		$.fn.notij = function(msg, options, callback){

			var settings={
				'speed': 'fast', // fast, slow, or number eg: 100
				'multiple': true,
				'autoclose': 5000, //set timeout speed
				'onfocusdelay': true, //keep dialog open
				'type' : 'default' // blank means default, other types error,info,
			};
			//adding DOM html to msg if dom specified.
			if(!msg){msg=this.html();}
			//merging settings with given options
			if(options){ $.extend(settings,options);}

			var display=true;
			var multiple="no";

			var uniquid=Math.floor(Math.random()*99999);

			$('.notij-msg').each(function(){
				if($(this).html()==msg && $(this).is(':visible')){
					multiple="yes";
					if(!settings['multiple']){
						display = false;
					}
				}
				if($(this).attr('id')==uniquid){
					uniquid=Math.floor(Math.random()*99999);
				}
			});

			//adding main layout
			if($('.notij-que').length==0){
				$('body').append('<div class="notij-que"></div>');
			}

			if(display){
                $('.notij-que').prepend('<div class="notij theme_' + settings['type'] + '" id="' + uniquid + '"></div>');
                $('#' + uniquid).append('<span class="notij-close close_' + settings['type'] + '" rel="' + uniquid + '">x</span>');
                $('#' + uniquid).append('<div class="notij-msg" rel="' + uniquid + '">' + msg + '</div>');

				var height=$('#'+uniquid).height();
				//$('#'+uniquid).css('height',height);

				$('#'+uniquid).slideDown(settings['speed']);
				display=true;
			}

			$('.notij').ready(function(){
				if(settings['autoclose']){
					$('#'+uniquid).delay(settings['autoclose']).fadeOut(settings['speed'],function(){
						$('#'+uniquid).remove();
					});
				}
			});

			//activating onfocus delay
			$('.notij').mouseover(function(){
				if(settings['onfocusdelay']){
					$('#' + $(this).attr('id')).dequeue().stop().show();
				}
			});

			$('.notij').mouseout(function(){
				if(settings['autoclose']){
					$('#' + $(this).attr('id')).delay(settings['autoclose']).fadeOut(settings['speed'],function(){
						$('#' + $(this).attr('id')).remove();
					});
				}
			});

			$('.notij-close').click(function(){
				$('#' + $(this).attr('rel')).dequeue().fadeOut(settings['speed']);
				$('#' + $(this).attr('rel')).remove();
			});

			var result={
				"id": uniquid,
				"multiple": multiple
			};

			if(callback){
				callback(result);
			} else{
				return(result);
			}

		}

})( jQuery );