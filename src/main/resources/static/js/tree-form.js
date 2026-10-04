function vocnjakToggleSpeciesOther() {
    var select = document.getElementById("species");
    var field = document.getElementById("speciesOtherField");
    if (!select || !field) {
        return;
    }
    field.style.display = select.value === "__other__" ? "" : "none";
}

document.addEventListener("DOMContentLoaded", vocnjakToggleSpeciesOther);
