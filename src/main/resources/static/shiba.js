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

var noneOfTheAbove = (function() {
    var noneOf = {
        init: function () {
            var $noneCheckbox = $('#none__checkbox');
            var $otherCheckboxes = $('input[type=checkbox]').not('#none__checkbox');

            // Uncheck None if another checkbox is checked
            $otherCheckboxes.click(function(e) {
                $noneCheckbox.prop('checked', false);
                $noneCheckbox.parent().removeClass('is-selected');
                $('#choose-none-warning').addClass('hidden');
            });

            if ($noneCheckbox.prop('checked')) {
                $('#choose-none-warning').removeClass("hidden");
            }

            // Uncheck all others if None is checked
            $noneCheckbox.click(function(e) {
                $otherCheckboxes.prop('checked', false);
                $otherCheckboxes.parent().removeClass('is-selected');
                if (this.checked) {
                    $('#choose-none-warning').removeClass('hidden');
                }
                else {
                    $('#choose-none-warning').addClass('hidden');
                }
            });
        }
    };
    return {
        init: noneOf.init
    }
})();

$(document).ready(function () {
    followUpQuestion.init();
    noneOfTheAbove.init();
    $("#page-form").submit(function () {
        var btn = $("#form-submit-button");
        btn.addClass('button--disabled');
        btn.prop('disabled', true)
        return true;
    });
});