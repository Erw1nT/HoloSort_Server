$(document).ready(function () {

    // Check Catheter
    $("#checkArterial").click(function () {
        console.log("Arterial checked");
        $("#arterialInput").prop("disabled", false);
        $("#dropDownArterial").prop("disabled", false);
        $("#CVCInput").prop("disabled", true);
        $("#dropDownCVC").prop("disabled", true);
        $("#PVCInput").prop("disabled", true);
        $("#dropDownPVC").prop("disabled", true);

        $("#dropDownPVC").prop('selectedIndex', 0);
        $("#dropDownCVC").prop('selectedIndex', 0);
        $("#PVCInput").val("");
        $("#CVCInput").val("");
    });

    $("#checkCVC").click(function() {
        console.log("CVC checked");
        $("#CVCInput").prop("disabled", false);
        $("#dropDownCVC").prop("disabled", false);
        $("#PVCInput").prop("disabled", true);
        $("#dropDownPVC").prop("disabled", true);
        $("#arterialInput").prop("disabled", true);
        $("#dropDownArterial").prop("disabled", true);


        $("#dropDownPVC").prop('selectedIndex', 0);
        $("#dropDownArterial").prop('selectedIndex', 0);
        $("#PVCInput").val("");
        $("#arterialInput").val("");
    });

    $("#checkPVC").click(function() {
        console.log("PVC checked");
        $("#PVCInput").prop("disabled", false);
        $("#dropDownPVC").prop("disabled", false);
        $("#CVCInput").prop("disabled", true);
        $("#dropDownCVC").prop("disabled", true);
        $("#arterialInput").prop("disabled", true);
        $("#dropDownArterial").prop("disabled", true);


        $("#dropDownPVC").prop('selectedIndex', 0);
        $("#dropDownArterial").prop('selectedIndex', 0);
        $("#arterialInput").val("");
        $("#CVCInput").val("");
    });


    // Check Tube
    $("#checkTube1").click(function() {
        console.log("Tube 1 checked");
        $("#dropDownTube1").prop("disabled", false);
        $("#dropDownTube2").prop("disabled", true);
        $("#dropDownTube3").prop("disabled", true);

        $("#dropDownTube2").prop('selectedIndex', 0);
        $("#dropDownTube3").prop('selectedIndex', 0);

    });

    $("#checkTube2").click(function() {
        console.log("Tube 2 checked");
        $("#dropDownTube1").prop("disabled", true);
        $("#dropDownTube2").prop("disabled", false);
        $("#dropDownTube3").prop("disabled", true);

        $("#dropDownTube1").prop('selectedIndex', 0);
        $("#dropDownTube3").prop('selectedIndex', 0);

    });

    $("#checkTube3").click(function () {
        console.log("Tube 3 checked");
        $("#dropDownTube1").prop("disabled", true);
        $("#dropDownTube2").prop("disabled", true);
        $("#dropDownTube3").prop("disabled", false);

        $("#dropDownTube2").prop('selectedIndex', 0);
        $("#dropDownTube1").prop('selectedIndex', 0);
    });

    $(".module").find('input, input[class="form-control"]').on("cut copy paste", function(e) {
        e.preventDefault();
    })


});