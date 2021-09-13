var followUpQuestion = (function () {
  var fUQ = {
    init: function () {
      $('.question-with-follow-up').each(function (index, question) {
        var self = this;
        var showFollowUpIfChecked = function (element) {
          if (element.is(':checked') && element.attr('data-follow-up')
              != null) {
            $(element.attr('data-follow-up')).show();
          }
        };

        // set initial state of follow-ups based on the page
        $(this).find('input').each(function (index, input) {
          showFollowUpIfChecked($(input));
        });

        // add click listeners to initial question inputs
        $(self).find('.question-with-follow-up__question input').click(
            function (e) {
              // reset follow ups
              $(self).find('.question-with-follow-up__follow-up input').attr(
                  'checked', false);
              $(self).find('.question-with-follow-up__follow-up').find(
                  'input[type="text"], input[type="number"]').val('');
              $(self).find('.question-with-follow-up__follow-up').find(
                  '.radio-button, .checkbox').removeClass('is-selected');
              $(self).find('.question-with-follow-up__follow-up').hide();
            });

        $(self).find(
            '.question-with-follow-up__question .radio-button input').click(
            function (e) {
              showFollowUpIfChecked($(this));
            });

        $(self).find(
            '.question-with-follow-up__question .checkbox input').click(
            function (e) {
              $(this).closest('.form-group').find('input').each(
                  function (index, element) {
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

// Adds role alert after a short delay to the errored input allowing screen readers to catch and read the alert
var hasError = (function () {
  var hasInputError = {
    init: function () {
      var invalidInputList = $('input[aria-invalid="true"]').add($('select[aria-invalid="true"]'))
      if (invalidInputList.length >= 1) {
        invalidInputList.each(function(index, input) {
          var inputID = $(input).attr('id');
          // If it's a checkbox use the input name minus the [] otherwise use the input id
          var errorMessageSpans = $(input).attr('type') === 'checkbox' ? $('.' + $(input).attr('name').slice(0, -2) + '-error') : $('.' + inputID + '-error');
          let inputIdWithHash = '#' + inputID;
          errorMessageSpans.each(function(index, span) {
            var errorMessageSpanID = $(span).attr('id');
            // If the described by doesn't exist yet, set its value to empty string, otherwise use it's current value
            var inputDescribedBy = $(inputIdWithHash).attr('aria-describedby');
            if (inputDescribedBy) {
              inputDescribedBy = " " + inputDescribedBy;
            } else {
              inputDescribedBy = "";
            }
            // Append the error spans id to the description which may or may not include a helper message id already
            $(inputIdWithHash).attr('aria-describedby', errorMessageSpanID + inputDescribedBy);
          });
          // Check if the input has an existing aria-label and don't set aria-labelledby if so
          var inputLabelID = $(inputIdWithHash + '-label').attr('id');
          if ($(input).attr('aria-label')) {
            $(input).attr('aria-label', 'Error ' + $(input).attr('aria-label'));
          } else if ($(input).attr('type') === 'checkbox') {
            var legend = $('#' + $(input).attr('name').slice(0, -2) + '-legend')
            if (legend.length > 0) {
              var inputErrorIconID = $(input).attr('name').slice(0, -2) + '-error-icon';
              var legendID = legend.attr('id');
              $(input).attr('aria-labelledby', inputErrorIconID + " " + legendID + " " + inputLabelID);
            }
          } else {
            var inputErrorIconID = $(inputIdWithHash + '-error-icon').attr('id');
            // Append the error icon id to the input aria-labelledby which causes the SR to read the error-icon ID before the input name like: Error first name
            $(input).attr('aria-labelledby', inputErrorIconID + " " + inputLabelID);
          }
        })
        setTimeout(function() {
          var inputId = invalidInputList.first().attr('id');
          document.getElementById(inputId).focus();
        }, 500)
      }
    }
  }
  return {
    init: hasInputError.init
  }
})();

var noneOfTheAbove = (function () {
  var noneOf = {
    init: function () {
      var $noneCheckbox = $('#none__checkbox');
      var $otherCheckboxes = $('input[type=checkbox]').not('#none__checkbox');

      // Uncheck None if another checkbox is checked
      $otherCheckboxes.click(function (e) {
        $noneCheckbox.prop('checked', false);
        $noneCheckbox.parent().removeClass('is-selected');
        $('#choose-none-warning').addClass('hidden');
      });

      if ($noneCheckbox.prop('checked')) {
        $('#choose-none-warning').removeClass("hidden");
      }

      // Uncheck all others if None is checked
      $noneCheckbox.click(function (e) {
        $otherCheckboxes.prop('checked', false);
        $otherCheckboxes.parent().removeClass('is-selected');
        if (this.checked) {
          $('#choose-none-warning').removeClass('hidden');
        } else {
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
  hasError.init();
  $("#page-form").submit(function () {
    var btn = $("#form-submit-button");
    btn.addClass('button--disabled');
    btn.prop('disabled', true)
    return true;
  });
});