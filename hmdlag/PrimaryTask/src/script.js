$(document).ready(function () {

    var idealWeight;
    var buttonID = 0;
    var amountOfErrors = 0;
    var timestamp;
    var jsonObj;
    var trial = 1;
    var trialString = "";

    // Patient Information disabled
    $("#height, #weight, #checkAllergies1, #checkAllergies2, #checkAllergies3, #checkAllergiesInput, #allergiesInput").prop("disabled", true);
    // Medication disabled
    $("#dropDownMedication1, #dropDownMedication2, #dropDownMedication3, #timeMedication1, #timeMedication2, #timeMedication3").prop("disabled", true);
    // Respiratory Tract disabled
    $("#dropDownRT1, #dropDownRT2, #dropDownRT3").prop("disabled", true);
    // Catheter disabled
    $("#checkArterial, #arterialInput, #dropDownArterial, #checkCVC, #CVCInput, #dropDownCVC, #checkPVC, #PVCInput, #dropDownPVC").prop("disabled", true);
    // Tube disabled
    $("#checkTube1, #dropDownTube1, #checkTube2, #dropDownTube2, #checkTube3, #timeTube").prop("disabled", true);
    // Positioning disabled
    $("#supinePos, #torsoHigh, #torsoLow, #stomach, #foamShell, #gelRing, #spineOrtho, #shoulderSupport").prop("disabled", true);

    // Calculates ideal body weight
    var height = $("#height");
    height.keyup(function () {
        idealWeight = 22 * ((height.val() / 100) * (height.val() / 100));
        idealWeight = idealWeight.toFixed(1);
        $("#bodyWeight").val(idealWeight);
        $("#bodyWeight").text(idealWeight + " kg");
    });


    // Calculates Dosis per Kilo
    $("#dropDownMedication1").change(function () {
        var dosisSterofundin = $('#dropDownMedication1 :selected').text();
        dosisSterofundin = parseInt(dosisSterofundin);
        var dosis1 = (dosisSterofundin / idealWeight).toFixed(1);
        $("#actualStero").val(dosis1);
        $("#actualStero").text(dosis1);
    });

    $("#dropDownMedication2").change(function () {
        var dosisFenanyl = $('#dropDownMedication2 :selected').text();
        dosisFenanyl = parseFloat(dosisFenanyl);
        var dosis2 = ((dosisFenanyl / idealWeight) * 1000).toFixed(1);
        $("#actualFena").val(dosis2);
        $("#actualFena").text(dosis2);
    });

    $("#dropDownMedication3").change(function () {
        var dosisPropofol = $('#dropDownMedication3 :selected').text();
        dosisPropofol = parseInt(dosisPropofol);
        var dosis3 = ((dosisPropofol / idealWeight) * 10).toFixed(1);
        $("#actualProp").val(dosis3);
        $("#actualProp").text(dosis3);
    });


    // Saves variable when OK button clicked
    $(function () {
        var buttonpressed;
        $(".okButton").click(function () {
            buttonpressed = $(this).attr('id');

            if (buttonpressed == "okPatientInformation") {
                buttonID = 1;
            } else if (buttonpressed == "okMedication") {
                buttonID = 2;
            } else if (buttonpressed == "okRT") {
                buttonID = 3;
            } else if (buttonpressed == "okCatheter") {
                buttonID = 4;
            } else if (buttonpressed == "okTube") {
                buttonID = 5;
            } else if (buttonpressed == "okPositioning") {
                buttonID = 6;
            }
        })
    });


    // Enables editing when edit button is clicked and shows error message
    $("#editPatientInformation").click(function () {
        if (buttonID !== 0) {
            $("#error1").css('visibility', 'visible');
            setTimeout(function () {
                $("#error1").css('visibility', 'hidden');
            }, 2000);

            amountOfErrors++;

            return;
        }

        if ($("#editPatientInformation").data('clicked')) {
            return;
        } else {
            $("#height, #weight, #checkAllergies1, #checkAllergies2, #checkAllergies3, #checkAllergiesInput, #allergiesInput").prop("disabled", false);
            $(this).data('clicked', true);
        }
    });

    $("#editMedication").click(function () {
        if (buttonID !== 1) {
            $("#error2").css('visibility', 'visible');
            setTimeout(function () {
                $("#error2").css('visibility', 'hidden');
            }, 2000);

            amountOfErrors++;

            return;
        }

        if ($("#editMedication").data('clicked')) {
            return;
        } else {
            $("#dropDownMedication1, #dropDownMedication2, #dropDownMedication3, #timeMedication1, #timeMedication2, #timeMedication3").prop("disabled", false);
            $(this).data('clicked', true);
        }
    });

    $("#editRT").click(function () {
        if (buttonID !== 3) {
            $("#error3").css('visibility', 'visible');
            setTimeout(function () {
                $("#error3").css('visibility', 'hidden');
            }, 2000);

            amountOfErrors++;

            return;
        }

        if ($("#editRT").data('clicked')) {
            return;
        } else {
            $("#dropDownRT1, #dropDownRT2, #dropDownRT3").prop("disabled", false);
            $(this).data('clicked', true);
        }
    });

    $("#editCatheter").click(function () {
        if (buttonID !== 4) {
            $("#error4").css('visibility', 'visible');
            setTimeout(function () {
                $("#error4").css('visibility', 'hidden');
            }, 2000);

            amountOfErrors++;

            return;
        }

        if ($("#editCatheter").data('clicked')) {
            return;
        } else {
            $("#checkArterial, #arterialInput, #dropDownArterial, #checkCVC, #CVCInput, #dropDownCVC, #checkPVC, #PVCInput, #dropDownPVC").prop("disabled", false);
            $(this).data('clicked', true);
        }
    });

    $("#editTube").click(function () {
        if (buttonID !== 5) {
            $("#error5").css('visibility', 'visible');
            setTimeout(function () {
                $("#error5").css('visibility', 'hidden');
            }, 2000);

            amountOfErrors++;

            return;
        }

        if ($("#editTube").data('clicked')) {
            return;
        } else {
            $("#checkTube1, #dropDownTube1, #checkTube2, #dropDownTube2, #checkTube3, #timeTube").prop("disabled", false);
            $(this).data('clicked', true);
        }
    });

    $("#editPositioning").click(function () {
        if (buttonID !== 6) {
            $("#error6").css('visibility', 'visible');
            setTimeout(function () {
                $("#error6").css('visibility', 'hidden');
            }, 2000);

            amountOfErrors++;

            return;
        }

        if ($("#editPositioning").data('clicked')) {
            return;
        } else {
            $("#supinePos, #torsoHigh, #torsoLow, #stomach, #foamShell, #gelRing, #spineOrtho, #shoulderSupport").prop("disabled", false);
            $(this).data('clicked', true);
        }
    });


    // Deletes input when clicking OK button
    $(".okButton").click(function () {
        $(".module").find('input, input[type="checkbox"]').val("");
        $('input[type="checkbox"]').prop("checked", false);
        $("#dropDownMedication1, #dropDownMedication2, #dropDownMedication2, #dropDownMedication3").prop('selectedIndex', 0);
        $("#dropDownRT1, #dropDownRT2, #dropDownRT3").prop('selectedIndex', 0);
        $("#dropDownArterial, #dropDownCVC, #dropDownPVC").prop('selectedIndex', 0);
        $("#dropDownTube1, #dropDownTube2").prop('selectedIndex', 0);
    });

    // Get timestamps
    $(".editButton").click(function() {
        var now = new Date();
            timestamp += ((now.getDate()) + '/' + now.getMonth() + '/' + now.getFullYear() + " " + now.getHours() + ':'
                + ((now.getMinutes() < 10) ? ("0" + now.getMinutes()) : (now.getMinutes())) + ':' + ((now.getSeconds() < 10) ? ("0" + now
                    .getSeconds()) : (now.getSeconds())) + ':' + now.getMilliseconds() + ", ");
    });


    // Creates and saves JSON object
    $("#processButton").each(function() {
        $(this).click(function() {
            var errors = amountOfErrors.toString();
            var time = timestamp.toString();
            jsonObj = [];

            jsonObj.push({Trial: trial, Errors: errors, Time: time});
            var jsonString = JSON.stringify(jsonObj);

            trialString += jsonString;

            trial++;
            amountOfErrors = 0;
            timestamp = 0;

            if(trial == 3) {
                download(trialString, 'json.txt', 'text/plain');
            }
        });
    });

    function download(content, fileName, contentType) {
        var a = document.createElement("a");
        var file = new Blob([content], {type: contentType});
        a.href = URL.createObjectURL(file);
        a.download = fileName;
        a.click();
    }

/*Overlay "verdeckung" an und ausschalten
function unsichtbar() {
  document.getElementById("verdeckung").style.display = "block";
}

function sichtbar() {
  document.getElementById("verdeckung").style.display = "none";
}
*/




});



