(function(exports) {
/*
exports.REWARD = 0;
exports.IMPRISON = 1;
exports.HOSPITALIZE = 2;
exports.SHOOT = 3;
exports.BANISH = 4;

exports.MAX_ACTION_TYPE = 5;
*/
var map = [];
var ids = [
    "REWARD",
    "IMPRISON",
    "HOSPITALIZE",
    "SHOOT",
    "BANISH"
];

for (var i = 0; i < ids.length; i++) {
    exports[ids[i]] = i;
    map[i] = String(ids[i]);
}

exports.toString = function(id) {
    return map[id];
}

/*
console.log(map);
console.log(exports.toString(exports.IMPRISON));
*/
})(typeof exports == 'undefined'? this['ACTION_TYPE']={} : exports);
