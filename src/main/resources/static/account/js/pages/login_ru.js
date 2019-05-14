/*
 *  Document   : login.js
 *  Author     : pixelcave
 *  Description: Custom javascript code used in Login page
 */

var Login = function() {

    // Function for switching form views (login, reminder and register forms)
    var switchView = function(viewHide, viewShow, viewHash){
        viewHide.slideUp(250);
        viewShow.slideDown(250, function(){
            $('input').placeholder();
        });

        if ( viewHash ) {
            window.location = '#' + viewHash;
        } else {
            window.location = '#';
        }
    };

    return {
        init: function() {

            $('#register-prefix').click(function () {

                $.get("/data/prefix/get", function (data) {
                    $('#register-prefix').html(data);
                    $('#register-prefix-input').val(data);
                });
            });

            /* Switch Login, Reminder and Register form views */
            var formLogin       = $('#form-login'),
                formReminder    = $('#form-reminder'),
                formRegister    = $('#form-register');

            $('#link-register-login').click(function(){
                switchView(formLogin, formRegister, 'register');
            });

            $('#link-register').click(function(){
                switchView(formRegister, formLogin, '');
            });

            $('#link-reminder-login').click(function(){
                switchView(formLogin, formReminder, 'reminder');
            });

            $('#link-reminder').click(function(){
                switchView(formReminder, formLogin, '');
            });

            // If the link includes the hashtag 'register', show the register form instead of login
            if (window.location.hash === '#register') {
                formLogin.hide();
                formRegister.show();
            }

            // If the link includes the hashtag 'reminder', show the reminder form instead of login
            if (window.location.hash === '#reminder') {
                formLogin.hide();
                formReminder.show();
            }

            /*
             *  Jquery Validation, Check out more examples and documentation at https://github.com/jzaefferer/jquery-validation
             */

            /* Login form - Initialize Validation */
            $('#form-login').validate({
                errorClass: 'help-block animation-slideDown', // You can change the animation class for a different entrance animation - check animations page
                errorElement: 'div',
                errorPlacement: function(error, e) {
                    e.parents('.form-group > div').append(error);
                },
                highlight: function(e) {
                    $(e).closest('.form-group').removeClass('has-success has-error').addClass('has-error');
                    $(e).closest('.help-block').remove();
                },
                success: function(e) {
                    e.closest('.form-group').removeClass('has-success has-error');
                    e.closest('.help-block').remove();
                },
                rules: {
                    'username': {
                        required: true,
                        email: true
                    },
                    'password': {
                        required: true,
                        minlength: 8
                    }
                },
                messages: {
                    'username': 'Введите ваш email',
                    'password': {
                        required: 'Пожалуйста, введите пароль',
                        minlength: 'Минимальная длина пароля составляет 8 символов'
                    }
                }
            });

            /* Reminder form - Initialize Validation */
            $('#form-reminder').validate({
                errorClass: 'help-block animation-slideDown', // You can change the animation class for a different entrance animation - check animations page
                errorElement: 'div',
                errorPlacement: function(error, e) {
                    e.parents('.form-group > div').append(error);
                },
                highlight: function(e) {
                    $(e).closest('.form-group').removeClass('has-success has-error').addClass('has-error');
                    $(e).closest('.help-block').remove();
                },
                success: function(e) {
                    e.closest('.form-group').removeClass('has-success has-error');
                    e.closest('.help-block').remove();
                },
                rules: {
                    'username': {
                        required: true,
                        email: true
                    }
                },
                messages: {
                    'username': 'Введите ваш email'
                }
            });

            /* Register form - Initialize Validation */
            $('#form-register').validate({
                errorClass: 'help-block animation-slideDown', // You can change the animation class for a different entrance animation - check animations page
                errorElement: 'div',
                errorPlacement: function(error, e) {
                    e.parents('.form-group > div').append(error);
                },
                highlight: function(e) {
                    $(e).closest('.form-group').removeClass('has-success has-error').addClass('has-error');
                    $(e).closest('.help-block').remove();
                },
                success: function(e) {
                    if (e.closest('.form-group').find('.help-block').length === 2) {
                        e.closest('.help-block').remove();
                    } else {
                        e.closest('.form-group').removeClass('has-success has-error');
                        e.closest('.help-block').remove();
                    }
                },
                rules: {
                    'account': {
                        required: true,
                        minlength: 4
                    },
                    'username': {
                        required: true,
                        email: true
                    },
                    'password': {
                        required: true,
                        minlength: 8
                    },
                    'password-verify': {
                        required: true,
                        equalTo: '#register-password'
                    },
                    'terms': {
                        required: true
                    }
                },
                messages: {
                    'account': {
                        required: 'Введите имя игрового аккаунта',
                        minlength: 'Минимальная длина аккаунта 4 символа'
                    },
                    'username': 'Введите корректный email',
                    'password': {
                        required: 'Пожалуйста введите пароль',
                        minlength: 'Минимальная длина пароля 8 символов'
                    },
                    'password-verify': {
                        required: 'Пожалуйста введите пароль',
                        minlength: 'Минимальная длина пароля 8 символов',
                        equalTo: 'Пароли должны совпадать'
                    },
                    'terms': {
                        required: 'Пожалуйста подтвердите правила'
                    }
                }
            });

        }
    };
}();