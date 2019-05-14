(function( $ ){
$.fn.videoBG = function( selector, options ) {
var options = {};
if (typeof selector == "object") {
options = $.extend({}, $.fn.videoBG.defaults, selector);
}
else if (!selector) {
options = $.fn.videoBG.defaults;
}
else {
return $(selector).videoBG(options);	
}
var container = $(this);

if (!container.length)
return;
if (container.css('position') == 'static' || !container.css('position'))
container.css('position','relative');
if (options.width == 0)
options.width = container.width();
if (options.height == 0)
options.height = container.height();	
var wrap = $.fn.videoBG.wrapper();
wrap.height(options.height)
.width(options.width);
if (options.textReplacement) {
options.scale = true;
container.width(options.width)
.height(options.height)
.css('text-indent','-9999px');
}
else {
wrap.css('z-index',options.zIndex+1);
}
wrap.html(container.clone(true));
var video = $.fn.videoBG.video(options);
if (options.scale) {
wrap.height(options.height)
.width(options.width);
video.height(options.height)
.width(options.width);
}
container.html(wrap);
container.append(video);
return video.find("video")[0];
}
$.fn.videoBG.setFullscreen = function($el) {
var windowWidth = $(window).width(),
windowHeight = $(window).height();
$el.css('min-height',0).css('min-width',0);
$el.parent().width(windowWidth).height(windowHeight);
if (windowWidth / windowHeight > $el.aspectRatio) {
$el.width(windowWidth).height('auto');
var height = $el.height();
var shift = (height - windowHeight) / 2;
if (shift < 0) shift = 0;
$el.css("top",-shift);
} else {
$el.width('auto').height(windowHeight);	
var width = $el.width();
var shift = (width - windowWidth) / 2;
if (shift < 0) shift = 0;
$el.css("left",-shift);
if (shift === 0) {
var t = setTimeout(function() {
$.fn.videoBG.setFullscreen($el);
},500);
}
}
$('body > .videoBG_wrapper');
}
$.fn.videoBG.video = function(options) {
$('html, body').scrollTop(-1);
var $div = $('<div/>');
$div.addClass('videoBG');
var $video = $('<video/>');
$video.css('position','relative')
.css('z-index',options.zIndex)
.attr('poster',options.poster)
.css('max-width','100%')
.css('display','block')
.css('margin','0 auto');
if (options.autoplay) {
$video.attr('autoplay',options.autoplay);
}
if (options.fullscreen) {
$video.bind('canplay',function() {
$video.aspectRatio = $video.width() / $video.height();
$.fn.videoBG.setFullscreen($video);
})
var resizeTimeout;
$(window).resize(function() {
clearTimeout(resizeTimeout);
resizeTimeout = setTimeout(function() {
$.fn.videoBG.setFullscreen($video);
},100);	
});
$.fn.videoBG.setFullscreen($video);
}
var v = $video[0];
if (options.loop) {
loops_left = options.loop;
$video.bind('ended', function(){
if (loops_left)
v.play();
if (loops_left !== true)
loops_left--;
   });
}
$video.bind('canplay', function(){
if (options.autoplay)
v.play();

});
if ($.fn.videoBG.supportsVideo()) {
if ($.fn.videoBG.supportType('webm')){
$video.attr('src',options.webm);
}
else if ($.fn.videoBG.supportType('mp4')) {	
$video.attr('src',options.mp4);
}
else {
$video.attr('src',options.ogv);
}
}
var $img = $('<img/>');
$img.attr('src',options.poster)
.css('position','relative')
.css('z-index',options.zIndex)
.css('max-width','100%')
.css('display','block')
.css('margin','0 auto');
if ($.fn.videoBG.supportsVideo()) {
$div.html($video);
}
else {
$div.html($img);
}
if (options.textReplacement) {
$div.css('min-height',1).css('min-width',1);
$video.css('min-height',1).css('min-width',1);
$img.css('min-height',1).css('min-width',1);
$div.height(options.height).width(options.width);
$video.height(options.height).width(options.width);
$img.height(options.height).width(options.width);	
}
if ($.fn.videoBG.supportsVideo()) {
v.play();
}
return $div;
}
$.fn.videoBG.supportsVideo = function() {
return (document.createElement('video').canPlayType);
}
$.fn.videoBG.supportType = function(str) {
if (!$.fn.videoBG.supportsVideo())
return false;
var v = document.createElement('video');
switch (str) {
case 'webm' :
return (v.canPlayType('video/webm; codecs="vp8, vorbis"'));
break;
case 'mp4' :
return (v.canPlayType('video/mp4; codecs="avc1.42E01E, mp4a.40.2"'));
break;
case 'ogv' :
return (v.canPlayType('video/ogg; codecs="theora, vorbis"'));
break;	
}
return false;	
}
$.fn.videoBG.wrapper = function() {
var $wrap = $('<div/>');
$wrap.addClass('videoBG_wrapper');
return $wrap;
}
$.fn.videoBG.defaults = {
mp4:'',
ogv:'',
webm:'',
poster:'',
autoplay:true,
loop:true,
scale:false,
position:"relative",
opacity:1,
textReplacement:false,
zIndex:0,
width:0,
height:0,
fullscreen:false,
imgFallback:true
}
})( jQuery );