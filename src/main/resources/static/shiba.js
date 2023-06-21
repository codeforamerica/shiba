var followUpQuestion = (function() {
	var fUQ = {
		init: function() {
			$('.question-with-follow-up').each(function(index, question) {
				var self = this;
				var showFollowUpIfChecked = function(element) {
					if (element.is(':checked') && element.attr('data-follow-up')
						!== null) {
						$(element.attr('data-follow-up')).show();
					}
				};

				// set initial state of follow-ups based on the page
				$(this).find('input').each(function(index, input) {
					showFollowUpIfChecked($(input));
				});

				// add click listeners to initial question inputs
				$(self).find('.question-with-follow-up__question input').click(
					function(e) {
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
						function(e) {
							showFollowUpIfChecked($(this));
						});

				$(self).find(
					'.question-with-follow-up__question .checkbox input').click(
						function(e) {
							$(this).closest('.form-group').find('input').each(
								function(index, element) {
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

var hasError = (function() {
	var hasInputError = {
		init: function() {
			var invalidInputList = $('input[aria-invalid="true"]').add($('select[aria-invalid="true"]'))
			if (invalidInputList.length >= 1) {
				invalidInputList.each(function(index, input) {
					var inputID = $(input).attr('id');
					// If it's a checkbox use the input name minus the [] otherwise use the input id
					var isCheckboxorDate = $(input).attr('name').includes('date') || $(input).attr('type') === 'checkbox';
					var errorMessageSpans = isCheckboxorDate ? $('.' + $(input).attr('name').slice(0, -2) + '-error') : $('.' + inputID + '-error');
					let inputIdWithHash = '#' + inputID;
					errorMessageSpans.each(function(index, span) {
						var errorMessageSpanID = $(span).attr('id');
						// If the described by doesn't exist yet, set its value to empty string, otherwise use it's current value
						var inputDescribedBy = $(inputIdWithHash).attr('aria-describedby');
						inputDescribedBy = inputDescribedBy ? " " + inputDescribedBy : "";
						// Append the error spans id to the description which may or may not include a helper message id already
						$(inputIdWithHash).attr('aria-describedby', errorMessageSpanID + inputDescribedBy);
					});
					// Check if the input has an existing aria-label and don't set aria-labelledby if so
					var inputLabelID = $(inputIdWithHash + '-label').attr('id');
					var legend = $('#' + $(input).attr('name').slice(0, -2) + '-legend')
					var inputErrorParagraphIDUsingName = $(input).attr('name').slice(0, -2) + '-error-p';
					var legendID = legend.attr('id');
					if ($(input).attr('aria-label')) {
						$(input).attr('aria-label', 'Error ' + $(input).attr('aria-label'));
					} else if ($(input).attr('type') === 'checkbox') {
						if (legend.length > 0) {
							$(input).attr('aria-labelledby', inputErrorParagraphIDUsingName + " " + legendID + " " + inputLabelID);
						}
					} else if ($(input).attr('name').includes('date')) {
						if (legend.length > 0) {
							$(input).attr('aria-labelledby',
								inputErrorParagraphIDUsingName + " " + legendID + " " + inputLabelID);
						}
					} else {
						var inputErrorParagraphID = $(inputIdWithHash + '-error-p').attr('id');
						// Append the error icon id to the input aria-labelledby which causes the SR to read the error-icon ID before the input name like: Error first name
						$(input).attr('aria-labelledby', inputErrorParagraphID + " " + inputLabelID);
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

var noneOfTheAbove = (function() {
	var noneOf = {
		init: function() {
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

var preCheckContactInfo = (function() {
	var preCheck = {
		init: function() {
			var phoneInputTextBox = document.getElementById('phoneNumber');
			var emailInputTextBox = document.getElementById('email');
			var textCheckbox = document.getElementById('TEXT');
			var emailCheckbox = document.getElementById('EMAIL');
			
			if (phoneInputTextBox.value.length === 0) {
			textCheckbox.disabled = true;
			}
			
			if (emailInputTextBox.value.length === 0) {
			emailCheckbox.disabled = true;
			}
			
			if (phoneInputTextBox !== null) {
				phoneInputTextBox.addEventListener('input', function() {
					
					if (phoneInputTextBox.value.length > 0 && !textCheckbox.checked) {
						textCheckbox.checked = true;
						textCheckbox.disabled = false;
						document.querySelector('label[for="TEXT"]').classList.add('is-selected');
					}
					if (phoneInputTextBox.value.length === 0) {
						textCheckbox.checked = false;
						textCheckbox.disabled = true;
						document.querySelector('label[for="TEXT"]').classList.remove('is-selected');
					}
				});
			}
			if (emailInputTextBox !== null) {
				emailInputTextBox.addEventListener('input', function() {
					
					if (emailInputTextBox.value.length > 0 && !emailCheckbox.checked) {
						emailCheckbox.checked = true;
						emailCheckbox.disabled = false;
						document.querySelector('label[for="EMAIL"]').classList.add('is-selected');
					}
					if (emailInputTextBox.value.length === 0) {
						emailCheckbox.checked = false;
						emailCheckbox.disabled = true;
						document.querySelector('label[for="EMAIL"]').classList.remove('is-selected');
					}
				});
			}
		}
	};
	return {
		init: preCheck.init
	}
})();

// This is a temp fix for an issue with Honeycrisp.js which automatically
// sets accordions to closed on page load
function handleAccordions() {
	var accordions = $('.accordion');
	accordions.each(function () {
		var accordion = $(this);
		accordion.hasClass('accordion--is-closed') ?
				accordion.find('button').attr('aria-expanded', 'false') :
				accordion.find('button').attr('aria-expanded', 'true');
	});
}

function handleReveals() {
	var reveals = $('.reveal');
	reveals.each(function () {
		var reveal = $(this);
		reveal.click(function(e) {
			reveal.hasClass('is-hiding-content') ?
					reveal.find('button').attr('aria-expanded', 'false') :
					reveal.find('button').attr('aria-expanded', 'true');
		})
	});
}

$(document).ready(function() {
	// hasError needs to come first in this for accessibility journey tests to pass
	hasError.init();
	followUpQuestion.init();
	noneOfTheAbove.init();
	
	var pathname = window.location.pathname;
	if("/pages/contactInfo" === pathname){
		preCheckContactInfo.init();
	}
	$("#page-form").submit(function() {
		var btn = $("#form-submit-button");
		btn.addClass('button--disabled');
		btn.prop('disabled', true);
		var docBtn = $("#form-doc-submit-button");
		docBtn.prop('disabled', true);
		docBtn.hide();
		$("#wait-button").addClass('button--disabled').prop('disabled', true).css('opacity','0.7').show();
		$("#add-more-doc").addClass('button--disabled').prop('disabled', true).css('pointer-events','none');
		return true;
	});
	$("#prepare-to-apply-accordion").removeClass('accordion--is-closed');
	handleAccordions();
	handleReveals();
});

