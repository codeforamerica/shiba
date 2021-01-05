var followUpQuestion = (function() {
    var fUQ = {
        init: function() {
            $('.question-with-follow-up').each(function(index, question) {
                var self = this;
                var showFollowUpIfChecked = function(element) {
                    if(element.is(':checked') && element.attr('data-follow-up') != null) {
                        $(element.attr('data-follow-up')).show();
                    }
                };

                // set initial state of follow-ups based on the page
                $(this).find('input').each(function(index, input) {
                    showFollowUpIfChecked($(input));
                });

                // add click listeners to initial question inputs
                $(self).find('.question-with-follow-up__question input').click(function(e) {
                    // reset follow ups
                    $(self).find('.question-with-follow-up__follow-up input').attr('checked', false);
                    $(self).find('.question-with-follow-up__follow-up').find('input[type="text"], input[type="number"]').val('');
                    $(self).find('.question-with-follow-up__follow-up').find('.radio-button, .checkbox').removeClass('is-selected');
                    $(self).find('.question-with-follow-up__follow-up').hide();
                });

                $(self).find('.question-with-follow-up__question .radio-button input').click(function(e) {
                    showFollowUpIfChecked($(this));
                });

                $(self).find('.question-with-follow-up__question .checkbox input').click(function(e) {
                    $(this).closest('.form-group').find('input').each(function(index, element) {
                        showFollowUpIfChecked($(element));
                    });
                });
            });
        }
    }
    return {
        init: fUQ.init
    }
})();

$(document).ready(function () {
    followUpQuestion.init();
});