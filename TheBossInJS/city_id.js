(function(exports) {
/*
exports.NEW_YORK = 0;
exports.BOSTON = 1;
exports.DETROIT = 2;
exports.KANSAS_CITY = 3;
exports.CINCINNATI = 4;
exports.MEMPHIS = 5;
exports.PHILADELPHIA = 6;
exports.CHICAGO = 7;

exports.MAX_CITY_ID = 8;
*/

var map = [];
var ids = [
    "NEW_YORK",
    "BOSTON",
    "DETROIT",
    "KANSAS_CITY",
    "CINCINNATI",
    "MEMPHIS",
    "PHILADELPHIA",
    "CHICAGO"
];

for (var i = 0; i < ids.length; i++) {
    exports[ids[i]] = i;
    map[i] = String(ids[i]);
}

exports.toString = function(id) {
    return map[id];
}

//console.log(exports.toString(exports.DETROIT));
})(typeof exports == 'undefined'? this['CITY_ID']={} : exports);
