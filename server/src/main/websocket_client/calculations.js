$(document).ready(function () {

    var idealWeight;

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
        var dosisFentanyl = $('#dropDownMedication2 :selected').text();
        dosisFentanyl = parseFloat(dosisFentanyl);
        var dosis2 = ((dosisFentanyl / idealWeight) * 1000).toFixed(1);
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

});

