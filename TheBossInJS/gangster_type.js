/*
exports.EXPERT = 0;
exports.OCCASIONAL = 1;
*/

var map = [];
var ids = [
    "EXPERT",
    "OCCASIONAL"
];

for (var i = 0; i < ids.length; i++) {
    exports[ids[i]] = i;
    map[i] = String(ids[i]);
}

exports.toString = function(id) {
    return map[id];
}

//console.log(exports.toString(exports.EXPERT));
