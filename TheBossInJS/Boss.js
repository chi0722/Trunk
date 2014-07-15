var CITY_ID = require("./city_id");
var GANG_TYPE = require("./gangster_type");
var GangFactory = require("./gangster");
var CITIES = require("./city").CITIES;

var MAX_NUM_OF_EXPERTS = 6;
var MAX_NUM_OF_OCCASIONALS = 3;

Array.prototype.remove = function(elem) {
    this.splice(this.indexOf(elem), 1);
}

function Boss(name, id) {

    this.name = name;
    this.id = id;
    this.money = 0;
    this.actionsInHand = [];
    this.expertsInHand = [];
    this.occsInHand = [];
    this.expertsInCity = [];
    this.expertsInPrison = [];
    this.expertsInHospital = [];

    var i;
    for (i = 0; i < MAX_NUM_OF_EXPERTS; i++) {
        this.expertsInHand.push(
                GangFactory.createGangster(GANG_TYPE.EXPERT));
    }
    for (i = 0; i < MAX_NUM_OF_OCCASIONALS; i++) {
        this.occsInHand.push(
                GangFactory.createGangster(GANG_TYPE.OCCASIONAL));
    }
}

Boss.prototype.getActionCities = function() {

    var cities = [];
    for (var i = 0; i < this.actionsInHand.length; i++) {
        var a = this.actionsInHand[i];
        cities[i] = a.cid;
    }
    return cities;
}

Boss.prototype.nextHand = function(actions) {

    // put back experts used in the previous hand
    var i;
    for (i = 0; i < this.expertsInPrison.length; i++) {
        var e = this.expertsInPrison[i];
        if (e.inSanction())
            e.impose(); // don't put back but impose once
        else {
            this.expertsInHand.push(e);
            this.expertsInPrison.splice(i, 1);
            i--;
        }
    }

    for (i = 0; i < this.expertsInHospital.length; i++) {
        var e = this.expertsInHospital[i];
        if (e.inSanction())
            e.impose();
        else {
            this.expertsInHand.push(e);
            this.expertsInHospital.splice(i, 1);
            i--;
        }
    }

    this.expertsInHand = this.expertsInHand.concat(this.expertsInCity);
    this.expertsInCity.length = 0;

    actions.sort();
    this.actionsInHand = actions;
}

Boss.prototype.unveilAction = function(action) {

    console.log("Boss(" + this.name + ") shows" + action.toString());
    this.actionsInHand.remove(action);
    var city = CITIES[action.cid];
    city.showAction(action);  
}

Boss.prototype.occupy = function(city, numOfExperts, numOfOccs) {

    var i;
    for (i = 0; i < numOfExperts; i++) {
        var e = this.expertsInHand.shift();
        this.expertsInCity.push(e);
        city.occupy(this, e);
    }

    for (i = 0; i < numOfOccs; i++) {
        var e = this.occsInHand.shift();
        city.occupy(this, e);
    }
}

Boss.prototype.reward = function(money) { this.money += money; }

Boss.prototype.shoot = function(experts) {

    for (var i = 0; i < experts.length; i++) {
        var e = experts[i];
        this.expertsInCity.remove(e);
    }
}

Boss.prototype.imprison = function(experts) {

    for (var i = 0; i < experts.length; i++) {
        var e = experts[i];
        this.expertsInCity.remove(e);
        this.expertsInPrison.push(e);
        e.imprison();
    }
}

Boss.prototype.hospitalize = function(experts) {

    for (var i = 0; i < experts.length; i++) {
        var e = experts[i];
        this.expertsInCity.remove(e);
        this.expertsInHospital.push(e);
        e.hospitalize();
    }
}

Boss.prototype.toString = function() {

    var str = "::" + this.name + ":: id = " + this.id + "\n";
    
    for (var i = 0; i < this.actionsInHand.length; i++) {
        var a = this.actionsInHand[i];
        str += "\t" + a.toString() + "\n";
    }
    //str += this.actionsInHand.toString();
    str += "\tLeft Experts: " + this.expertsInHand.length;
    str += "\n\tLeft Occasionals: " + this.occsInHand.length;
    str += "\n\tIn Prison: " + this.expertsInPrison.length;
    str += "\n\tIn Hospital: " + this.expertsInHospital.length;    

    return str;
}

var id = 0;
function BossFactory() {}
BossFactory.prototype.reset = function() { id = 0; }
BossFactory.prototype.getTotalId = function() { return id; }
BossFactory.prototype.createBoss = function(name) {

    return new Boss(name, id++);
}
module.exports = new BossFactory();
