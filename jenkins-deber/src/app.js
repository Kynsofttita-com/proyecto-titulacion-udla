// =============================================================================
// Aplicacion de ejemplo para el deber de Jenkins
// =============================================================================

function sumar(a, b) {
    return a + b;
}

function restar(a, b) {
    return a - b;
}

console.log("App iniciada - Demo Jenkins CI/CD");
console.log("Suma de 2 + 3 =", sumar(2, 3));
console.log("Resta de 5 - 2 =", restar(5, 2));

module.exports = { sumar, restar };
