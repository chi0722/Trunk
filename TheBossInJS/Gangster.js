var assert = require('assert');
var GANG_TYPE = require('./gangster_type');

function Gangster(type) {

    this.type = type;
    this.pauseHands = 0;
}

Gangster.prototype.imprison = function() {

    this.pauseHands = 2;
}

Gangster.prototype.hospitalize = function() {

    this.pauseHands = 1;
}

Gangster.prototype.inSanction = function() {

    return (this.pausehands > 0);
}

Gangster.prototype.impose = function() {

    assert(this.pauseHands > 0);
    this.pauseHands--;
}

function GangFactory() {}
GangFactory.prototype.createGangster = function(type) {

    if (type !== GANG_TYPE.EXPERT &&
        type !== GANG_TYPE.OCCASIONAL)
        throw "NOT a valid gangster type:" + type;

    return new Gangster(type);
}

module.exports = new GangFactory();

